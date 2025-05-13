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

package org.jodconverter.local.office;

import static org.jodconverter.local.process.ProcessManager.PID_NOT_FOUND;
import static org.jodconverter.local.process.ProcessManager.PID_UNKNOWN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.DisposedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.NamedThreadFactory;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.RetryTimeoutException;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.ProcessQuery;

/**
 * An {@link LocalOfficeProcessManager} is responsible to manage an office process and the
 * connection (bridge) to this office process.
 *
 * @see OfficeConnection
 */
class LocalOfficeProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeProcessManager.class);

  private VerboseProcess process;
  private long pid = PID_UNKNOWN;
  private OfficeDescriptor descriptor;
  private final OfficeConnection connection;
  private final ExecutorService executor;
  private final File instanceProfileDir;
  private final OfficeUrl officeUrl;
  private final File officeHome;
  private final ProcessManager processManager;
  private final List<String> runAsArgs;
  private final File templateProfileDir;
  private final long processTimeout;
  private final long processRetryInterval;
  private final long afterStartProcessDelay;
  private final ExistingProcessAction existingProcessAction;
  private final boolean startFailFast;
  private final boolean keepAliveOnShutdown;

  /**
   * Creates a new manager with the specified configuration.
   *
   * @param officeUrl The URL for which the office process is created.
   * @param officeHome The home directory of the office installation.
   * @param workingDir The working directory to set to the office process.
   * @param processManager The process manager to use to deal with the office process.
   * @param runAsArgs The sudo arguments that will be used with unix commands.
   * @param templateProfileDir The directory to copy to the temporary office profile directories to
   *     be created.
   * @param processTimeout The timeout, in milliseconds, when trying to execute an office process
   *     call (start/terminate).
   * @param processRetryInterval The delay, in milliseconds, between each try when trying to execute
   *     an office process call (start/terminate).
   * @param afterStartProcessDelay The delay, in milliseconds, after the start of an office process
   *     before doing anything else.
   * @param existingProcessAction Represents the action to take when starting a new office process,
   *     and there already is a process running with the same connection string.
   * @param startFailFast Controls whether the manager will "fail fast" if the office process cannot
   *     be started. If set to {@code true}, the {@link #start()} operation will wait for the task
   *     to be completed, and will throw an exception if the office process is not started
   *     successfully. If set to {@code false}, the {@link #start()} operation will submit the task
   *     and return immediately, meaning a faster operation.
   * @param keepAliveOnShutdown Controls whether the manager will keep the office process alive on
   *     shutdown. If set to {@code true}, the {@link #stop()} will only disconnect from the office
   *     process, which will stay alive. If set to {@code false}, the office process will be stopped
   *     gracefully (or killed if it could not be stopped gracefully).
   * @param connection The object that will manage the connection to the office process.
   */
  /* default */ LocalOfficeProcessManager(
      final OfficeUrl officeUrl,
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
      final OfficeConnection connection) {

    this.officeUrl = officeUrl;
    this.officeHome = officeHome;
    this.processManager = processManager;
    this.runAsArgs = runAsArgs;
    this.templateProfileDir = templateProfileDir;
    this.processTimeout = processTimeout;
    this.processRetryInterval = processRetryInterval;
    this.afterStartProcessDelay = afterStartProcessDelay;
    this.existingProcessAction = existingProcessAction;
    this.startFailFast = startFailFast;
    this.keepAliveOnShutdown = keepAliveOnShutdown;
    this.connection = connection;

    executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("jodconverter-offprocmng"));
    instanceProfileDir =
        new File(
            workingDir,
            ".jodconverter_" + officeUrl.getConnectString().replace(',', '_').replace('=', '-'));
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
   * Starts an office process and connect to the running process.
   *
   * <p>If {@link #startFailFast} is set to {@code true}, the operation will wait for the task to be
   * completed, and will throw an exception if the office process is not started successfully or
   * that we cannot connect to the started process. If set to {@code false}, the operation will
   * submit the task and return immediately, meaning a faster operation.
   *
   * @throws OfficeException If the office process cannot be started, or we are unable to connect to
   *     the started process.
   */
  /* default */ void start() throws OfficeException {

    if (startFailFast) {
      // Submit the start task to the executor.
      LOGGER.debug("Submitting start task...");
      final Future<Void> future = executor.submit(() -> startProcessAndConnect(false));

      // Wait for completion of the task.
      try {
        LOGGER.debug("Waiting for start task to complete...");
        future.get();
        LOGGER.debug("Start task executed successfully.");

      } catch (ExecutionException ex) {

        // An error occurred while executing the start and connect task...
        throw handleStartTaskExecutionException(ex);

      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt(); // ignore/reset
        throw new OfficeException("Interruption while starting the office process.", ex);
      }
    } else {
      // Submit a start task to the executor and return immediately.
      executor.execute(
          () -> {
            try {
              startProcessAndConnect(false);
            } catch (OfficeException ex) {
              LOGGER.error("Could not start the office process.", ex);
            }
          });
    }
  }

  private OfficeException handleStartTaskExecutionException(
      final ExecutionException executionException) {

    // Rethrow the original (cause) exception
    if (executionException.getCause() instanceof OfficeException) {
      return (OfficeException) executionException.getCause();
    }

    return new OfficeException("Start task did not complete", executionException.getCause());
  }

  /**
   * Restarts an office process.
   *
   * <p>The task of restarting the process and connecting to it is executed by a single thread
   * {@link ExecutorService} and thus, the current {@code restart()} function returns immediately.
   * The restart will be done as soon as the executor is available to execute the task.
   */
  /* default */ void restart() {
    LOGGER.info("Restarting...");

    executor.execute(
        () -> {
          // On clean restart, we won't delete the instance profile directory,
          // causing a faster start of an office process.
          stopProcess(false);
          try {
            startProcessAndConnect(true);
          } catch (OfficeException ex) {
            LOGGER.error("Could not restart the office process.", ex);
          }
        });
  }

  /**
   * Restarts the office process when the connection is lost.
   *
   * <p>The task of restarting the process and connecting to it is executed by a single thread
   * {@link ExecutorService} and thus, the current {@code restartDueToLostConnection()} function
   * returns immediately.
   */
  /* default */ void restartDueToLostConnection() {
    LOGGER.info("Restarting due to lost connection...");

    executor.execute(
        () -> {
          LOGGER.debug("Connection lost unexpectedly");

          // Since we have lost the connection unexpectedly, it could mean that
          // the office process has crashed. Thus, we want a clean instance profile
          // directory on restart.
          ensureProcessExited(true);
          try {
            startProcessAndConnect(false);
          } catch (OfficeException ex) {
            LOGGER.error(
                "Could not restart the office process after an unexpected lost connection.", ex);
          }
        });
  }

  /**
   * Restarts the office process when there is a timeout while executing a task.
   *
   * <p>The function will only forcibly kill the office process, causing an unexpected disconnection
   * and later restart.
   *
   * @see LocalOfficeManagerPoolEntry
   */
  /* default */ void restartDueToTaskTimeout() {
    LOGGER.info("Restarting due to task timeout...");

    // This will cause unexpected disconnection and later restart.
    forciblyTerminateProcess();
  }

  /**
   * Stops an office process and waits until the process is stopped.
   *
   * @throws OfficeException If we are not able to stop the office process.
   */
  /* default */ void stop() throws OfficeException {

    // Submit a task to stop the office process and wait task termination.
    // This is required if we don't want to let garbage on disk since the
    // stopProcess must be fully executed to clean the temp files and
    // directories.
    LOGGER.debug("Submitting stop task...");

    // If we must keep the process alive, just disconnect.
    if (keepAliveOnShutdown) {
      // We must disconnect from the process
      executor.execute(connection::disconnect);
    } else {
      // We must stop the process.
      executor.execute(() -> stopProcess(true));
    }

    // Shutdown the executor, no other task will be accepted.
    executor.shutdown();

    // Await for task termination. This is required if we don't want to let garbage on disk.
    try {
      // +1000L to allow the deletion of the templateProfileDir.
      // But is it really necessary? It is a wild guess...
      final long stopTimeout = processTimeout + 1000L;
      LOGGER.debug("Waiting for stop task to complete ({} millisecs)...", stopTimeout);
      if (executor.awaitTermination(stopTimeout, TimeUnit.MILLISECONDS)) {
        LOGGER.debug("Stop task executed successfully.");
      } else {
        // TODO: Should we do something special ?
        LOGGER.debug("Could not execute stop task within {} millisecs...", stopTimeout);
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new OfficeException("Interruption while stopping the office process.", ex);
    }
  }

  /**
   * Starts the office process managed by this manager and connect to the started process.
   *
   * <p>This function is always called into tasks that are executed by a single thread {@link
   * ExecutorService}.
   *
   * @param restart Indicates whether it is a fresh start or a restart. A restart will assume that
   *     the instance profile directory is already created. To recreate the instance profile
   *     directory, {@code restart} should be set to {@code false}.
   * @return {@code null}. So it could be used in a {@link java.util.concurrent.Callable}.
   * @throws OfficeException If the office process cannot be started, or we are unable to connect to
   *     the started process.
   */
  @SuppressWarnings("SameReturnValue")
  private Void startProcessAndConnect(final boolean restart) throws OfficeException {

    // Reinitialize pid and process.
    pid = PID_UNKNOWN;
    process = null;

    // Detect the office version if required.
    if (descriptor == null) {
      descriptor = detectOfficeDescriptor();
    }

    // Build the 'accept' argument (connection string).
    final String acceptString = officeUrl.getAcceptString();

    // Search for an existing process.
    final ProcessQuery processQuery = new ProcessQuery("soffice", acceptString);
    pid = checkForExistingProcess(processQuery);

    // If we already have a PID, it means that the process is already started and that
    // the configuration didn't tell us to kill the process.
    if (pid > PID_UNKNOWN) {
      return null;
    }

    // Prepare the instance directory only on first start
    if (!restart) {
      prepareInstanceProfileDir();
    }

    // Launch the office process and connect.
    executeStartProcessAndConnect(acceptString, processQuery);

    if (pid == PID_NOT_FOUND) {
      throw new OfficeException(
          String.format(
              "A process with --accept '%s' started but its pid could not be found", acceptString));
    }

    return null;
  }

  private void executeStartProcessAndConnect(
      final String acceptString, final ProcessQuery processQuery) throws OfficeException {

    // Create the builder used to launch the office process
    final ProcessBuilder processBuilder = prepareProcessBuilder(acceptString);

    LOGGER.debug("OFFICE HOME: {}", officeHome);
    LOGGER.info(
        "Starting process with --accept '{}' and profileDir '{}'",
        acceptString,
        instanceProfileDir);

    // Launch the process.
    try {
      // Start the process.
      final StartProcessAndConnectRetryable retryable =
          new StartProcessAndConnectRetryable(
              processManager, processBuilder, processQuery, afterStartProcessDelay, connection);
      try {
        retryable.execute(processRetryInterval, processTimeout);
      } finally {
        // We must keep these even on connection failure to be able to kill the process if
        // required.
        process = retryable.getProcess();
        pid = retryable.getProcessId();
      }

      LOGGER.info(
          "Started process; pid: {}",
          pid == PID_NOT_FOUND ? "PID_NOT_FOUND" : pid == PID_UNKNOWN ? "PID_UNKNOWN" : pid);

    } catch (Exception ex) {
      throw new OfficeException(
          String.format("An error prevents us to start a process with --accept '%s'", acceptString),
          ex);
    }
  }

  /**
   * Stops the office process managed by this manager.
   *
   * <p>This function is always called into tasks that are executed by a single thread {@link
   * ExecutorService} and thus, the function must manage its own exception handling.
   *
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted. We don't always want to delete the instance profile directory on restart since it
   *     may be an expensive operation.
   * @return {@code null}. So it could be used in a {@link java.util.concurrent.Callable}.
   */
  @SuppressWarnings("SameReturnValue")
  private Void stopProcess(final boolean deleteInstanceProfileDir) {
    LOGGER.debug(
        "Stopping the office process with deleteInstanceProfileDir set to {}...",
        deleteInstanceProfileDir);

    try {
      final XDesktop desktop = connection.getDesktop();
      if (desktop == null) {
        // We are not connected to the office process. We can still try to terminate it.
        forciblyTerminateProcess();
      } else {
        // Try to terminate
        final boolean terminated = connection.getDesktop().terminate();

        LOGGER.debug(
            "The office process {}",
            terminated
                ? "will be terminated shortly. A request has been sent to terminate the desktop."
                : "is still running. Someone else prevents termination, e.g. the quickstarter.");
      }

    } catch (DisposedException ex) {
      // Expected so ignore it
      LOGGER.debug("Expected DisposedException catch and ignored in stopProcess", ex);

    } finally {
      ensureProcessExited(deleteInstanceProfileDir);
    }

    return null;
  }

  private void killExistingProcess(final long pid, final ProcessQuery processQuery)
      throws IOException, OfficeException {

    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn(
          "A process with --accept '{}' is already running; pid {}; trying to kill it...",
          processQuery.getArgument(),
          pid);
    }
    processManager.kill(null, pid);
    // Wait a sec...
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    // Throw an exception if it still lives.
    if (processManager.findPid(processQuery) > PID_UNKNOWN) {
      throw new OfficeException(
          String.format(
              "A process with --accept '%s' is already running and could not be killed; pid %d",
              processQuery.getArgument(), pid));
    }
  }

  private void connectToExistingProcess(final long pid, final String accept)
      throws OfficeException {

    LOGGER.debug("Connecting to existing process with --accept '{}'; pid {}", accept, pid);
    try {
      new ConnectRetryable(connection).execute(processRetryInterval, processTimeout);
    } catch (RetryTimeoutException ex) {
      throw new OfficeException(
          String.format(
              "Could not establish connection to existing process with --accept '%s'; pid %d",
              accept, pid),
          ex);
    }
  }

  /**
   * Checks if there already is an office process that runs with the connection string we want to
   * use. The process will be killed if the kill switch is on.
   *
   * @param processQuery The query that connection string we want to use.
   * @return The process id of the process if the process is already running with the same
   *     connection string; {@link ProcessManager#PID_UNKNOWN} otherwise.
   * @throws OfficeException If the verification fails.
   */
  private long checkForExistingProcess(final ProcessQuery processQuery) throws OfficeException {

    final String accept = processQuery.getArgument();
    try {
      // Search for an existing process that would prevent us to start a new
      // office process with the same connection string.
      long pid = processManager.findPid(processQuery);

      if (pid <= PID_UNKNOWN) {
        // No process was found.
        LOGGER.debug(
            "Checking existing process done; no process running with --accept '{}'", accept);
        return pid;
      }

      // A process was found!
      switch (existingProcessAction) {
        case FAIL:
          // Throw an exception if the kill switch is off.
          throw new OfficeException(
              String.format(
                  "A process with --accept '%s' is already running; pid %d", accept, pid));
        case KILL:
          // Kill any running process with the same connection string if the kill switch is on.
          killExistingProcess(pid, processQuery);
          pid = PID_UNKNOWN;
          break;
        case CONNECT:
          // Connect to the existing office process.
          connectToExistingProcess(pid, accept);
          break;
        case CONNECT_OR_KILL:
          // Try to connect to the existing office process.
          try {
            connectToExistingProcess(pid, accept);
          } catch (OfficeException ex) {
            // Could not establish connection. Kill the process.
            killExistingProcess(pid, processQuery);
            pid = PID_UNKNOWN;
          }
          break;
      }

      // Return the pid.
      return pid;

    } catch (IOException ioEx) {
      throw new OfficeException(
          String.format(
              "Could not check if there is already an existing process with --accept '%s'", accept),
          ioEx);
    }
  }

  /**
   * Prepare the ProcessBuilder that will be used to launch the office process.
   *
   * @param acceptString The connection string (accept argument) of the office process.
   * @return The created ProcessBuilder.
   */
  private @NonNull ProcessBuilder prepareProcessBuilder(final @NonNull String acceptString) {

    // Create the command used to launch the office process
    final List<String> command = new ArrayList<>(runAsArgs);
    final File executable = LocalOfficeUtils.getOfficeExecutable(officeHome);

    // LibreOffice:
    // https://help.libreoffice.org/Common/Starting_the_Software_With_Parameters
    // https://help.libreoffice.org/7.4/en-US/text/shared/guide/start_parameters.html
    //
    // Apache OpenOffice:
    // https://wiki.openoffice.org/wiki/Framework/Article/Command_Line_Arguments

    final String execPath = executable.getAbsolutePath();
    final String prefix = descriptor.useLongOptionNameGnuStyle() ? "--" : "-";
    command.add(execPath);
    command.add(prefix + "accept=" + acceptString);
    command.add(prefix + "headless");
    command.add(prefix + "invisible");
    command.add(prefix + "nocrashreport");
    command.add(prefix + "nodefault");
    command.add(prefix + "nofirststartwizard");
    command.add(prefix + "nolockcheck");
    command.add(prefix + "nologo");
    command.add(prefix + "norestore");
    // command.add(prefix + "safe-mode"); // Add this to debug connection error (always work with
    // this argument)
    command.add("-env:UserInstallation=" + LocalOfficeUtils.toUrl(instanceProfileDir));

    // It could be interesting to use the LibreOffice pidfile switch
    // to retrieve the LibreOffice pid. But is it reliable? And it would
    // not work with Apache OpenOffice.

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("ProcessBuilder command: {}", String.join(" ", command));
    }
    return new ProcessBuilder(command);
  }

  /**
   * Detects the office descriptor. This function will create the OfficeDescriptor using the path of
   * the office executable.
   */
  private OfficeDescriptor detectOfficeDescriptor() {

    // Create the command used to launch the office process
    final File executable = LocalOfficeUtils.getOfficeExecutable(officeHome);

    final String execPath = executable.getAbsolutePath();

    return OfficeDescriptor.fromExecutablePath(execPath);
  }

  /** Kills the office process instance. */
  private void forciblyTerminateProcess() {

    // No need to terminate anything if we don't have anything to terminate.
    if (process == null && pid <= PID_UNKNOWN) {
      return;
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Trying to forcibly terminate process: '{}'; pid: {}",
          officeUrl.getAcceptString(),
          pid == PID_NOT_FOUND ? "PID_NOT_FOUND" : pid == PID_UNKNOWN ? "PID_UNKNOWN" : pid);
    }

    try {
      processManager.kill(process == null ? null : process.getProcess(), pid);
    } catch (IOException ex) {
      LOGGER.error("Could not forcibly terminate process", ex);
    }
  }

  /**
   * Ensures that the process exited.
   *
   * <p>This function is always called into tasks that are executed by a single thread {@link
   * ExecutorService} and thus, the function must manage its own exception handling.
   *
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted. We don't always want to delete the instance profile directory on restart since it
   *     may be an expensive operation.
   */
  private void ensureProcessExited(final boolean deleteInstanceProfileDir) {

    try {
      // If the process has never been started by us (process != null),
      // just return a success exit code (0).
      int exitCode = 0;
      if (process != null) {
        final ExitCodeRetryable retryable = new ExitCodeRetryable(process);
        retryable.execute(processRetryInterval, processTimeout);
        exitCode = retryable.getExitCode();
      }
      LOGGER.info("Process exited with code {}", exitCode);

    } catch (RetryTimeoutException ex) {
      LOGGER.error("Time out ensuring process exited", ex);
      forciblyTerminateProcess();

    } finally {
      if (deleteInstanceProfileDir) {
        deleteInstanceProfileDir();
      }
    }
  }

  /**
   * Prepare the profile directory of the office process.
   *
   * @throws OfficeException If the template profile directory cannot be copied to the new instance
   *     profile directory.
   */
  private void prepareInstanceProfileDir() throws OfficeException {

    if (instanceProfileDir.exists()) {
      LOGGER.warn("Profile dir '{}' already exists; deleting", instanceProfileDir);
      deleteInstanceProfileDir();
    }

    // Allow the templateProfileDir to be set using a System property for development.
    File templateDir = templateProfileDir;
    if (templateDir == null) {
      final String property =
          System.getProperty("org.jodconverter.local.manager.templateProfileDir");
      if (StringUtils.isNotBlank(property)) {
        templateDir = new File(property);
      }
    }
    if (templateDir != null) {
      try {
        FileUtils.copyDirectory(templateDir, instanceProfileDir);
      } catch (IOException ioEx) {
        throw new OfficeException("Failed to create the instance profile directory", ioEx);
      }
    }
  }

  /** Deletes the profile directory of the office process. */
  private void deleteInstanceProfileDir() {
    // TODO: Should the timeout be configurable?
    OfficeUtils.deleteOrRenameFile(instanceProfileDir, 250L, 1_000L);
  }
}
