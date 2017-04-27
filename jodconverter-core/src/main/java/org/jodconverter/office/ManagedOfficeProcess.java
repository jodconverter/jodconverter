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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.DisposedException;

/**
 * A ManagedOfficeProcess is responsible to manage an office process and the connection (bridge) to
 * this office process.
 *
 * @see OfficeProcess
 * @see OfficeConnection
 */
class ManagedOfficeProcess {

  private static final Logger logger = LoggerFactory.getLogger(ManagedOfficeProcess.class);

  private final OfficeProcess process;
  private final OfficeConnection connection;
  private final ManagedOfficeProcessSettings settings;
  private final ExecutorService executor;

  /**
   * Creates a new instance of the class with the specified settings.
   *
   * @param settings the managed office process settings.
   */
  public ManagedOfficeProcess(final ManagedOfficeProcessSettings settings) {

    this.settings = settings;
    process =
        new OfficeProcess(
            settings.getOfficeHome(),
            settings.getUnoUrl(),
            settings.getRunAsArgs(),
            settings.getTemplateProfileDir(),
            settings.getWorkingDir(),
            settings.getProcessManager(),
            settings.isKillExistingProcess());
    connection = new OfficeConnection(settings.getUnoUrl());
    executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OfficeProcessThread"));
  }

  /**
   * Ensures that the process exited.
   *
   * @throws OfficeException if an exception occurs.
   */
  private void doEnsureProcessExited() throws OfficeException {
    logger.trace("> doEnsureProcessExited");

    try {
      final int exitCode =
          process.getExitCode(settings.getRetryInterval(), settings.getRetryTimeout());
      logger.info("process exited with code " + exitCode);

    } catch (RetryTimeoutException retryTimeoutEx) {

      logger.debug("doEnsureProcessExited times out", retryTimeoutEx);
      doTerminateProcess();

    } finally {
      process.deleteProfileDir();
      logger.trace("< doEnsureProcessExited");
    }
  }

  /** Starts the office process managed by this class and connect to the process. */
  private void doStartProcessAndConnect() throws OfficeException {
    logger.trace("> doStartProcessAndConnect");

    try {
      process.start();
      new ConnectRetryable(process, connection)
          .execute(settings.getRetryInterval(), settings.getRetryTimeout());

    } catch (Exception ex) {
      throw new OfficeException("Could not establish connection", ex);

    } finally {
      logger.trace("< doStartProcessAndConnect");
    }
  }

  /** Stops the office process managed by this class. */
  private void doStopProcess() throws OfficeException {
    logger.trace("> doStopProcess");

    try {
      boolean terminated = connection.getDesktop().terminate();

      // once more: try to terminate
      logger.debug(
          "The Office Process {}",
          terminated
              ? "has been terminated"
              : "is still running. Someone else prevents termination, e.g. the quickstarter");

    } catch (DisposedException disposedEx) {
      // expected so ignore it
      logger.debug("DisposedException catched and ignored in doStopProcess", disposedEx);

    } catch (Exception ex) {
      logger.debug("Exception catched in doStopProcess", ex);
      // in case we can't get hold of the desktop
      doTerminateProcess();

    } finally {
      doEnsureProcessExited();
      logger.trace("< doStopProcess");
    }
  }

  /**
   * Ensures that the process exited.
   *
   * @throws OfficeException if an exception occurs.
   */
  private void doTerminateProcess() throws OfficeException {
    logger.trace("> doTerminateProcess");

    try {
      final int exitCode =
          process.forciblyTerminate(settings.getRetryInterval(), settings.getRetryTimeout());
      logger.info("process forcibly terminated with code " + exitCode);

    } catch (Exception ex) {
      throw new OfficeException("Could not terminate process", ex);

    } finally {
      logger.trace("< doTerminateProcess");
    }
  }

  // Executes the specified task without waiting for the completion of the task
  private void execute(final String taskName, final Runnable task) {
    logger.trace("> execute - '{}'", taskName);

    logger.info("Executing task '{}'...", taskName);
    executor.execute(task);

    logger.trace("< execute - '{}'", taskName);
  }

  /**
   * Gets the connection of this ManagedOfficeProcess.
   *
   * @return the {@link OfficeConnection} of this ManagedOfficeProcess.
   */
  public OfficeConnection getConnection() {
    return connection;
  }

  /**
   * Gets the process of this ManagedOfficeProcess.
   *
   * @return the {@link OfficeProcess} of this ManagedOfficeProcess.
   */
  public OfficeProcess getOfficeProcess() {
    return process;
  }

  /** Gets whether the connection to the office instance is opened. */
  public boolean isConnected() {
    return connection.isConnected();
  }

  /**
   * Restarts an office process and wait until we are connected to the retarted process.
   *
   * @throws OfficeException if we are not able to restart the office process.
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
   * @throws OfficeException if we are not able to start and connect to the office process.
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
   * @throws OfficeException if we are not able to stop the office process.
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
    logger.trace("> submitAndWait - '{}'", taskName);

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

    } finally {
      logger.trace("< submitAndWait - '{}'", taskName);
    }
  }
}
