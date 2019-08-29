/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XHierarchicalPropertySet;
import com.sun.star.beans.XHierarchicalPropertySetInfo;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesBatch;

import org.jodconverter.office.utils.Lo;
import org.jodconverter.task.OfficeTask;

/**
 * A OfficeProcessManagerPoolEntry is responsible to execute tasks submitted through a {@link
 * LocalOfficeManager}. It will submit tasks to its inner {@link OfficeProcessManager} and wait
 * until the task is done or a configured task execution timeout is reached.
 *
 * <p>A OfficeProcessManagerPoolEntry is also responsible to restart an office process when the
 * maximum number of tasks per process is reached.
 *
 * @see OfficeProcessManager
 * @see LocalOfficeManager
 */
class OfficeProcessManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

  private static final String PROPPATH_USE_OPENGL = "VCL/UseOpenGL";
  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeProcessManagerPoolEntry.class);

  private final OfficeProcessManager officeProcessManager;

  private final AtomicInteger taskCount = new AtomicInteger(0);
  private final AtomicBoolean disconnectExpected = new AtomicBoolean(false);

  /**
   * This connection event listener will be notified when a connection is established or closed/lost
   * to/from an office instance.
   */
  private final OfficeConnectionEventListener connectionEventListener =
      new OfficeConnectionEventListener() { // NOSONAR

        // A connection is established.
        @Override
        public void connected(final OfficeConnectionEvent event) {

          // Reset the task count and make the task executor available.
          taskCount.set(0);
          taskExecutor.setAvailable(true);
        }

        // A connection is closed/lost.
        @Override
        public void disconnected(final OfficeConnectionEvent event) {

          // Make the task executor unavailable.
          taskExecutor.setAvailable(false);

          // When it comes from an expected behavior (we have put
          // the field to true before calling a function), just reset
          // the disconnectExpected value to false. When we didn't expect
          // the disconnection, we must restart the office process, which
          // will cancel any task that may be running.
          if (!disconnectExpected.compareAndSet(true, false)) {

            // Here, we didn't expect this disconnection. We must restart
            // the office process, canceling any task that may be running.
            LOGGER.warn("Connection lost unexpectedly; attempting restart");
            if (currentFuture != null) {
              currentFuture.cancel(true);
            }
            officeProcessManager.restartDueToLostConnection();
          }
        }
      };

  /**
   * Creates a new pool entry for the specified office URL with default configuration.
   *
   * @param officeUrl The URL for which the entry is created.
   */
  public OfficeProcessManagerPoolEntry(final OfficeUrl officeUrl) {
    this(officeUrl, new OfficeProcessManagerPoolEntryConfig());
  }

  /**
   * Creates a new pool entry for the specified office URL with the specified configuration.
   *
   * @param officeUrl The URL for which the entry is created.
   * @param config The entry configuration.
   */
  public OfficeProcessManagerPoolEntry(
      final OfficeUrl officeUrl, final OfficeProcessManagerPoolEntryConfig config) {
    super(config);

    // Create the process manager that will deal with the office instance
    officeProcessManager = new OfficeProcessManager(officeUrl, config);

    // Listen to any connection events to the office instance.
    officeProcessManager.getConnection().addConnectionEventListener(connectionEventListener);
  }

  @Override
  public void doExecute(final OfficeTask task) throws OfficeException {

    final OfficeProcessManagerPoolEntryConfig entryConfig =
        (OfficeProcessManagerPoolEntryConfig) config;

    // First check if the office process must be restarted
    final int count = taskCount.getAndIncrement();
    if (entryConfig.getMaxTasksPerProcess() > 0 && count == entryConfig.getMaxTasksPerProcess()) {

      LOGGER.info(
          "Reached limit of {} maximum tasks per process; restarting...",
          entryConfig.getMaxTasksPerProcess());
      restart();

      // taskCount will be 0 rather than 1 at this point, so fix this.
      taskCount.getAndIncrement();
    }

    // Execute the task
    task.execute(officeProcessManager.getConnection());
  }

  @Override
  protected void handleExecuteTimeoutException(final TimeoutException timeoutEx) {

    // Is the the task did not complete within the configured timeout, we must restart
    officeProcessManager.restartDueToTaskTimeout();
  }

  @Override
  public boolean isRunning() {

    return super.isRunning() && officeProcessManager.getConnection().isConnected();
  }

  @Override
  public void doStart() throws OfficeException {

    // Start the office process and connect to it.
    officeProcessManager.startAndWait();

    // Here a connection has been made successfully. Check to disable
    // the usage of OpenGL. Some file won't load properly if OpenGL
    // is on (LibreOffice).
    final OfficeProcessManagerPoolEntryConfig entryConfig =
        (OfficeProcessManagerPoolEntryConfig) config;
    if (entryConfig.isDisableOpengl()
        && disableOpengl(officeProcessManager.getConnection().getComponentContext())) {

      LOGGER.info("OpenGL has been disabled and a restart is required; restarting...");
      restart();
    }
  }

  @Override
  public void doStop() throws OfficeException {

    // From here on, any disconnection from an office process is expected.
    disconnectExpected.set(true);

    // Now we can stopped the running office process
    officeProcessManager.stopAndWait();
  }

  private void restart() throws OfficeException {

    // The executor is no longer available
    taskExecutor.setAvailable(false);

    // Indicates that the disconnection to follow is expected
    disconnectExpected.set(true);

    // Restart the office instance
    officeProcessManager.restartAndWait();
  }

  // Create a configuration view for the specified configuration path.
  private Object createConfigurationView(final XMultiServiceFactory provider, final String path)
      throws com.sun.star.uno.Exception {

    // Creation arguments: nodepath
    final PropertyValue argument = new PropertyValue();
    argument.Name = "nodepath";
    argument.Value = path;

    final Object[] arguments = new Object[1];
    arguments[0] = argument;

    // create the view
    return provider.createInstanceWithArguments(
        "com.sun.star.configuration.ConfigurationUpdateAccess", arguments);
  }

  private boolean disableOpengl(final XComponentContext officeContext) throws OfficeException {

    // See configuration registry for more options.
    // e.g: C:\Program Files\LibreOffice 5\share\registry\main.xcd

    try {

      // Create the view to the root element where UseOpenGL option lives
      final Object viewRoot =
          createConfigurationView(
              Lo.createInstanceMCF(
                  officeContext,
                  XMultiServiceFactory.class,
                  "com.sun.star.configuration.ConfigurationProvider"),
              "/org.openoffice.Office.Common");
      try {

        // Check if the OpenGL option is on
        final XHierarchicalPropertySet properties = Lo.qi(XHierarchicalPropertySet.class, viewRoot);

        final XHierarchicalPropertySetInfo propsInfo = properties.getHierarchicalPropertySetInfo();
        if (propsInfo.hasPropertyByHierarchicalName(PROPPATH_USE_OPENGL)) {
          final boolean useOpengl =
              (boolean) properties.getHierarchicalPropertyValue(PROPPATH_USE_OPENGL);
          LOGGER.info("Use OpenGL is set to {}", useOpengl);
          if (useOpengl) {
            properties.setHierarchicalPropertyValue(PROPPATH_USE_OPENGL, false);
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
      throw new OfficeException("Unable to check if the Use OpenGL option is on.", ex);
    }
  }
}
