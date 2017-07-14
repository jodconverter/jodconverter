/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.office;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.DisposedException;

/**
 * A OfficeProcessManager is responsible to manage an office process and the connection (bridge) to
 * this office process.
 *
 * @see OfficeProcess
 * @see OfficeConnection
 */
class OfficeProcessManager {

  private static final Logger logger = LoggerFactory.getLogger(OfficeProcessManager.class);

  private final OfficeProcess process;
  private final OfficeConnection connection;
  private final ExecutorService executor;
  private final OfficeProcessManagerConfig config;

  /**
   * Creates a new manager with the specified configuration.
   *
   * @param officeUrl The URL for which the manager is created.
   * @param config The configuration of the manager.
   */
  public OfficeProcessManager(final OfficeUrl officeUrl, final OfficeProcessManagerConfig config) {

    this.config = config;
    process = new OfficeProcess(officeUrl, config);
    connection = new OfficeConnection(officeUrl);
    executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OfficeProcessThread"));
  }

  /**
   * Ensures that the process exited.
   *
   * @throws OfficeException If an exception occurs.
   */
  private void doEnsureProcessExited() throws OfficeException {

    try {
      final int exitCode =
          process.getExitCode(config.getProcessRetryInterval(), config.getProcessTimeout());
      logger.info("process exited with code {}", exitCode);

    } catch (RetryTimeoutException retryTimeoutEx) {

      logger.debug("doEnsureProcessExited times out", retryTimeoutEx);
      doTerminateProcess();

    } finally {
      process.deleteProfileDir();
    }
  }

  /** Starts the office process managed by this class and connect to the process. */
  private void doStartProcessAndConnect() throws OfficeException {

    try {
      process.start();
      new ConnectRetryable(process, connection)
          .execute(config.getProcessRetryInterval(), config.getProcessTimeout());

    } catch (Exception ex) {
      throw new OfficeException("Could not establish connection", ex);
    }
  }

  /** Stops the office process managed by this OfficeProcessManager. */
  private void doStopProcess() throws OfficeException {

    try {
      final boolean terminated = connection.getDesktop().terminate();

      // Once more: try to terminate
      logger.debug(
          "The Office Process {}",
          (terminated
              ? "has been terminated"
              : "is still running. Someone else prevents termination, e.g. the quickstarter"));

    } catch (DisposedException disposedEx) {
      // expected so ignore it
      logger.debug("Expected DisposedException catched and ignored in doStopProcess", disposedEx);

    } catch (Exception ex) {
      logger.debug("Exception catched in doStopProcess", ex);

      // in case we can't get hold of the desktop
      doTerminateProcess();

    } finally {
      doEnsureProcessExited();
    }
  }

  /**
   * Ensures that the process exited.
   *
   * @throws OfficeException If an exception occurs.
   */
  private void doTerminateProcess() throws OfficeException {

    try {
      final int exitCode =
          process.forciblyTerminate(config.getProcessRetryInterval(), config.getProcessTimeout());
      logger.info("process forcibly terminated with code {}", exitCode);

    } catch (Exception ex) {
      throw new OfficeException("Could not terminate process", ex);
    }
  }

  // Executes the specified task without waiting for the completion of the task
  private void execute(final String taskName, final Runnable task) {

    logger.info("Executing task '{}'...", taskName);
    executor.execute(task);
  }

  /**
   * Gets the connection of this manager.
   *
   * @return The {@link OfficeConnection} of this manager.
   */
  OfficeConnection getConnection() {
    return connection;
  }

  /**
   * Gets whether the connection to the office instance is opened.
   *
   * @return {@code true} is the connection is alive, {@code false} otherwise.
   */
  public boolean isConnected() {
    return connection.isConnected();
  }

  /**
   * Restarts an office process and wait until we are connected to the restarted process.
   *
   * @throws OfficeException If we are not able to restart the office process.
   */
  public void restartAndWait() throws OfficeException {

    // Create the restart task to be execute
    final Callable<Void> restartTask =
        new Callable<Void>() {

          @Override
          public Void call() throws Exception {
            doStopProcess();
            doStartProcessAndConnect();

            return null;
          }
        };

    // Submit the task to the executor and wait
    submitAndWait("Restart", restartTask);
  }

  /** Restarts the office process when the connection is lost. */
  public void restartDueToLostConnection() {

    // Create the restart task to be execute
    final Runnable restartTask =
        new Runnable() {

          @Override
          public void run() {

            try {
              doEnsureProcessExited();
              doStartProcessAndConnect();

            } catch (OfficeException officeEx) {
              logger.error("Could not restart process", officeEx);
            }
          }
        };

    // Execute the task
    execute("Restart After Lost Connection", restartTask);
  }

  /** Restarts the office process when there is a timeout while executing a task. */
  public void restartDueToTaskTimeout() {

    // Create the restart task to be execute
    final Runnable restartTask =
        new Runnable() {

          @Override
          public void run() {

            try {
              doTerminateProcess();
              // will cause unexpected disconnection and subsequent restart

            } catch (OfficeException officeException) {
              logger.error("Could not restart process", officeException);
            }
          }
        };

    // Execute the task
    execute("Restart After Timeout", restartTask);
  }

  /**
   * Starts an office process and wait until we are connected to the running process.
   *
   * @throws OfficeException If we are not able to start and connect to the office process.
   */
  public void startAndWait() throws OfficeException {

    // Create the start task to be execute
    final Callable<Void> startTask =
        new Callable<Void>() {

          @Override
          public Void call() throws Exception {
            doStartProcessAndConnect();

            return null;
          }
        };

    // Submit the task to the executor and wait
    submitAndWait("Start", startTask);
  }

  /**
   * Stop an office process and wait until the process is stopped.
   *
   * @throws OfficeException If we are not able to stop the office process.
   */
  public void stopAndWait() throws OfficeException {

    // Create the stop task to be execute
    final Callable<Void> stopTask =
        new Callable<Void>() {

          @Override
          public Void call() throws Exception {
            doStopProcess();

            return null;
          }
        };

    // Submit the task to the executor and wait
    submitAndWait("Stop", stopTask);
  }

  // Submits the specified task to the executor and waits for its completion
  private void submitAndWait(final String taskName, final Callable<Void> task)
      throws OfficeException {

    logger.info("Submitting task '{}' and waiting...", taskName);
    final Future<Void> future = executor.submit(task);

    // Wait for completion of the restart task
    try {
      future.get();
      logger.debug("Task '{}' executed successfully", taskName);

    } catch (ExecutionException executionEx) {
      logger.debug("ExecutionException catched in submitAndWait", executionEx);

      // Rethrow the original (cause) exception
      if (executionEx.getCause() instanceof OfficeException) {
        throw (OfficeException) executionEx.getCause();
      }
      throw new OfficeException(
          "Failed to execute task '" + taskName + "'", executionEx.getCause());

    } catch (InterruptedException interruptedEx) {
      Thread.currentThread().interrupt(); // ignore/reset
    }
  }
}
