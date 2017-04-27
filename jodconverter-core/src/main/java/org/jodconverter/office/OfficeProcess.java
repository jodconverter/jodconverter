/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
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
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

import org.jodconverter.process.ProcessManager;
import org.jodconverter.process.ProcessQuery;

/** Represents an office process. */
class OfficeProcess {

  private static final Logger logger = LoggerFactory.getLogger(OfficeProcess.class);

  private Process process;
  private long pid = PID_UNKNOWN;
  private final File officeHome;
  private final UnoUrl unoUrl;
  private final String[] runAsArgs;
  private final File templateProfileDir;
  private final File instanceProfileDir;
  private final ProcessManager processManager;
  private final boolean killExistingProcess;

  /**
   * Constructs a new instance of an office process class with the specified settings.
   *
   * @param settings the office process settings.
   */
  public OfficeProcess(
      final File officeHome,
      final UnoUrl unoUrl,
      final String[] runAsArgs,
      final File templateProfileDir,
      final File workingDir,
      final ProcessManager processManager,
      final boolean killExistingProcess) {

    this.officeHome = officeHome;
    this.unoUrl = unoUrl;
    this.runAsArgs = ArrayUtils.clone(runAsArgs);
    this.templateProfileDir = templateProfileDir;
    this.processManager = processManager;
    this.killExistingProcess = killExistingProcess;
    this.instanceProfileDir = getInstanceProfileDir(workingDir, unoUrl);
  }

  /**
   * Checks if there already is an office process that runs with the connection string we want to
   * use. The process will be killed if the kill switch is on.
   *
   * @param processQuery the query that connection string we want to use.
   * @throws OfficeException if the verification fails.
   */
  private void checkForExistingProcess(final ProcessQuery processQuery) throws OfficeException {

    long existingPid = PID_UNKNOWN;

    try {
      // Search for an existing process that would prevent us to start a new
      // office process with the same connection string.
      existingPid = processManager.findPid(processQuery);

      // Kill the any running process with the same connection string if the kill switch is on
      if (!(existingPid == PID_NOT_FOUND || existingPid == PID_UNKNOWN) && killExistingProcess) {
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
   * Kills the office process.
   *
   * @param retryInterval internal between each exit code retrieval attempt.
   * @param retryTimeout timeout after which we won't try again to retrieve the exit code.
   * @return the exit code.
   * @throws OfficeException if we are unable to kill the process due to an I/O error occurs.
   * @throws RetryTimeoutException if we are unable to get the exit code of the process.
   */
  public int forciblyTerminate(final long retryInterval, final long retryTimeout) // NOSONAR
      throws OfficeException, RetryTimeoutException {

    logger.info(
        "Trying to forcibly terminate process: '{}'{}",
        unoUrl.getConnectionParametersAsString(),
        pid == PID_UNKNOWN ? "" : " (pid " + pid + ")");

    try {
      processManager.kill(process, pid);
      return getExitCode(retryInterval, retryTimeout);
    } catch (IOException ioEx) {
      throw new OfficeException("Unable to kill the process with pid: " + pid, ioEx);
    }
  }

  /**
   * Gets the exit code of the office process.
   *
   * @return the exit value of the process. The value 0 indicates normal termination.
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
   * @param retryInterval internal between each retrieval attempt.
   * @param retryTimeout timeout after which we won't try again to retrieve the exit code.
   * @return the exit value of the process. The value 0 indicates normal termination.
   * @throws OfficeException if we are unable to kill the process.
   * @throws RetryTimeoutException if we are unable to get the exit code of the process.
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
   * @param workingDir the working directory.
   * @param unoUrl the UNO URL for the process.
   * @return the profile directory instance.
   */
  private File getInstanceProfileDir(final File workingDir, final UnoUrl unoUrl) {

    return new File(
        workingDir,
        ".jodconverter_"
            + unoUrl.getConnectionAndParametersAsString().replace(',', '_').replace('=', '-'));
  }

  /**
   * Gets the PID of the office process.
   *
   * @return the office process PID, or -1 of the PID is unknown.
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
   * @throws OfficeException if the template profile directory cannot be copied to the new instance
   *     profile directory.
   */
  private void prepareInstanceProfileDir() throws OfficeException {

    if (instanceProfileDir.exists()) {
      logger.warn("Profile dir '{}' already exists; deleting", instanceProfileDir);
      deleteProfileDir();
    }
    if (templateProfileDir != null) {
      try {
        FileUtils.copyDirectory(templateProfileDir, instanceProfileDir);
      } catch (IOException ioEx) {
        throw new OfficeException("Failed to create the instance profile directory", ioEx);
      }
    }
  }

  /**
   * Prepare the ProcessBuilder that will be used to launch the office process.
   *
   * @param acceptString the connection string (accept argument) of the office process.
   * @return the created ProcessBuilder.
   */
  private ProcessBuilder prepareProcessBuilder(final String acceptString) {

    // Create the command used to launch the office process
    final List<String> command = new ArrayList<>();
    final File executable = OfficeUtils.getOfficeExecutable(officeHome);
    if (runAsArgs != null) {
      command.addAll(Arrays.asList(runAsArgs));
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
   * @throws OfficeException if the office process cannot be started.
   */
  public void start() throws OfficeException {

    start(false);
  }

  /**
   * Starts the office process.
   *
   * @param restart Indicates whether it is a fresh start of a restart after a failure.
   * @return the PID of the started office process, or -1 of the PID is unknown.
   * @throws OfficeException if the office process cannot be started.
   */
  public void start(final boolean restart) throws OfficeException {

    final String acceptString =
        unoUrl.getConnectionAndParametersAsString()
            + ";"
            + unoUrl.getProtocolAndParametersAsString()
            + ";"
            + unoUrl.getRootOid();

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
      pid = processManager.findPid(processQuery);
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
