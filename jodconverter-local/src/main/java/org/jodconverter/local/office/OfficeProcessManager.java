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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.star.beans.XHierarchicalPropertySet;
import com.sun.star.beans.XHierarchicalPropertySetInfo;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.NamedThreadFactory;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.RetryTimeoutException;
import org.jodconverter.local.office.utils.Info;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.process.ProcessManager;

/**
 * An {@link OfficeProcessManager} is responsible to manage an office process and the connection
 * (bridge) to this office process.
 *
 * @see org.jodconverter.local.office.OfficeProcess
 * @see org.jodconverter.local.office.OfficeConnection
 */
class OfficeProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeProcessManager.class);

  // The default initial delay a process call (start/terminate).
  private static final long DEFAULT_PROCESS_INITIAL_DELAY = 0L; // No delay
  // The default timeout when executing a process call (start/terminate).
  private static final long DEFAULT_PROCESS_TIMEOUT = 120_000L; // 2 minutes
  // The default delay between each try when executing a process call (start/terminate).
  private static final long DEFAULT_PROCESS_RETRY_INTERVAL = 250L; // 0.25 secs.
  // The default behavior when an office process is started regarding to OpenGL usage.
  private static final boolean DEFAULT_DISABLE_OPENGL = false;
  // The path to the UseOpenGL configuration property.
  private static final String PROP_PATH_USE_OPENGL = "VCL/UseOpenGL";

  private final OfficeProcess process;
  private final OfficeConnection connection;
  private final ExecutorService executor;
  private final long processTimeout;
  private final long processRetryInterval;
  private final boolean disableOpengl;
  // Disconnection is expected when disabling OpenGL (restart required).
  private final AtomicBoolean disconnectExpected = new AtomicBoolean(false);

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
   * @param killExistingProcess Indicates whether an existing office process is killed when starting
   *     a new office process for the same connection string.
   * @param processTimeout The timeout, in milliseconds, when trying to execute an office process
   *     call (start/terminate).
   * @param processRetryInterval The delay, in milliseconds, between each try when trying to execute
   *     an office process call (start/terminate).
   * @param disableOpengl Indicates whether OpenGL must be disabled when starting a new office
   *     process. Nothing will be done if OpenGL is already disabled according to the user profile
   *     used with the office process. If the options is changed, then office must be restarted.
   */
  /* default */ OfficeProcessManager(
      final OfficeUrl officeUrl,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager,
      final List<String> runAsArgs,
      final File templateProfileDir,
      final Boolean killExistingProcess,
      final Long processTimeout,
      final Long processRetryInterval,
      final Boolean disableOpengl) {

    process =
        new OfficeProcess(
            officeUrl,
            officeHome,
            workingDir,
            processManager,
            runAsArgs,
            templateProfileDir,
            killExistingProcess);
    connection = new OfficeConnection(officeUrl);
    executor =
        Executors.newSingleThreadExecutor(new NamedThreadFactory("jodconverter-officeprocess"));
    this.processTimeout = processTimeout == null ? DEFAULT_PROCESS_TIMEOUT : processTimeout;
    this.processRetryInterval =
        processRetryInterval == null ? DEFAULT_PROCESS_RETRY_INTERVAL : processRetryInterval;
    this.disableOpengl = disableOpengl == null ? DEFAULT_DISABLE_OPENGL : disableOpengl;
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
   * <p>The task of starting the process and connecting to it is executed by a single thread {@link
   * java.util.concurrent.ExecutorService} and thus, the current {@code start()} function returns
   * immediately.
   */
  public void start() {

    // Submit a start task to the executor and wait
    executor.execute(() -> startProcessAndConnect(false, true));
  }

  /**
   * Restarts an office process and wait until we are connected to the restarted process.
   *
   * <p>The task of restarting the process and connecting to it is executed by a single thread
   * {@link java.util.concurrent.ExecutorService} and thus, the current {@code restart()} function
   * returns immediately.
   */
  public void restart() {
    LOGGER.info("Restarting...");

    executor.execute(
        () -> {
          // On clean restart, we won't delete the instance profile directory,
          // causing a faster start of an office process.
          stopProcess(false);
          startProcessAndConnect(true, false);
        });
  }

  /**
   * Restarts the office process when the connection is lost.
   *
   * <p>The task of restarting the process and connecting to it is executed by a single thread
   * {@link java.util.concurrent.ExecutorService} and thus, the current {@code
   * restartDueToLostConnection()} function returns immediately.
   */
  public void restartDueToLostConnection() {
    LOGGER.info("Restarting due to lost connection...");

    executor.execute(
        () -> {
          if (disconnectExpected.compareAndSet(true, false)) {
            LOGGER.debug("Connection lost because OpenGL was changed");
            // Since we have lost the connection because OpenGL was changed.
            // Thus, we want to keep the instance profile directory on restart.
            ensureProcessExited(false);
            startProcessAndConnect(true, false);
          } else {
            LOGGER.debug("Connection lost unexpectedly");
            // Since we have lost the connection unexpectedly, it could mean that
            // the office process has crashed. Thus, we want a clean instance profile
            // directory on restart.
            ensureProcessExited(true);
            startProcessAndConnect(false, true);
          }
        });
  }

  /**
   * Restarts the office process when there is a timeout while executing a task.
   *
   * <p>The function will only forcibly kill the office process, causing an unexpected disconnection
   * and subsequent restart.
   *
   * @see OfficeProcessManagerPoolEntry
   */
  public void restartDueToTaskTimeout() {
    LOGGER.info("Restarting due to task timeout...");

    // This will cause unexpected disconnection and subsequent restart.
    executor.execute(process::forciblyTerminate);
  }

  /** Stops an office process and waits until the process is stopped. */
  public void stop() {

    // Submit a task to stop the office process and wait task termination.
    // This is required if we don't want to let garbage on disk since the
    // stopProcess must be fully executed to clean the temp files and
    // directories.
    executor.execute(() -> stopProcess(true));

    // Shutdown the executor, no other task will be accepted.
    executor.shutdown();

    // Await for task termination. This is required if we don't want to
    // let garbage on disk since the stopProcess must be fully executed
    // to clean the temp files and directories.
    try {
      // TODO: Add <stop> configuration option for this ?
      // Wait 2 minutes max for termination. It seems a safe
      // and reasonable amount of time.
      executor.awaitTermination(2L, TimeUnit.MINUTES);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Starts the office process managed by this class and connect to the process.
   *
   * <p>This function is always called into tasks that are executed by a single thread {@link
   * java.util.concurrent.ExecutorService} and thus, the function must managed its own exception
   * handling.
   *
   * @param restart Indicates whether it is a fresh start or a restart. A restart will assume that
   *     the instance profile directory is already created. To recreate the instance profile
   *     directory, {@code restart} should be set to {@code false}.
   * @param checkOpengl Indicates whether we must check to change the OpenGL setting.
   */
  private void startProcessAndConnect(final boolean restart, final boolean checkOpengl) {
    LOGGER.debug("Starting the office process with restart set to {}...", restart);

    try {
      process.start(restart);
    } catch (OfficeException ex) {
      LOGGER.error(
          String.format("Could not %s the office process", restart ? "restart" : "start"), ex);
      return;
    }

    try {
      // TODO: Add configuration field for initial delay
      LOGGER.debug("Connecting to the started office process...");
      new ConnectRetryable(connection, process)
          .execute(DEFAULT_PROCESS_INITIAL_DELAY, processRetryInterval, processTimeout);

      // Here a connection has been made successfully. Check to disable
      // the usage of OpenGL. Some file won't load properly if OpenGL
      // is on (LibreOffice).
      if (checkOpengl && disableOpengl && disableOpengl(connection.getComponentContext())) {

        LOGGER.info("OpenGL has been disabled and a restart is required; restarting...");
        // Set disconnectExpected to tru in order to avoid instanceProfileDir deletion.
        disconnectExpected.set(true);
        // This will cause unexpected disconnection and subsequent restart.
        executor.execute(process::forciblyTerminate);
      }

    } catch (Exception ex) {
      LOGGER.error(
          String.format(
              "Could not establish connection to the office process after %s",
              restart ? "restart" : "start"),
          ex);
    }
  }

  /**
   * Stops the office process managed by this OfficeProcessManager.
   *
   * <p>This function is always called into tasks that are executed by a single thread {@link
   * java.util.concurrent.ExecutorService} and thus, the function must managed its own exception
   * handling.
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
      final XDesktop desktop = connection.getDesktop();
      if (desktop == null) {
        // We are not connected to the office process. We can still try to terminate it.
        process.forciblyTerminate();
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
  }

  /**
   * Ensures that the process exited.
   *
   * <p>This function is always called into tasks that are executed by a single thread {@link
   * java.util.concurrent.ExecutorService} and thus, the function must managed its own exception
   * handling.
   *
   * @param deleteInstanceProfileDir If {@code true}, the instance profile directory will be
   *     deleted. We don't always want to delete the instance profile directory on restart since it
   *     may be an expensive operation.
   */
  private void ensureProcessExited(final boolean deleteInstanceProfileDir) {

    try {
      final int exitCode = process.getExitCode(processRetryInterval, processTimeout);
      LOGGER.info("Process exited with code {}", exitCode);

    } catch (RetryTimeoutException ex) {
      LOGGER.error("Time out ensuring process exited", ex);
      process.forciblyTerminate();

    } finally {
      if (deleteInstanceProfileDir) {
        process.deleteInstanceProfileDir();
      }
    }
  }

  private boolean disableOpengl(final XComponentContext officeContext) throws OfficeException {

    // See configuration registry for more options.
    // e.g: C:\Program Files\LibreOffice 5\share\registry\main.xcd

    try {

      // Create the view to the root element where UseOpenGL option lives
      final Object viewRoot =
          Info.getConfigUpdateAccess(officeContext, "/org.openoffice.Office.Common");
      if (viewRoot == null) {
        return false; // No restart needed
      }
      try {

        // Check if the OpenGL option is on
        final XHierarchicalPropertySet properties = Lo.qi(XHierarchicalPropertySet.class, viewRoot);

        final XHierarchicalPropertySetInfo propsInfo = properties.getHierarchicalPropertySetInfo();
        if (propsInfo.hasPropertyByHierarchicalName(PROP_PATH_USE_OPENGL)) {
          final boolean useOpengl =
              (boolean) properties.getHierarchicalPropertyValue(PROP_PATH_USE_OPENGL);
          LOGGER.info("Use OpenGL is set to {}", useOpengl);
          if (useOpengl) {
            properties.setHierarchicalPropertyValue(PROP_PATH_USE_OPENGL, false);
            // Changes have been applied to the view here
            final XChangesBatch updateControl = Lo.qi(XChangesBatch.class, viewRoot);
            updateControl.commitChanges();

            // A restart is required.
            return true;
          }
        }
      } finally {
        // We are done with the view - dispose it
        Lo.qi(XComponent.class, viewRoot).dispose();
      }
      return false; // No restart needed

    } catch (com.sun.star.uno.Exception ex) {
      throw new OfficeException("Could not check if the Use OpenGL option is on.", ex);
    }
  }
}
