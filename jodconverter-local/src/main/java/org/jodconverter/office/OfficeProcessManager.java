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

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeProcessManager.class);

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
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted. We don't always want to delete the instance profile directory on restart since it
   *     may be an expensive operation.
   * @throws OfficeException If an exception occurs.
   */
  private void doEnsureProcessExited(final boolean deleteInstanceProfileDir)
      throws OfficeException {

    try {
      final int exitCode =
          process.getExitCode(config.getProcessRetryInterval(), config.getProcessTimeout());
      LOGGER.info("process exited with code {}", exitCode);

    } catch (RetryTimeoutException retryTimeoutEx) {

      LOGGER.debug("doEnsureProcessExited times out", retryTimeoutEx);
      doTerminateProcess();

    } finally {
      if (deleteInstanceProfileDir) {
        process.deleteInstanceProfileDir();
      }
    }
  }

  /**
   * Starts the office process managed by this class and connect to the process.
   *
   * @param restart Indicates whether it is a fresh start or a restart. A restart will assume that
   *     the instance profile directory is already created. To recreate the instance profile
   *     directory, {@code restart} should be set to {@code false}.
   */
  private void doStartProcessAndConnect(final boolean restart) throws OfficeException {

    process.start(restart);

    try {
      new ConnectRetryable(process, connection)
          .execute(config.getProcessRetryInterval(), config.getProcessTimeout());

    } catch (Exception ex) {
      if (ex instanceof OfficeException) {
        throw (OfficeException) ex;
      }
      throw new OfficeException("Could not establish connection", ex);
    }
  }

  /**
   * Stops the office process managed by this OfficeProcessManager.
   *
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted. We don't always want to delete the instance profile directory on restart since it
   *     may be an expensive operation.
   */
  private void doStopProcess(final boolean deleteInstanceProfileDir) throws OfficeException {

    try {
      final boolean terminated = connection.getDesktop().terminate();

      // Once more: try to terminate
      LOGGER.debug(
          "The Office Process {}",
          terminated
              ? "has been terminated"
              : "is still running. Someone else prevents termination, e.g. the quickstarter");

    } catch (DisposedException disposedEx) {
      // expected so ignore it
      LOGGER.debug("Expected DisposedException catched and ignored in doStopProcess", disposedEx);

    } catch (Exception ex) {
      LOGGER.debug("Exception catched in doStopProcess", ex);

      // in case we can't get hold of the desktop
      doTerminateProcess();

    } finally {
      doEnsureProcessExited(deleteInstanceProfileDir);
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
      LOGGER.info("process forcibly terminated with code {}", exitCode);

    } catch (Exception ex) {
      throw new OfficeException("Could not terminate process", ex);
    }
  }

  /**
   * Gets the connection of this manager.
   *
   * @return The {@link OfficeConnection} of this manager.
   */
  /* default */ OfficeConnection getConnection() {
    return connection;
  }

  /**
   * Restarts an office process and wait until we are connected to the restarted process.
   *
   * @throws OfficeException If we are not able to restart the office process.
   */
  public void restartAndWait() throws OfficeException {

    // Submit a restart task to the executor and wait
    submitAndWait(
        "Restart",
        () -> {
          // On clean restart, we won't delete the instance profile directory,
          // causing a faster start of an office process.
          doStopProcess(false);
          doStartProcessAndConnect(true);
          return null;
        });
  }

  /** Restarts the office process when the connection is lost. */
  public void restartDueToLostConnection() {

    // Execute the task
    LOGGER.info("Executing task 'Restart After Lost Connection'...");
    executor.execute(
        () -> {
          try {
            // Since we have lost the connection, it could mean that
            // the office process has crashed. Thus, we want a clean
            // instance profile directory on restart.
            doEnsureProcessExited(true);
            doStartProcessAndConnect(false);
          } catch (OfficeException officeEx) {
            LOGGER.error("Could not restart process after connection lost.", officeEx);
          }
        });
  }

  /** Restarts the office process when there is a timeout while executing a task. */
  public void restartDueToTaskTimeout() {

    // Execute the restart task
    LOGGER.info("Executing task 'Restart After Timeout'...");
    executor.execute(
        () -> {
          try {
            // This will cause unexpected disconnection and subsequent restart.
            doTerminateProcess();
          } catch (OfficeException officeException) {
            LOGGER.error("Could not terminate process after task timeout.", officeException);
          }
        });
  }

  /**
   * Starts an office process and wait until we are connected to the running process.
   *
   * @throws OfficeException If we are not able to start and connect to the office process.
   */
  public void startAndWait() throws OfficeException {

    // Submit a start task to the executor and wait
    submitAndWait(
        "Start",
        () -> {
          doStartProcessAndConnect(false);
          return null;
        });
  }

  /**
   * Stop an office process and wait until the process is stopped.
   *
   * @throws OfficeException If we are not able to stop the office process.
   */
  public void stopAndWait() throws OfficeException {

    // Submit a stop task to the executor and wait
    submitAndWait(
        "Stop",
        () -> {
          doStopProcess(true);
          return null;
        });
  }

  // Submits the specified task to the executor and waits for its completion
  private void submitAndWait(final String taskName, final Callable<Void> task)
      throws OfficeException {

    LOGGER.info("Submitting task '{}' and waiting...", taskName);
    final Future<Void> future = executor.submit(task);

    // Wait for completion of the restart task
    try {
      future.get();
      LOGGER.debug("Task '{}' executed successfully", taskName);

    } catch (ExecutionException executionEx) {
      LOGGER.debug("ExecutionException catched in submitAndWait", executionEx);

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
