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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.star.lang.DisposedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <p>All its functions block until they are done, and are called by a single thread, the one of the
 * {@link LocalOfficeWorker} that owns the manager. Only {@link #kill()} may be called by another
 * thread.
 *
 * @see OfficeConnection
 */
class LocalOfficeProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeProcessManager.class);

  // How long a process that lost its connection gets to exit by itself before it is killed. A
  // process that crashed is already gone; one that is still running after losing its connection
  // (a disposed bridge) is not worth waiting the whole process timeout for.
  private static final long LOST_CONNECTION_EXIT_TIMEOUT = 2_000L;

  // How long to wait for a connection when checking whether a port is already used.
  private static final int PORT_CHECK_TIMEOUT = 1_000;

  // Volatile: read by the thread that kills the process.
  private volatile VerboseProcess process;
  private volatile long pid = PID_UNKNOWN;
  private OfficeDescriptor descriptor;
  private final OfficeConnection connection;
  private final File instanceProfileDir;
  private final OfficeUrl officeUrl;
  private final File officeHome;
  private final File officeExecutable;
  private final ProcessManager processManager;
  private final List<String> runAsArgs;
  private final File templateProfileDir;
  private final long processTimeout;
  private final long processRetryInterval;
  private final long afterStartProcessDelay;
  private final ExistingProcessAction existingProcessAction;
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
      final boolean keepAliveOnShutdown,
      final OfficeConnection connection) {

    this(
        officeUrl,
        officeHome,
        null,
        workingDir,
        processManager,
        runAsArgs,
        templateProfileDir,
        processTimeout,
        processRetryInterval,
        afterStartProcessDelay,
        existingProcessAction,
        keepAliveOnShutdown,
        connection);
  }

  /**
   * Creates a new manager with the specified configuration, starting the office process with the
   * specified executable.
   *
   * @param officeUrl The URL for which the office process is created.
   * @param officeHome The home directory of the office installation, used to find the executable
   *     when {@code officeExecutable} is {@code null}.
   * @param officeExecutable The program that starts the office process (a launcher such as the one
   *     of a snap or an AppImage), or {@code null} to use the executable of the office home.
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
   * @param keepAliveOnShutdown Controls whether the manager will keep the office process alive on
   *     shutdown.
   * @param connection The object that will manage the connection to the office process.
   */
  /* default */ LocalOfficeProcessManager(
      final OfficeUrl officeUrl,
      final File officeHome,
      final File officeExecutable,
      final File workingDir,
      final ProcessManager processManager,
      final List<String> runAsArgs,
      final File templateProfileDir,
      final long processTimeout,
      final long processRetryInterval,
      final long afterStartProcessDelay,
      final ExistingProcessAction existingProcessAction,
      final boolean keepAliveOnShutdown,
      final OfficeConnection connection) {

    this.officeUrl = officeUrl;
    this.officeHome = officeHome;
    this.officeExecutable = officeExecutable;
    this.processManager = processManager;
    this.runAsArgs = runAsArgs;
    this.templateProfileDir = templateProfileDir;
    this.processTimeout = processTimeout;
    this.processRetryInterval = processRetryInterval;
    this.afterStartProcessDelay = afterStartProcessDelay;
    this.existingProcessAction = existingProcessAction;
    this.keepAliveOnShutdown = keepAliveOnShutdown;
    this.connection = connection;

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
   * Starts an office process and connects to it. The function returns when the connection is
   * established.
   *
   * @throws OfficeException If the office process cannot be started, or we are unable to connect to
   *     the started process.
   */
  /* default */ void start() throws OfficeException {

    startProcessAndConnect(false, true);
  }

  /**
   * Restarts an office process that is still usable, after it executed its maximum number of tasks
   * for example. The office process is asked to terminate, and a new one is started with the same
   * instance profile directory, which makes its start faster. The function returns when the
   * connection to the new process is established.
   *
   * @throws OfficeException If the office process cannot be started, or we are unable to connect to
   *     the started process.
   */
  /* default */ void restart() throws OfficeException {
    LOGGER.info("Restarting...");

    stopProcess(false);
    startProcessAndConnect(true, false);
  }

  /**
   * Restarts an office process that cannot be used anymore: its connection was lost, it was killed,
   * or it could not be started. The function returns when the connection to the new process is
   * established.
   *
   * @throws OfficeException If the office process cannot be started, or we are unable to connect to
   *     the started process.
   */
  /* default */ void restartDueToLostConnection() throws OfficeException {
    LOGGER.info("Restarting due to lost connection...");

    // When no office process was started, the port is checked as it is on the first start.
    final var neverStarted = process == null && pid <= PID_UNKNOWN;

    // Since we have lost the connection unexpectedly, it could mean that
    // the office process has crashed. Thus, we want a clean instance profile
    // directory on restart. A process still running is killed after a short
    // grace period: without its connection, it cannot be used anymore.
    ensureProcessExited(true, Math.min(processTimeout, LOST_CONNECTION_EXIT_TIMEOUT));
    startProcessAndConnect(false, neverStarted);
  }

  /**
   * Forcibly terminates the office process, if any. This is how a task that must not go on is
   * ended: the function may be called by any thread, while another function of this manager is
   * running, and does not wait for the process to exit.
   */
  /* default */ void kill() {

    forciblyTerminateProcess();
  }

  /**
   * Stops the office process and waits until the process is stopped. If the process must be kept
   * alive on shutdown, the connection is only closed.
   */
  /* default */ void stop() {

    if (keepAliveOnShutdown) {
      // We must disconnect from the process
      LOGGER.debug("Disconnecting from the office process, which is kept alive...");
      connection.disconnect();
    } else {
      // We must stop the process. This is required if we don't want to let garbage on disk
      // since the stopProcess must be fully executed to clean the temp files and directories.
      stopProcess(true);
    }
  }

  /**
   * Starts the office process managed by this manager and connect to the started process.
   *
   * @param restart Indicates whether it is a fresh start or a restart. A restart will assume that
   *     the instance profile directory is already created. To recreate the instance profile
   *     directory, {@code restart} should be set to {@code false}.
   * @param checkPortAvailable If {@code true}, fails right away when the port of a socket
   *     connection is already used by another program. Not done when an office process was started
   *     before: it may still be releasing the port.
   * @throws OfficeException If the office process cannot be started, or we are unable to connect to
   *     the started process.
   */
  private void startProcessAndConnect(final boolean restart, final boolean checkPortAvailable)
      throws OfficeException {

    // Reinitialize pid and process.
    pid = PID_UNKNOWN;
    process = null;

    // Detect the office version if required.
    if (descriptor == null) {
      descriptor = detectOfficeDescriptor();
    }

    // Build the 'accept' argument (connection string).
    final var acceptString = officeUrl.getAcceptString();

    // Search for an existing process.
    final var processQuery = new ProcessQuery("soffice", acceptString);
    pid = checkForExistingProcess(processQuery);

    // If we already have a PID, it means that the process is already started and that
    // the configuration didn't tell us to kill the process.
    if (pid > PID_UNKNOWN) {
      return;
    }

    // No office process uses the connection string. If another program already listens on the
    // port, the office process we would start cannot listen on it, and we would try to connect
    // to that other program instead.
    if (checkPortAvailable) {
      checkPortAvailable(acceptString);
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
  }

  /**
   * Fails if the port of a socket connection is already used by another program.
   *
   * @param acceptString The connection string (accept argument) of the office process.
   * @throws OfficeException If another program listens on the port.
   */
  private void checkPortAvailable(final String acceptString) throws OfficeException {

    final var unoUrl = officeUrl.unoUrl();
    if (!"socket".equalsIgnoreCase(unoUrl.getConnection())) {
      return; // Pipes and websockets have no port
    }
    // The office API returns a raw map
    @SuppressWarnings("unchecked")
    final Map<String, String> parameters = unoUrl.getConnectionParameters();
    final var host = parameters.get("host");
    final var port = Integer.parseInt(parameters.get("port"));

    try (var socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), PORT_CHECK_TIMEOUT);
    } catch (IOException ex) {
      // Nothing listens on the port: the office process can use it.
      return;
    }
    throw new OfficeException(
        String.format(
            "Port %d on host '%s' is already used by another program; cannot start an office"
                + " process with --accept '%s'",
            port, host, acceptString));
  }

  private void executeStartProcessAndConnect(
      final String acceptString, final ProcessQuery processQuery) throws OfficeException {

    // Create the builder used to launch the office process
    final var processBuilder = prepareProcessBuilder(acceptString);

    LOGGER.debug("OFFICE EXECUTABLE: {}", getOfficeExecutable());
    LOGGER.info(
        "Starting process with --accept '{}' and profileDir '{}'",
        acceptString,
        instanceProfileDir);

    // Launch the process.
    try {
      // Start the process.
      final var retryable =
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
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted. We don't always want to delete the instance profile directory on restart since it
   *     may be an expensive operation.
   */
  private void stopProcess(final boolean deleteInstanceProfileDir) {
    LOGGER.debug(
        "Stopping the office process with deleteInstanceProfileDir set to {}...",
        deleteInstanceProfileDir);

    try {
      final var desktop = connection.getDesktop();
      if (desktop == null) {
        // We are not connected to the office process. We can still try to terminate it.
        forciblyTerminateProcess();
      } else {
        // Try to terminate
        final var terminated = connection.getDesktop().terminate();

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
  }

  private void killExistingProcess(final long pid, final ProcessQuery processQuery)
      throws IOException, OfficeException {

    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn(
          "A process with --accept '{}' is already running; pid {}; trying to kill it...",
          processQuery.argument(),
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
              processQuery.argument(), pid));
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

    final var accept = processQuery.argument();
    try {
      // Search for an existing process that would prevent us to start a new
      // office process with the same connection string.
      var pid = processManager.findPid(processQuery);

      if (pid <= PID_UNKNOWN) {
        // No process was found.
        LOGGER.debug(
            "Checking existing process done; no process running with --accept '{}'", accept);
        return pid;
      }

      // A process was found!
      switch (existingProcessAction) {
        case FAIL ->
            // Throw an exception if the kill switch is off.
            throw new OfficeException(
                String.format(
                    "A process with --accept '%s' is already running; pid %d", accept, pid));
        case KILL -> {
          // Kill any running process with the same connection string if the kill switch is on.
          killExistingProcess(pid, processQuery);
          pid = PID_UNKNOWN;
        }
        case CONNECT ->
            // Connect to the existing office process.
            connectToExistingProcess(pid, accept);
        case CONNECT_OR_KILL -> {
          // Try to connect to the existing office process.
          try {
            connectToExistingProcess(pid, accept);
          } catch (OfficeException ex) {
            // Could not establish connection. Kill the process.
            killExistingProcess(pid, processQuery);
            pid = PID_UNKNOWN;
          }
        }
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
    final var command = new ArrayList<>(runAsArgs);
    final var executable = getOfficeExecutable();

    // LibreOffice:
    // https://help.libreoffice.org/Common/Starting_the_Software_With_Parameters
    // https://help.libreoffice.org/7.4/en-US/text/shared/guide/start_parameters.html
    //
    // Apache OpenOffice:
    // https://wiki.openoffice.org/wiki/Framework/Article/Command_Line_Arguments

    final var execPath = executable.getAbsolutePath();
    final var prefix = descriptor.useLongOptionNameGnuStyle() ? "--" : "-";
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
    final var processBuilder = new ProcessBuilder(command);

    // The office process writes its temporary files (lu*.tmp) into the instance profile
    // directory instead of the system temp directory. It only removes them on a graceful exit;
    // this way, they are also removed with the profile directory when the process is killed.
    final var tempDir = getInstanceTempDir();
    if (!tempDir.isDirectory() && !tempDir.mkdirs()) {
      LOGGER.warn("Could not create the temp directory '{}'", tempDir);
    }
    final var environment = processBuilder.environment();
    environment.put("TMPDIR", tempDir.getAbsolutePath()); // Linux, macOS
    environment.put("TMP", tempDir.getAbsolutePath()); // Windows
    environment.put("TEMP", tempDir.getAbsolutePath()); // Windows
    return processBuilder;
  }

  /**
   * Gets the directory where the office process writes its temporary files.
   *
   * @return The temp directory, inside the instance profile directory.
   */
  /* default */
  @NonNull File getInstanceTempDir() {
    return new File(instanceProfileDir, "tmp");
  }

  /**
   * Detects the office descriptor. This function will create the OfficeDescriptor using the path of
   * the office executable.
   */
  private OfficeDescriptor detectOfficeDescriptor() {

    // Create the command used to launch the office process
    final var executable = getOfficeExecutable();

    final var execPath = executable.getAbsolutePath();

    return OfficeDescriptor.fromExecutablePath(execPath);
  }

  /**
   * Gets the program that starts the office process: the configured office executable if any,
   * otherwise the executable of the office home.
   *
   * @return The office executable.
   */
  private File getOfficeExecutable() {
    return officeExecutable == null
        ? LocalOfficeUtils.getOfficeExecutable(officeHome)
        : officeExecutable;
  }

  /** Kills the office process instance. */
  private void forciblyTerminateProcess() {

    // The process may be replaced by the thread of this manager while another thread kills it.
    final var process = this.process;
    final var pid = this.pid;

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
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted. We don't always want to delete the instance profile directory on restart since it
   *     may be an expensive operation.
   */
  private void ensureProcessExited(final boolean deleteInstanceProfileDir) {

    ensureProcessExited(deleteInstanceProfileDir, processTimeout);
  }

  /**
   * Ensures that the process exited, forcibly terminating it if it is still running after the
   * specified timeout.
   *
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted.
   * @param exitTimeout The maximum time to wait for the process to exit by itself, in milliseconds.
   */
  private void ensureProcessExited(final boolean deleteInstanceProfileDir, final long exitTimeout) {

    try {
      // If the process has never been started by us (process != null),
      // just return a success exit code (0).
      var exitCode = 0;
      if (process != null) {
        final var retryable = new ExitCodeRetryable(process);
        retryable.execute(processRetryInterval, exitTimeout);
        exitCode = retryable.getExitCode();
      }
      LOGGER.info("Process exited with code {}", exitCode);

    } catch (RetryTimeoutException ex) {
      LOGGER.warn("Process did not exit within {} ms; forcibly terminating it", exitTimeout);
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
    var templateDir = templateProfileDir;
    if (templateDir == null) {
      final var property = System.getProperty("org.jodconverter.local.manager.templateProfileDir");
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
