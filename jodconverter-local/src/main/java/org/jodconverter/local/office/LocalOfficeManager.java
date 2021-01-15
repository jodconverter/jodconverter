/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.local.office;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractOfficeManagerPool;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.process.ProcessManager;

/**
 * Default {@link org.jodconverter.core.office.OfficeManager} implementation that uses a pool of
 * office processes to execute conversion tasks.
 */
public final class LocalOfficeManager
    extends AbstractOfficeManagerPool<LocalOfficeManagerPoolEntry> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeManager.class);

  // The default value for hostName.
  public static final String DEFAULT_HOSTNAME = "127.0.0.1";
  // The default timeout when executing a process call (start/terminate).
  public static final long DEFAULT_PROCESS_TIMEOUT = 120_000L; // 2 minutes
  // The default delay between each try when executing a process call (start/terminate).
  public static final long DEFAULT_PROCESS_RETRY_INTERVAL = 250L; // 0.25 secs.
  // The default delay after an attempt to start an office process before doing anything else.
  public static final long DEFAULT_AFTER_START_PROCESS_DELAY = 0L; // No delay.
  // The default behavior when we want to start an office process and a process with
  // the same URL already exists.
  public static final ExistingProcessAction DEFAULT_EXISTING_PROCESS_ACTION =
      ExistingProcessAction.KILL;
  // The default "fail fast" behavior when an office process is started.
  public static final boolean DEFAULT_START_FAIL_FAST = false;
  // The default behavior when an office process is started regarding to OpenGL usage.
  public static final boolean DEFAULT_DISABLE_OPENGL = false;
  // The default "keep process alive" behavior on shutdown.
  public static final boolean DEFAULT_KEEP_ALIVE_ON_SHUTDOWN = false;
  // The default maximum number of tasks an office process can execute before restarting.
  public static final int DEFAULT_MAX_TASKS_PER_PROCESS = 200;
  // The minimum value for the delay between each try when executing a process call
  // (start/terminate).
  public static final long MIN_PROCESS_RETRY_INTERVAL = 0L; // No delay.
  // The maximum value for the delay between each try when executing a process call
  // (start/terminate).
  public static final long MAX_PROCESS_RETRY_INTERVAL = 10_000L; // 10 sec.
  // The minimum value for the delay after a start process attempt.
  public static final long MIN_AFTER_START_PROCESS_DELAY = 0L; // No delay.
  // The maximum value for the delay after a start process attempt.
  public static final long MAX_AFTER_START_PROCESS_DELAY = 10_000L; // 10 sec.

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link LocalOfficeManager} with default configuration.
   *
   * @return A {@link LocalOfficeManager} with default configuration.
   */
  public static @NonNull LocalOfficeManager make() {
    return builder().build();
  }

  /**
   * Creates a new {@link LocalOfficeManager} with default configuration. The created manager will
   * then be the unique instance of the {@link
   * org.jodconverter.core.office.InstalledOfficeManagerHolder} class. Note that if the {@code
   * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the owner
   * of this existing manager is responsible to stopped it.
   *
   * @return A {@link LocalOfficeManager} with default configuration.
   */
  public static @NonNull LocalOfficeManager install() {
    return builder().install().build();
  }

  private LocalOfficeManager(
      final List<OfficeUrl> officeUrls,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager,
      final List<String> runAsArgs,
      final File templateProfileDir,
      final long processTimeout,
      final long processRetryInterval,
      final long afterStartProcessDelay,
      final ExistingProcessAction existingProcessAction,
      final boolean startFailFast,
      final boolean keepAliveOnShutdown,
      final boolean disableOpengl,
      final int maxTasksPerProcess,
      final long taskExecutionTimeout,
      final long taskQueueTimeout) {
    super(officeUrls.size(), workingDir, taskQueueTimeout);

    setEntries(
        officeUrls.stream()
            .map(
                officeUrl ->
                    new LocalOfficeManagerPoolEntry(
                        maxTasksPerProcess,
                        taskExecutionTimeout,
                        new LocalOfficeProcessManager(
                            officeUrl,
                            officeHome,
                            workingDir,
                            processManager,
                            runAsArgs,
                            templateProfileDir,
                            processTimeout,
                            processRetryInterval,
                            afterStartProcessDelay,
                            existingProcessAction,
                            startFailFast,
                            keepAliveOnShutdown,
                            disableOpengl,
                            new OfficeConnection(officeUrl))))
            .collect(Collectors.toList()));
  }

  /**
   * A builder for constructing a {@link LocalOfficeManager}.
   *
   * @see LocalOfficeManager
   */
  public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

    private List<String> pipeNames;
    private String hostName = DEFAULT_HOSTNAME;
    private List<Integer> portNumbers;
    private File officeHome = LocalOfficeUtils.getDefaultOfficeHome();
    private ProcessManager processManager = LocalOfficeUtils.findBestProcessManager();
    private List<String> runAsArgs = Collections.emptyList();
    private File templateProfileDir;
    private boolean useDefaultOnInvalidTemplateProfileDir;
    private long processTimeout = DEFAULT_PROCESS_TIMEOUT;
    private long processRetryInterval = DEFAULT_PROCESS_RETRY_INTERVAL;
    private long afterStartProcessDelay = DEFAULT_AFTER_START_PROCESS_DELAY;
    private ExistingProcessAction existingProcessAction = DEFAULT_EXISTING_PROCESS_ACTION;
    private boolean startFailFast = DEFAULT_START_FAIL_FAST;
    private boolean keepAliveOnShutdown = DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;
    private boolean disableOpengl = DEFAULT_DISABLE_OPENGL;
    private int maxTasksPerProcess = DEFAULT_MAX_TASKS_PER_PROCESS;

    // Private constructor so only LocalOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public @NonNull LocalOfficeManager build() {

      // Validate the directories we are working with
      OfficeUtils.validateWorkingDir(workingDir);
      LocalOfficeUtils.validateOfficeHome(officeHome);
      if (useDefaultOnInvalidTemplateProfileDir) {
        try {
          LocalOfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);
        } catch (IllegalStateException ex) {
          // Use default
          templateProfileDir = null;
          LOGGER.warn("Falling back to default templateProfileDir. Cause: {}", ex.getMessage());
        }
      } else {
        LocalOfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);
      }

      // Build the office URLs
      final LocalOfficeManager manager =
          new LocalOfficeManager(
              LocalOfficeUtils.buildOfficeUrls(hostName, portNumbers, pipeNames),
              officeHome,
              workingDir,
              processManager,
              runAsArgs,
              templateProfileDir,
              processTimeout,
              processRetryInterval,
              afterStartProcessDelay,
              existingProcessAction,
              startFailFast,
              keepAliveOnShutdown,
              disableOpengl,
              maxTasksPerProcess,
              taskExecutionTimeout,
              taskQueueTimeout);
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
    }

    /**
     * Specifies the pipe names that will be use to communicate with office. An instance of office
     * will be launched for each pipe name.
     *
     * @param pipeNames The pipe names to use.
     * @return This builder instance.
     */
    public @NonNull Builder pipeNames(final @Nullable String... pipeNames) {

      if (pipeNames != null && pipeNames.length != 0) {
        this.pipeNames = Arrays.asList(pipeNames);
      }
      return this;
    }

    /**
     * Specifies host name that will be use in the --accept argument when starting an office
     * process. Most of the time, the default will work. But if it doesn't work (unable to connect
     * to the started process), using {@code localhost} instead may work.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 127.0.0.1
     *
     * @param hostName The host name to use.
     * @return This builder instance.
     */
    public @NonNull Builder hostName(final String hostName) {
      this.hostName = hostName;
      return this;
    }

    /**
     * Specifies the port numbers that will be use to communicate with office. An instance of office
     * will be launched for each port number.
     *
     * @param portNumbers The port numbers to use.
     * @return This builder instance.
     */
    public @NonNull Builder portNumbers(final int... portNumbers) {

      if (portNumbers != null && portNumbers.length != 0) {
        this.portNumbers = Arrays.stream(portNumbers).boxed().collect(Collectors.toList());
      }
      return this;
    }

    /**
     * Specifies the office home directory (office installation).
     *
     * @param officeHome The new home directory to set.
     * @return This builder instance.
     */
    public @NonNull Builder officeHome(final @Nullable File officeHome) {

      if (officeHome != null) {
        this.officeHome = officeHome;
      }
      return this;
    }

    /**
     * Specifies the office home directory (office installation).
     *
     * @param officeHome The new home directory to set.
     * @return This builder instance.
     */
    public @NonNull Builder officeHome(final @Nullable String officeHome) {

      return StringUtils.isBlank(officeHome) ? this : officeHome(new File(officeHome));
    }

    /**
     * Provides a specific {@link ProcessManager} implementation to be used when dealing with an
     * office process (retrieve PID, kill process).
     *
     * @param processManager The provided process manager.
     * @return This builder instance.
     */
    public @NonNull Builder processManager(final @Nullable ProcessManager processManager) {

      if (processManager != null) {
        this.processManager = processManager;
      }
      return this;
    }

    /**
     * Provides a custom {@link ProcessManager} implementation, which may not be included in the
     * standard JODConverter distribution.
     *
     * @param processManagerClass Type of the provided process manager. The class must implement the
     *     {@code ProcessManager} interface, must be on the classpath (or more specifically
     *     accessible from the current classloader) and must have a default public constructor (no
     *     argument).
     * @return This builder instance.
     * @see org.jodconverter.local.process.ProcessManager
     * @see org.jodconverter.local.process.AbstractProcessManager
     */
    public @NonNull Builder processManager(final @Nullable String processManagerClass) {

      try {
        return StringUtils.isBlank(processManagerClass)
            ? this
            : processManager((ProcessManager) Class.forName(processManagerClass).newInstance());
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
        throw new IllegalArgumentException(
            "Could not create a process manager from the specified class name: "
                + processManagerClass,
            ex);
      }
    }

    /**
     * Specifies the sudo arguments that will be used with unix commands.
     *
     * @param runAsArgs The sudo arguments for a unix os.
     * @return This builder instance.
     */
    public @NonNull Builder runAsArgs(final @Nullable String... runAsArgs) {

      if (runAsArgs != null && runAsArgs.length != 0) {
        this.runAsArgs = Collections.unmodifiableList(Arrays.asList(runAsArgs));
      }
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    public @NonNull Builder templateProfileDir(final @Nullable File templateProfileDir) {

      if (templateProfileDir != null) {
        this.templateProfileDir = templateProfileDir;
      }
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    public @NonNull Builder templateProfileDir(final @Nullable String templateProfileDir) {

      return StringUtils.isBlank(templateProfileDir)
          ? this
          : templateProfileDir(new File(templateProfileDir));
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created. If
     * the given templateProfileDir is not valid, it will be ignored and the default behavior will
     * be applied.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    public @NonNull Builder templateProfileDirOrDefault(final @Nullable File templateProfileDir) {

      if (templateProfileDir != null) {
        this.useDefaultOnInvalidTemplateProfileDir = true;
        this.templateProfileDir = templateProfileDir;
      }
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created. If
     * the given templateProfileDir is not valid, it will be ignored and the default behavior will
     * be applied.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    public @NonNull Builder templateProfileDirOrDefault(final @Nullable String templateProfileDir) {

      return StringUtils.isBlank(templateProfileDir)
          ? this
          : templateProfileDirOrDefault(new File(templateProfileDir));
    }

    /**
     * Specifies the timeout, in milliseconds, when trying to execute an office process call
     * (start/terminate).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param processTimeout The process timeout, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull Builder processTimeout(final @Nullable Long processTimeout) {

      if (processTimeout != null) {
        AssertUtils.isTrue(
            processTimeout >= 0,
            String.format("processTimeout %s must be greater than or equal to 0", processTimeout));
        this.processTimeout = processTimeout;
      }
      return this;
    }

    /**
     * Specifies the delay, in milliseconds, between each try when trying to execute an office
     * process call (start/terminate).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
     *
     * @param processRetryInterval The retry interval, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull Builder processRetryInterval(final @Nullable Long processRetryInterval) {

      if (processRetryInterval != null) {
        AssertUtils.isTrue(
            processRetryInterval >= MIN_PROCESS_RETRY_INTERVAL
                && processRetryInterval <= MAX_PROCESS_RETRY_INTERVAL,
            String.format(
                "processRetryInterval %s must be in the inclusive range of %s to %s",
                processRetryInterval, MIN_PROCESS_RETRY_INTERVAL, MAX_PROCESS_RETRY_INTERVAL));
        this.processRetryInterval = processRetryInterval;
      }
      return this;
    }

    /**
     * Specifies the delay, in milliseconds, after an attempt to start an office process before
     * doing anything else. It is required on some OS to avoid an attempt to connect to the started
     * process that will hang for more than 5 minutes before throwing a timeout exception, we do not
     * know why.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 0 (no delay). On FreeBSD, which is a known OS needing this,
     * it default to 2000 (2 seconds).
     *
     * @param afterStartProcessDelay The delay, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull Builder afterStartProcessDelay(final @Nullable Long afterStartProcessDelay) {

      if (afterStartProcessDelay != null) {
        AssertUtils.isTrue(
            processRetryInterval >= MIN_AFTER_START_PROCESS_DELAY
                && processRetryInterval <= MAX_AFTER_START_PROCESS_DELAY,
            String.format(
                "afterStartProcessDelay %s must be in the inclusive range of %s to %s",
                afterStartProcessDelay,
                MIN_AFTER_START_PROCESS_DELAY,
                MAX_AFTER_START_PROCESS_DELAY));
        this.afterStartProcessDelay = afterStartProcessDelay;
      }
      return this;
    }

    /**
     * Specifies the action that must be taken when starting a new office process and there already
     * is a existing running process for the same connection string.
     *
     * <p>&nbsp; <b><i>Default</i></b>: ExistingProcessAction.KILL
     *
     * @param existingProcessAction The existing process action.
     * @return This builder instance.
     */
    public @NonNull Builder existingProcessAction(
        final @Nullable ExistingProcessAction existingProcessAction) {

      if (existingProcessAction != null) {
        this.existingProcessAction = existingProcessAction;
      }
      return this;
    }

    /**
     * Controls whether the manager will "fail fast" if an office process cannot be started or the
     * connection to the started process fails. If set to {@code true}, the start of a process will
     * wait for the task to be completed, and will throw an exception if the office process is not
     * started successfully or if the connection to the started process fails. If set to {@code
     * false}, the task of starting the process and connecting to it will be submitted and will
     * return immediately, meaning a faster starting process. Only error logs will be produced if
     * anything goes wrong.
     *
     * <p>&nbsp; <b><i>Default</i></b>: false
     *
     * @param startFailFast {@code true} to "fail fast", {@code false} otherwise.
     * @return This builder instance.
     */
    public @NonNull Builder startFailFast(final @Nullable Boolean startFailFast) {

      if (startFailFast != null) {
        this.startFailFast = startFailFast;
      }
      return this;
    }

    /**
     * Controls whether the manager will keep the office process alive on shutdown. If set to {@code
     * true}, the stop task will only disconnect from the office process, which will stay alive. If
     * set to {@code false}, the office process will be stopped gracefully (or killed if could not
     * been stopped gracefully).
     *
     * <p>&nbsp; <b><i>Default</i></b>: false
     *
     * @param keepAliveOnShutdown {@code true} to keep the process alive, {@code false} otherwise.
     * @return This builder instance.
     */
    public @NonNull Builder keepAliveOnShutdown(final @Nullable Boolean keepAliveOnShutdown) {

      if (keepAliveOnShutdown != null) {
        this.keepAliveOnShutdown = keepAliveOnShutdown;
      }
      return this;
    }

    /**
     * Specifies whether OpenGL must be disabled when starting a new office process. Nothing will be
     * done if OpenGL is already disabled according to the user profile used with the office
     * process. If the options is changed, then office will be restarted.
     *
     * <p>&nbsp; <b><i>Default</i></b>: false
     *
     * @param disableOpengl {@code true} to disable OpenGL, {@code false} otherwise.
     * @return This builder instance.
     */
    public @NonNull Builder disableOpengl(final @Nullable Boolean disableOpengl) {

      if (disableOpengl != null) {
        this.disableOpengl = disableOpengl;
      }
      return this;
    }

    /**
     * Specifies the maximum number of tasks an office process can execute before restarting. 0
     * means infinite number of task (will never restart).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 200
     *
     * @param maxTasksPerProcess The new maximum number of tasks an office process can execute.
     * @return This builder instance.
     */
    public @NonNull Builder maxTasksPerProcess(final @Nullable Integer maxTasksPerProcess) {

      if (maxTasksPerProcess != null) {
        AssertUtils.isTrue(
            maxTasksPerProcess >= 0,
            String.format(
                "maxTasksPerProcess %s must be greater than or equal to 0", maxTasksPerProcess));
        this.maxTasksPerProcess = maxTasksPerProcess;
      }
      return this;
    }
  }
}
