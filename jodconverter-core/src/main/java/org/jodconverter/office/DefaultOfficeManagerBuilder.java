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

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

import org.jodconverter.process.AbstractProcessManager;
import org.jodconverter.process.ProcessManager;
import org.jodconverter.process.ProcessUtils;

/** Helper class used to creates ProcessPoolOfficeManager instances. */
public class DefaultOfficeManagerBuilder {

  private static final Logger logger = LoggerFactory.getLogger(DefaultOfficeManagerBuilder.class);
  private static final OfficeConnectionProtocol DEFAULT_CONNECTION_PROTOCOL =
      OfficeConnectionProtocol.SOCKET;

  // OfficeProcess
  private OfficeConnectionProtocol connectionProtocol = DEFAULT_CONNECTION_PROTOCOL;
  private String[] pipeNames;
  private int[] portNumbers;
  private File officeHome;
  private File workingDir;
  private ProcessManager processManager;
  private String[] runAsArgs;
  private File templateProfileDir;
  private boolean killExistingProcess = OfficeProcessConfig.DEFAULT_KILLING_EXISTING_PROCESS;

  // OfficeManager
  private long retryTimeout = OfficeProcessManagerConfig.DEFAULT_TIMEOUT;
  private long retryInterval = OfficeProcessManagerConfig.DEFAULT_INTERVAL;

  // OfficeManagerPoolEntry
  private long taskExecutionTimeout = OfficeManagerPoolEntryConfig.DEFAULT_TASK_EXECUTION_TIMEOUT;
  private int maxTasksPerProcess = OfficeManagerPoolEntryConfig.DEFAULT_MAX_TASK_PER_PROCESS;

  // OfficeManagerPool
  private long taskQueueTimeout = OfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;

  // Assign default values for properties that are not set yet.
  private void setMissingDefaults() {

    if (officeHome == null) {
      officeHome = OfficeUtils.getDefaultOfficeHome();
    }

    if (workingDir == null) {
      workingDir = new File(System.getProperty("java.io.tmpdir"));
    }

    if (processManager == null) {
      processManager = ProcessUtils.getDefault();
    }
  }

  // Validate the provided configuration before building the manager.
  private void validateConfiguration() {

    // Validate the office directories
    OfficeUtils.validateOfficeHome(officeHome);
    OfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);
    OfficeUtils.validateOfficeWorkingDirectory(workingDir);

    if (retryInterval > OfficeProcessManagerConfig.MAX_RETRY_INTERVAL) {
      throw new IllegalStateException(
          "retryInterval cannot be greater than "
              + OfficeProcessManagerConfig.MAX_RETRY_INTERVAL
              + ", was: "
              + retryInterval);
    }
  }

  private UnoUrl[] buildUnoUrls() {

    int numInstances = 0;
    if (connectionProtocol == OfficeConnectionProtocol.SOCKET) {
      if (portNumbers == null) {
        portNumbers = new int[] {2002};
      }
      numInstances = portNumbers.length;
    } else {
      if (pipeNames == null) {
        pipeNames = new String[] {"office"};
      }
      numInstances = pipeNames.length;
    }

    final UnoUrl[] unoUrls = new UnoUrl[numInstances];
    for (int i = 0; i < numInstances; i++) {
      unoUrls[i] =
          (connectionProtocol == OfficeConnectionProtocol.PIPE)
              ? UnoUrlUtils.pipe(pipeNames[i])
              : UnoUrlUtils.socket(portNumbers[i]);
    }
    return unoUrls;
  }

  /**
   * Builds a {@code ProcessPoolOfficeManager} with the current configuration.
   *
   * @return the created OfficeManager
   */
  public OfficeManager build() {

    setMissingDefaults();
    validateConfiguration();
    final UnoUrl[] unoUrls = buildUnoUrls();

    //@formatter:off
    return new OfficeManagerPool(
        // OfficeProcess
        unoUrls,
        officeHome,
        workingDir,
        processManager,
        runAsArgs,
        templateProfileDir,
        killExistingProcess,
        // OfficeManager
        retryTimeout,
        retryInterval,
        // OfficeManagerPoolEntry
        taskExecutionTimeout,
        maxTasksPerProcess,
        // OfficeManagerPool
        taskQueueTimeout);
    //@formatter:on
  }

  /**
   * Sets the connection protocol.
   *
   * @param connectionProtocol the new protocol to set.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setConnectionProtocol(
      final OfficeConnectionProtocol connectionProtocol) {

    Validate.notNull(connectionProtocol);
    this.connectionProtocol = connectionProtocol;
    return this;
  }

  /**
   * Used for killing existing office process when an office process already exists for the same
   * connection string. If not set, it defaults to true
   *
   * @param killExistingProcess {@code true} to kill existing process when a new process must be
   *     created with the same command line, {@code false} otherwise.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setKillExistingProcess(final boolean killExistingProcess) {

    this.killExistingProcess = killExistingProcess;
    return this;
  }

  /**
   * Sets the maximum number of tasks an office process can execute before restarting.
   *
   * @param maxTasksPerProcess the new value to set.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setMaxTasksPerProcess(final int maxTasksPerProcess) {

    this.maxTasksPerProcess = maxTasksPerProcess;
    return this;
  }

  /**
   * Sets the office home directory.
   *
   * @param officeHome the new home directory to set.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setOfficeHome(final File officeHome) {

    Validate.notNull(officeHome);
    Validate.isTrue(officeHome.isDirectory(), "The office home must exist and be a directory");
    this.officeHome = officeHome;
    return this;
  }

  /**
   * Sets the home office directory.
   *
   * @param officeHome the new home directory to set.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setOfficeHome(final String officeHome) {

    Validate.notBlank(officeHome);
    return setOfficeHome(new File(officeHome));
  }

  /**
   * Sets the pipe name that will be use to communicate with office.
   *
   * @param pipeName the pipe name to use.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setPipeName(final String pipeName) {

    Validate.notBlank(pipeName);
    return setPipeNames(new String[] {pipeName});
  }

  /**
   * Sets the list of pipe names that will be use to communicate with office. An instance of office
   * will be launched for each pipe name.
   *
   * @param pipeNames the pipe names to use.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setPipeNames(final String[] pipeNames) {

    Validate.isTrue(
        pipeNames != null && pipeNames.length > 0, "The pipe name list must not be empty");
    this.pipeNames = ArrayUtils.clone(pipeNames);
    return this;
  }

  /**
   * Sets the port number that will be use to communicate with office.
   *
   * @param portNumber the port number to use.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setPortNumber(final int portNumber) {

    return setPortNumbers(new int[] {portNumber});
  }

  /**
   * Sets the list of port numbers that will be use to communicate with office. An instance of
   * office will be launched for each port number.
   *
   * @param portNumbers the port numbers to use.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setPortNumbers(final int... portNumbers) {

    Validate.isTrue(
        portNumbers != null && portNumbers.length > 0, "The port number list must not be empty");
    this.portNumbers = ArrayUtils.clone(portNumbers);
    return this;
  }

  /**
   * Provides a specific {@code ProcessManager} implementation.
   *
   * @param processManager the provided process manager.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setProcessManager(final ProcessManager processManager) {

    Validate.notNull(processManager);
    this.processManager = processManager;
    return this;
  }

  /**
   * Provides a custom {@code ProcessManager} implementation, which may not be included in the
   * standard JODConverter distribution.
   *
   * @param processManagerClass type of the provided process manager. The class must implement the
   *     {@code ProcessManager} interface, must be on the classpath (or more specifically accessible
   *     from the current classloader) and must have a default public constructor (no argument).
   * @return the updated configuration.
   * @see ProcessManager
   * @see AbstractProcessManager
   */
  public DefaultOfficeManagerBuilder setProcessManager(final String processManagerClass) {

    Validate.notBlank(processManagerClass);
    try {
      this.processManager = (ProcessManager) Class.forName(processManagerClass).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
      logger.warn(
          "Ignoring custom process manager '" + processManagerClass + "': " + ex.getMessage(), ex);
    }
    return this;
  }

  /**
   * Retry interval set in milliseconds. Used for waiting between office process call attempts
   * (start/terminate). If not set, it defaults to 0.25 secs.
   *
   * @param retryInterval the retry interval, in milliseconds.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setRetryInterval(final long retryInterval) {

    this.retryInterval = retryInterval;
    return this;
  }

  /**
   * Retry timeout set in milliseconds. Used for retrying office process calls (start/terminate). If
   * not set, it defaults to 2 minutes.
   *
   * @param retryTimeout the retry timeout, in milliseconds.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setRetryTimeout(final long retryTimeout) {

    this.retryTimeout = retryTimeout;
    return this;
  }

  /**
   * Sets the sudo arguments that will be used with unix commands.
   *
   * @param runAsArgs the sudo arguments for a unix os.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setRunAsArgs(final String... runAsArgs) {

    this.runAsArgs = ArrayUtils.clone(runAsArgs);
    return this;
  }

  /**
   * Sets the maximum time allowed to process a task. If the processing time of a task is longer
   * than this timeout, this task will be aborted and the next task is processed. Default is 120000
   * (2 minutes).
   *
   * @param taskExecutionTimeout the new timeout value.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setTaskExecutionTimeout(final long taskExecutionTimeout) {

    this.taskExecutionTimeout = taskExecutionTimeout;
    return this;
  }

  /**
   * The maximum living time of a task in the conversion queue. The task will be removed from the
   * queue if the waiting time is longer than this timeout. Default is 30000 (30 seconds).
   *
   * @param taskQueueTimeout the new timeout value.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setTaskQueueTimeout(final long taskQueueTimeout) {

    this.taskQueueTimeout = taskQueueTimeout;
    return this;
  }

  /**
   * Sets the directory to copy to the temporary office profile directories to be created.
   *
   * @param templateProfileDir the new template profile directory.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setTemplateProfileDir(final File templateProfileDir) {

    if (templateProfileDir != null) {
      Validate.isTrue(
          templateProfileDir.isDirectory(),
          "The template profile directory must exist and be a directory");
    }
    this.templateProfileDir = templateProfileDir;
    return this;
  }

  /**
   * Sets the directory where temporary office profiles will be created.
   *
   * <p>Defaults to the system temporary directory as specified by the <code>java.io.tmpdir</code>
   * system property.
   *
   * @param workingDir the new directory to set.
   * @return the updated configuration.
   */
  public DefaultOfficeManagerBuilder setWorkingDir(final File workingDir) {

    this.workingDir = workingDir;
    return this;
  }
}
