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

import static org.jodconverter.process.ProcessManager.PID_NOT_FOUND;
import static org.jodconverter.process.ProcessManager.PID_UNKNOWN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.process.ProcessManager;
import org.jodconverter.process.ProcessQuery;

/**
 * An OfficeProcess represents an instance of an office program that is executed by JODConverter.
 */
class OfficeProcess {

  private static final Logger logger = LoggerFactory.getLogger(OfficeProcess.class);

  private Process process;
  private long pid = PID_UNKNOWN;
  private final OfficeUrl officeUrl;
  private final OfficeProcessConfig config;
  private final File instanceProfileDir;

  /**
   * Constructs a new instance of an office process class for the specified URL with default
   * configuration.
   *
   * @param officeUrl The URL for which the process is created.
   */
  public OfficeProcess(final OfficeUrl officeUrl) {
    this(officeUrl, new OfficeProcessConfig());
  }

  /**
   * Constructs a new instance of an office process class with the specified configuration.
   *
   * @param officeUrl The URL for which the process is created.
   * @param config The office process configuration.
   */
  public OfficeProcess(final OfficeUrl officeUrl, final OfficeProcessConfig config) {

    this.officeUrl = officeUrl;
    this.config = config;
    this.instanceProfileDir = getInstanceProfileDir();
  }

  /**
   * Checks if there already is an office process that runs with the connection string we want to
   * use. The process will be killed if the kill switch is on.
   *
   * @param processQuery The query that connection string we want to use.
   * @throws OfficeException If the verification fails.
   */
  private void checkForExistingProcess(final ProcessQuery processQuery) throws OfficeException {

    long existingPid = PID_UNKNOWN;

    try {
      // Search for an existing process that would prevent us to start a new
      // office process with the same connection string.
      final ProcessManager processManager = config.getProcessManager();
      existingPid = processManager.findPid(processQuery);

      // Kill the any running process with the same connection string if the kill switch is on
      if (!(existingPid == PID_NOT_FOUND || existingPid == PID_UNKNOWN)
          && config.isKillExistingProcess()) {
        logger.warn(
            "A process with acceptString '{}' is already running; pid {}",
            processQuery.getArgument(),
            existingPid);
        processManager.kill(null, existingPid);
        waitForProcessToDie();
        existingPid = processManager.findPid(processQuery);
      }

    } catch (IOException ioEx) {
      throw new OfficeException(
          String.format(
              "Unable to check if there is already an existing process with acceptString '%s'",
              processQuery.getArgument()),
          ioEx);
    }

    if (existingPid != PID_NOT_FOUND && existingPid != PID_UNKNOWN) {
      throw new OfficeException(
          String.format(
              "A process with acceptString '%s' is already running; pid %d",
              processQuery.getArgument(), existingPid));
    }
  }

  /** Deletes the profile directory of the office process. */
  public void deleteProfileDir() {

    if (instanceProfileDir != null) {
      logger.debug("Deleting instance profile directory '{}'", instanceProfileDir);
      try {
        FileUtils.deleteDirectory(instanceProfileDir);
      } catch (IOException ioEx) { // NOSONAR
        final File oldProfileDir =
            new File(
                instanceProfileDir.getParentFile(),
                instanceProfileDir.getName() + ".old." + System.currentTimeMillis());
        if (instanceProfileDir.renameTo(oldProfileDir)) {
          logger.warn(
              "Could not delete profileDir: {}; renamed it to {}",
              ioEx.getMessage(),
              oldProfileDir);
        } else {
          logger.error("Could not delete profileDir: {}", ioEx.getMessage());
        }
      }
    }
  }

  /**
   * Kills the office process instance.
   *
   * @param retryInterval The interval between each exit code retrieval attempt.
   * @param retryTimeout The timeout after which we won't try again to retrieve the exit code.
   * @throws OfficeException If we are unable to kill the process due to an I/O error occurs.
   * @throws RetryTimeoutException If we are unable to get the exit code of the process.
   */
  public int forciblyTerminate(final long retryInterval, final long retryTimeout) // NOSONAR
      throws OfficeException, RetryTimeoutException {

    logger.info(
        "Trying to forcibly terminate process: '{}'{}",
        officeUrl.getConnectionParametersAsString(),
        pid == PID_UNKNOWN ? "" : " (pid " + pid + ")");

    try {
      config.getProcessManager().kill(process, pid);
      return getExitCode(retryInterval, retryTimeout);
    } catch (IOException ioEx) {
      throw new OfficeException("Unable to kill the process with pid: " + pid, ioEx);
    }
  }

  /**
   * Gets the exit code of the office process.
   *
   * @return The exit value of the process. The value 0 indicates normal termination.
   */
  public Integer getExitCode() {

    try {
      return process.exitValue();
    } catch (IllegalThreadStateException illegalThreadStateEx) {
      logger.trace("IllegalThreadStateException catch in getExitCode", illegalThreadStateEx);
      return null;
    }
  }

  /**
   * Gets the exit code of the office process. We will try to get the exit code until we succeed or
   * that the specified timeout is reached.
   *
   * @param retryInterval The interval between each exit code retrieval attempt.
   * @param retryTimeout The timeout after which we won't try again to retrieve the exit code.
   * @return The exit value of the process. The value 0 indicates normal termination.
   * @throws OfficeException If we are unable to kill the process.
   * @throws RetryTimeoutException If we are unable to get the exit code of the process.
   */
  public int getExitCode(final long retryInterval, final long retryTimeout) // NOSONAR
      throws OfficeException, RetryTimeoutException {

    try {
      final ExitCodeRetryable retryable = new ExitCodeRetryable(process);
      retryable.execute(retryInterval, retryTimeout);
      return retryable.getExitCode();
    } catch (RetryTimeoutException retryTimeoutEx) {
      throw retryTimeoutEx;
    } catch (Exception ex) {
      throw new OfficeException("Could not get the process exit code", ex);
    }
  }

  /**
   * Gets the profile directory of the office process.
   *
   * @return The profile directory instance.
   */
  private File getInstanceProfileDir() {

    return new File(
        config.getWorkingDir(),
        ".jodconverter_"
            + officeUrl.getConnectionAndParametersAsString().replace(',', '_').replace('=', '-'));
  }

  /**
   * Gets the PID of the office process.
   *
   * @return The office process PID, or -1 of the PID is unknown.
   */
  public long getPid() {

    return pid;
  }

  /**
   * Gets whether the office process is running.
   *
   * @return {@code true} is the office process is running; {@code false otherwise}.
   */
  public boolean isRunning() {

    if (process == null) {
      return false;
    }
    return getExitCode() == null;
  }

  /**
   * Prepare the profile directory of the office process.
   *
   * @throws OfficeException If the template profile directory cannot be copied to the new instance
   *     profile directory.
   */
  private void prepareInstanceProfileDir() throws OfficeException {

    if (instanceProfileDir.exists()) {
      logger.warn("Profile dir '{}' already exists; deleting", instanceProfileDir);
      deleteProfileDir();
    }
    if (config.getTemplateProfileDir() != null) {
      try {
        FileUtils.copyDirectory(config.getTemplateProfileDir(), instanceProfileDir);
      } catch (IOException ioEx) {
        throw new OfficeException("Failed to create the instance profile directory", ioEx);
      }
    }
  }

  /**
   * Prepare the ProcessBuilder that will be used to launch the office process.
   *
   * @param acceptString The connection string (accept argument) of the office process.
   * @return The created ProcessBuilder.
   */
  private ProcessBuilder prepareProcessBuilder(final String acceptString) {

    // Create the command used to launch the office process
    final List<String> command = new ArrayList<>();
    final File executable = OfficeUtils.getOfficeExecutable(config.getOfficeHome());
    if (config.getRunAsArgs() != null) {
      command.addAll(Arrays.asList(config.getRunAsArgs()));
    }
    command.add(executable.getAbsolutePath());
    command.add("-accept=" + acceptString);
    command.add("-env:UserInstallation=" + OfficeUtils.toUrl(instanceProfileDir));
    command.add("-headless");
    command.add("-invisible");
    command.add("-nocrashreport");
    command.add("-nodefault");
    command.add("-nofirststartwizard");
    command.add("-nolockcheck");
    command.add("-nologo");
    command.add("-norestore");

    return new ProcessBuilder(command);
  }

  /**
   * Starts the office process.
   *
   * @throws OfficeException If the office process cannot be started.
   */
  public void start() throws OfficeException {

    start(false);
  }

  /**
   * Starts the office process.
   *
   * @param restart Indicates whether it is a fresh start of a restart after a failure.
   * @return The PID of the started office process, or -1 of the PID is unknown.
   * @throws OfficeException If the office process cannot be started.
   */
  public void start(final boolean restart) throws OfficeException {

    final String acceptString =
        officeUrl.getConnectionAndParametersAsString()
            + ";"
            + officeUrl.getProtocolAndParametersAsString()
            + ";"
            + officeUrl.getRootOid();

    // Search for an existing process.
    final ProcessQuery processQuery = new ProcessQuery("soffice", acceptString);
    checkForExistingProcess(processQuery);

    // Prepare the instance directory only on first start
    if (!restart) {
      prepareInstanceProfileDir();
    }

    // Create the builder used to launch the office process
    final ProcessBuilder processBuilder = prepareProcessBuilder(acceptString);

    // Launch the process.
    logger.info(
        "Starting process with acceptString '{}' and profileDir '{}'",
        acceptString,
        instanceProfileDir);
    try {
      process = processBuilder.start();
      pid = config.getProcessManager().findPid(processQuery);
      logger.info("Started process{}", pid == PID_UNKNOWN ? "" : "; pid = " + pid);
    } catch (IOException ioEx) {
      throw new OfficeException(
          String.format(
              "An I/O error prevents us to start a process with acceptString '%s'", acceptString),
          ioEx);
    }

    if (pid == PID_NOT_FOUND) {
      throw new OfficeException(
          String.format(
              "A process with acceptString '%s' started but its pid could not be found",
              acceptString));
    }
  }

  private void waitForProcessToDie() {

    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
