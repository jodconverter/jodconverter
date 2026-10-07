/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.core.office;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.StringUtils;

/**
 * An office manager that dispatches the tasks to a pool of {@link OfficeWorker}s.
 *
 * <p>The tasks wait in a single queue. Each worker has its own thread, which takes a task from the
 * queue only when the worker is ready: a worker that is starting, or restarting, is not given any
 * task, and the tasks go to the workers that are ready.
 *
 * <p>Two timeouts apply to a task:
 *
 * <ul>
 *   <li>The <em>task queue timeout</em> runs from the moment the task is submitted until a worker
 *       takes it. Waiting for a worker to be ready is part of it.
 *   <li>The <em>task execution timeout</em> runs from the moment a worker starts executing the
 *       task. When it expires, the task fails, its worker is aborted and then restarted.
 * </ul>
 *
 * <p>A worker that cannot be made ready is retried, with a growing delay between the attempts.
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class AbstractOfficeWorkerPool implements OfficeManager, TemporaryFileMaker {

  /** The default maximum time a task waits in the queue for a worker. */
  public static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30_000L; // 30 seconds

  /** The default maximum time allowed to execute a task. */
  public static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120_000L; // 2 minutes

  /** The default maximum number of tasks waiting in the queue; 0 means no limit. */
  public static final int DEFAULT_TASK_QUEUE_CAPACITY = 0;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfficeWorkerPool.class);

  private static final int POOL_STOPPED = 0;
  private static final int POOL_STARTED = 1;
  private static final int POOL_SHUTDOWN = 2;

  private static final String ERROR_NOT_RUNNING = "This office manager is not running.";

  // The delays, in milliseconds, before each new attempt to make a worker ready. The last one
  // is used for all the following attempts.
  private static final long[] DEFAULT_RESTART_DELAYS = {
    0L, 1_000L, 2_000L, 5_000L, 10_000L, 30_000L
  };

  // How often, in milliseconds, an idle worker checks that it is still ready.
  private static final long DEFAULT_IDLE_CHECK_INTERVAL = 1_000L;

  private final AtomicInteger poolState = new AtomicInteger(POOL_STOPPED);
  private final File tempDir;
  private final AtomicLong tempFileCounter = new AtomicLong(0);
  private final long taskQueueTimeout;
  private final long taskExecutionTimeout;
  private final int taskQueueCapacity;
  private final boolean startFailFast;
  private final BlockingDeque<OfficeJob> queue = new LinkedBlockingDeque<>();
  private final ThreadFactory threadFactory = new NamedThreadFactory("jodconverter-worker");

  private List<OfficeWorker> workers = List.of();
  private volatile List<OfficeWorkerRunner> runners = List.of();
  private ScheduledThreadPoolExecutor watchdog;

  // Changed by the tests only.
  private long[] restartDelays = DEFAULT_RESTART_DELAYS;
  private long idleCheckInterval = DEFAULT_IDLE_CHECK_INTERVAL;

  /**
   * Creates a pool. The workers are given by the subclass with {@link #setWorkers(List)}, before
   * the pool is started.
   *
   * @param workingDir The directory where the temporary directory of the pool is created.
   * @param taskQueueTimeout The maximum time a task waits in the queue, in milliseconds.
   * @param taskExecutionTimeout The maximum time allowed to execute a task, in milliseconds.
   * @param taskQueueCapacity The maximum number of tasks waiting in the queue; 0 means no limit.
   * @param startFailFast Whether {@link #start()} waits for all the workers to be ready, and fails
   *     if one cannot be. Otherwise {@code start()} returns at once, and a worker that cannot be
   *     made ready is retried.
   */
  protected AbstractOfficeWorkerPool(
      final @NonNull File workingDir,
      final long taskQueueTimeout,
      final long taskExecutionTimeout,
      final int taskQueueCapacity,
      final boolean startFailFast) {
    super();

    Objects.requireNonNull(workingDir, "workingDir must not be null");

    this.taskQueueTimeout = taskQueueTimeout;
    this.taskExecutionTimeout = taskExecutionTimeout;
    this.taskQueueCapacity = taskQueueCapacity;
    this.startFailFast = startFailFast;
    this.tempDir = new File(workingDir, ".jodconverter_" + UUID.randomUUID());
  }

  /**
   * Sets the workers of this pool.
   *
   * @param workers The workers.
   */
  protected void setWorkers(final @NonNull List<? extends @NonNull OfficeWorker> workers) {
    AssertUtils.notEmpty(workers, "workers must not be null or empty");
    this.workers = List.copyOf(workers);
  }

  // For the tests: shorter delays than the default ones.
  /* default */ void setRestartDelays(final long... restartDelays) {
    this.restartDelays = restartDelays.clone();
  }

  // For the tests: a shorter interval than the default one.
  /* default */ void setIdleCheckInterval(final long idleCheckInterval) {
    this.idleCheckInterval = idleCheckInterval;
  }

  @Override
  public final void start() throws OfficeException {

    final List<OfficeWorkerRunner> newRunners;
    synchronized (this) {
      if (poolState.get() == POOL_SHUTDOWN) {
        throw new IllegalStateException("This office manager has been shutdown.");
      }
      if (poolState.get() == POOL_STARTED) {
        throw new IllegalStateException("This office manager is already running.");
      }
      AssertUtils.isTrue(!workers.isEmpty(), "This office manager has no worker");

      prepareTempDir();

      watchdog =
          new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("jodconverter-watchdog"));
      watchdog.setRemoveOnCancelPolicy(true);

      // From here the tasks are accepted; they wait in the queue until a worker is ready.
      newRunners = new ArrayList<>();
      for (final var worker : workers) {
        newRunners.add(new OfficeWorkerRunner(this, worker, startFailFast));
      }
      runners = newRunners;
      poolState.set(POOL_STARTED);
      newRunners.forEach(runner -> runner.start(threadFactory));
    }

    if (startFailFast) {
      // Outside the monitor: a stop() from another thread aborts the starts, instead of waiting
      // for them, and this start then fails.
      try {
        for (final var runner : newRunners) {
          awaitFirstStart(runner);
        }
      } catch (OfficeException ex) {
        // A pool that could not be started cannot be used anymore.
        synchronized (this) {
          if (poolState.get() != POOL_SHUTDOWN) {
            shutdown();
          }
        }
        throw ex;
      }
    }
  }

  private void awaitFirstStart(final OfficeWorkerRunner runner) throws OfficeException {
    try {
      runner.getFirstStart().get();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new OfficeException("Interruption while starting the office manager", ex);
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof OfficeException officeEx) {
        throw officeEx;
      }
      throw new OfficeException("Could not start the office manager", ex.getCause());
    }
  }

  @Override
  public final void stop() throws OfficeException {

    synchronized (this) {
      if (poolState.get() == POOL_SHUTDOWN) {
        // Already shutdown, just exit
        return;
      }
      if (poolState.get() == POOL_STOPPED) {
        // Never started: there is nothing to stop.
        poolState.set(POOL_SHUTDOWN);
        return;
      }

      LOGGER.info("Stopping the office manager pool...");
      shutdown();
    }
  }

  // Stops the workers, fails the tasks that were not executed, and releases the resources.
  private void shutdown() {

    poolState.set(POOL_SHUTDOWN);
    try {
      runners.forEach(OfficeWorkerRunner::requestStop);
      for (final var runner : runners) {
        runner.join();
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      LOGGER.warn("Interruption while waiting for the office workers to stop", ex);
    } finally {
      watchdog.shutdownNow();
      // The tasks still in the queue will never be executed.
      for (OfficeJob job = queue.pollFirst(); job != null; job = queue.pollFirst()) {
        failStopped(job);
      }
      deleteTempDir();
    }
  }

  private void failStopped(final OfficeJob job) {
    if (job.tryEndWaiting()) {
      job.getFuture()
          .completeExceptionally(
              new OfficeException(
                  String.format(
                      "Task was not executed, the office manager is stopped: %s", job.getTask())));
    }
  }

  @Override
  public final boolean isRunning() {

    if (poolState.get() == POOL_STARTED) {
      // Check for at least one worker that is ready, or executing a task.
      for (final var runner : runners) {
        final var state = runner.getState();
        if (state == OfficeWorkerState.READY || state == OfficeWorkerState.BUSY) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets a snapshot of this pool: the status of each worker, in the order of the workers, and the
   * number of tasks waiting in the queue.
   *
   * @return The status of the pool; without any worker if the pool was never started.
   */
  public @NonNull OfficeWorkerPoolStatus getStatus() {
    return new OfficeWorkerPoolStatus(
        runners.stream().map(OfficeWorkerRunner::getStatus).toList(), queue.size());
  }

  /**
   * Submits a task, to be executed as soon as a worker is ready, and returns at once.
   *
   * <p>The returned future completes when the task is done. It completes exceptionally, with an
   * {@link OfficeException}, when the task fails, when no worker took it within the task queue
   * timeout, when its execution exceeds the task execution timeout, when the queue is full, or when
   * the manager is stopped first. Cancelling the future removes the task from the queue, or aborts
   * its execution if it has started, which makes its worker restart.
   *
   * <p>The actions chained to the future may run on the thread of a worker: they must not block.
   *
   * @param task The task to execute.
   * @return The future of the task.
   * @throws IllegalStateException If this manager is not running.
   */
  @Override
  public final @NonNull CompletableFuture<Void> submit(final @NonNull OfficeTask task) {

    Objects.requireNonNull(task, "task must not be null");
    if (poolState.get() != POOL_STARTED) {
      throw new IllegalStateException(ERROR_NOT_RUNNING);
    }

    final var job = new OfficeJob(task);
    final var future = job.getFuture();

    if (taskQueueCapacity > 0 && queue.size() >= taskQueueCapacity) {
      job.tryEndWaiting();
      future.completeExceptionally(
          new OfficeException(
              String.format(
                  "The task queue is full (%d tasks waiting): %s", taskQueueCapacity, task)));
      return future;
    }

    // A caller that cancels the future does not want the task to be executed, or to go on.
    future.whenComplete(
        (result, failure) -> {
          if (future.isCancelled()) {
            cancel(job);
          }
        });

    enqueue(job);
    return future;
  }

  /**
   * Adds a job to the queue, with its queue timeout.
   *
   * @param job The job.
   */
  /* default */ void enqueue(final OfficeJob job) {

    job.setTimeout(schedule(() -> onQueueTimeout(job), taskQueueTimeout));
    queue.addLast(job);

    // The manager may have been stopped while the job was added, after the queue was emptied.
    // Failing the job does nothing if the stop already did it.
    if (poolState.get() != POOL_STARTED) {
      queue.remove(job);
      failStopped(job);
    }
  }

  @Override
  public final void execute(final @NonNull OfficeTask task) throws OfficeException {

    final var future = submit(task);
    try {
      future.get();
    } catch (InterruptedException ex) {
      // The waiting thread was interrupted: the task must not keep running for nobody.
      future.cancel(true);
      Thread.currentThread().interrupt();
      throw new OfficeException(
          String.format("Task was interrupted while executing: %s", task), ex);
    } catch (ExecutionException ex) {
      // The tasks always fail with an OfficeException.
      throw (OfficeException) ex.getCause();
    }
  }

  // Schedules an action of the watchdog. Returns null when the pool is being stopped.
  private Future<?> schedule(final Runnable action, final long delay) {
    try {
      return watchdog.schedule(action, delay, TimeUnit.MILLISECONDS);
    } catch (RejectedExecutionException ex) {
      LOGGER.debug("The watchdog is stopped; no timeout scheduled", ex);
      return null;
    }
  }

  /* default */ void onQueueTimeout(final OfficeJob job) {
    if (job.tryEndWaiting()) {
      queue.remove(job);
      job.getFuture()
          .completeExceptionally(
              new OfficeException(
                  String.format(
                      "No office manager available after %d millisec", taskQueueTimeout)));
    }
  }

  /* default */ void onExecutionTimeout(final OfficeJob job) {
    if (job.tryEndRunning()) {
      LOGGER.warn("Task did not complete within {} ms; aborting it", taskExecutionTimeout);
      job.getFuture()
          .completeExceptionally(
              new OfficeException(
                  String.format(
                      "Task did not complete within timeout (%s ms): %s",
                      taskExecutionTimeout, job.getTask()),
                  new TimeoutException()));
      job.getRunner().abort(job);
    }
  }

  /* default */ void cancel(final OfficeJob job) {
    if (job.tryEndWaiting()) {
      queue.remove(job);
    } else if (job.tryEndRunning()) {
      job.getRunner().abort(job);
    }
  }

  /**
   * Starts the execution timeout of a job that a worker is about to execute.
   *
   * @param job The job.
   */
  /* default */ void scheduleExecutionTimeout(final OfficeJob job) {
    job.setTimeout(schedule(() -> onExecutionTimeout(job), taskExecutionTimeout));
  }

  /**
   * Waits for a job, for the workers that are ready.
   *
   * @return A job, or null if none arrived within the idle check interval, or if the waiting thread
   *     was interrupted.
   */
  /* default */ OfficeJob takeJob() {
    try {
      return queue.pollFirst(idleCheckInterval, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ex) {
      // The worker is being stopped: its runner checks it.
      LOGGER.trace("Interrupted while waiting for a task", ex);
      return null;
    }
  }

  /**
   * Puts back, at the head of the queue, a job that a worker took but cannot execute.
   *
   * @param job The job.
   */
  /* default */ void requeueJob(final OfficeJob job) {
    queue.addFirst(job);
  }

  /**
   * Gets the delay before a new attempt to make a worker ready.
   *
   * @param failures The number of attempts that failed so far, starting at 0.
   * @return The delay, in milliseconds.
   */
  /* default */ long getRestartDelay(final int failures) {
    return restartDelays[Math.min(failures, restartDelays.length - 1)];
  }

  /**
   * Gets the exception a job fails with, from what its execution threw.
   *
   * @param job The job.
   * @param failure What the execution of the task threw.
   * @return The failure itself if it is an office exception, or an office exception caused by it.
   */
  /* default */ OfficeException toOfficeException(final OfficeJob job, final Throwable failure) {
    if (failure instanceof OfficeException officeEx) {
      return officeEx;
    }
    return new OfficeException(String.format("Task did not complete: %s", job.getTask()), failure);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void prepareTempDir() throws OfficeException {

    if (tempDir.exists()) {
      LOGGER.warn("Temporary directory '{}' already exists; deleting", tempDir);
      deleteTempDir();
    }

    tempDir.mkdirs();
    if (!tempDir.isDirectory()) {
      throw new OfficeException(String.format("Cannot create temporary directory: %s", tempDir));
    }
  }

  private void deleteTempDir() {
    OfficeUtils.deleteOrRenameFile(tempDir, 0L, 0L);
  }

  /**
   * Gets the directory where the temporary files of this manager are created. It is created when
   * the manager starts and deleted when it stops.
   *
   * @return The temporary directory.
   */
  /* default */ @NonNull File getTempDir() {
    return tempDir;
  }

  @Override
  public @NonNull File makeTemporaryFile(final @Nullable String extension) {
    return new File(
        tempDir,
        "tempfile_"
            + tempFileCounter.getAndIncrement()
            + (StringUtils.isBlank(extension) ? "" : "." + extension));
  }

  /**
   * A builder for constructing an {@link AbstractOfficeWorkerPool}.
   *
   * @see AbstractOfficeWorkerPool
   */
  @SuppressWarnings("unchecked")
  public abstract static class AbstractOfficeWorkerPoolBuilder<
      B extends AbstractOfficeWorkerPoolBuilder<B>> {

    protected boolean install;
    protected File workingDir = OfficeUtils.getDefaultWorkingDir();
    protected long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
    protected int taskQueueCapacity = DEFAULT_TASK_QUEUE_CAPACITY;
    protected long taskQueueTimeout = DEFAULT_TASK_QUEUE_TIMEOUT;

    /** Creates a builder; only the subclasses can. */
    protected AbstractOfficeWorkerPoolBuilder() {
      super();
    }

    /**
     * Creates the manager that is specified by this builder.
     *
     * @return The manager that is specified by this builder.
     */
    protected abstract @NonNull AbstractOfficeWorkerPool build();

    /**
     * Specifies that the manager created by this builder will then set the unique instance of the
     * {@link InstalledOfficeManagerHolder} class. Note that if the {@code
     * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the
     * owner of this existing manager is responsible to stopped it.
     *
     * @return This builder instance.
     */
    public @NonNull B install() {

      this.install = true;
      return (B) this;
    }

    /**
     * Specifies the directory where temporary files and directories are created.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public @NonNull B workingDir(final @Nullable File workingDir) {

      if (workingDir != null) {
        this.workingDir = workingDir;
      }
      return (B) this;
    }

    /**
     * Specifies the directory where temporary files and directories are created.
     *
     * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
     * java.io.tmpdir</code> system property.
     *
     * @param workingDir The new working directory to set.
     * @return This builder instance.
     */
    public @NonNull B workingDir(final @Nullable String workingDir) {

      return StringUtils.isBlank(workingDir) ? (B) this : workingDir(new File(workingDir));
    }

    /**
     * Specifies the maximum time allowed to execute a task, counted from the moment a worker starts
     * executing it. When it expires, the task fails and its worker is restarted.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param taskExecutionTimeout The task execution timeout, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull B taskExecutionTimeout(final long taskExecutionTimeout) {
      AssertUtils.isTrue(
          taskExecutionTimeout >= 0,
          String.format(
              "taskExecutionTimeout %s must be greater than or equal to 0", taskExecutionTimeout));
      this.taskExecutionTimeout = taskExecutionTimeout;
      return (B) this;
    }

    /**
     * Specifies the maximum number of tasks waiting in the queue. A task submitted while the queue
     * is full fails at once.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 0 (no limit)
     *
     * @param taskQueueCapacity The task queue capacity; 0 means no limit.
     * @return This builder instance.
     */
    public @NonNull B taskQueueCapacity(final int taskQueueCapacity) {
      AssertUtils.isTrue(
          taskQueueCapacity >= 0,
          String.format(
              "taskQueueCapacity %s must be greater than or equal to 0", taskQueueCapacity));
      this.taskQueueCapacity = taskQueueCapacity;
      return (B) this;
    }

    /**
     * Specifies the maximum time a task waits in the queue, counted from the moment it is submitted
     * until a worker takes it. Waiting for a worker to be ready is part of it. When it expires, the
     * task fails without having been executed.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 30000 (30 seconds)
     *
     * @param taskQueueTimeout The task queue timeout, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull B taskQueueTimeout(final long taskQueueTimeout) {
      AssertUtils.isTrue(
          taskQueueTimeout >= 0,
          String.format(
              "taskQueueTimeout %s must be greater than or equal to 0", taskQueueTimeout));
      this.taskQueueTimeout = taskQueueTimeout;
      return (B) this;
    }

    /**
     * Installs the given manager as the unique instance of the {@link InstalledOfficeManagerHolder}
     * if {@link #install()} was called.
     *
     * @param manager The manager just built.
     * @param <M> The type of the manager.
     * @return The given manager.
     */
    protected <M extends AbstractOfficeWorkerPool> @NonNull M installed(final @NonNull M manager) {
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
    }
  }
}
