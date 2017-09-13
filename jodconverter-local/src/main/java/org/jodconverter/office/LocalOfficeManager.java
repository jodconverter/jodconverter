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
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import org.jodconverter.process.AbstractProcessManager;
import org.jodconverter.process.ProcessManager;

/**
 * Default {@link OfficeManager} implementation that uses a pool of office processes to execute
 * conversion tasks.
 */
public final class LocalOfficeManager extends AbstractOfficeManagerPool {

  private final OfficeUrl[] officeUrls;

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link LocalOfficeManager} with default configuration.
   *
   * @return A {@link LocalOfficeManager} with default configuration.
   */
  public static LocalOfficeManager make() {
    return builder().build();
  }

  /**
   * Creates a new {@link LocalOfficeManager} with default configuration. The created manager will
   * then be the unique instance of the {@link InstalledOfficeManagerHolder} class. Note that if the
   * {@code InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the
   * owner of this existing manager is responsible to stopped it.
   *
   * @return A {@link LocalOfficeManager} with default configuration.
   */
  public static LocalOfficeManager install() {
    return builder().install().build();
  }

  private LocalOfficeManager(
      final OfficeUrl[] officeUrls, final OfficeProcessManagerPoolConfig config) {
    super(officeUrls.length, config);

    this.officeUrls = Arrays.copyOf(officeUrls, officeUrls.length);
  }

  @Override
  protected OfficeProcessManagerPoolEntry[] createPoolEntries() {

    OfficeProcessManagerPoolEntry[] entries = new OfficeProcessManagerPoolEntry[officeUrls.length];
    for (int i = 0; i < officeUrls.length; i++) {
      entries[i] =
          new OfficeProcessManagerPoolEntry(officeUrls[i], (OfficeProcessManagerPoolConfig) config);
    }
    return entries;
  }

  /**
   * A builder for constructing a {@link LocalOfficeManager}.
   *
   * @see LocalOfficeManager
   */
  public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

    // OfficeProcess
    private String[] pipeNames;
    private int[] portNumbers;
    private File officeHome;
    private ProcessManager processManager;
    private String[] runAsArgs;
    private File templateProfileDir;
    private boolean killExistingProcess = OfficeProcessConfig.DEFAULT_KILL_EXISTING_PROCESS;

    // OfficeProcessManager
    private long processTimeout = OfficeProcessManagerConfig.DEFAULT_PROCESS_TIMEOUT;
    private long processRetryInterval = OfficeProcessManagerConfig.DEFAULT_PROCESS_RETRY_INTERVAL;
    private int maxTasksPerProcess = OfficeProcessManagerConfig.DEFAULT_MAX_TASKS_PER_PROCESS;
    private boolean disableOpengl = OfficeProcessManagerConfig.DEFAULT_DISABLE_OPENGL;

    // Private ctor so only LocalOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    public LocalOfficeManager build() {

      // Assign default values for properties that are not set yet.
      if (officeHome == null) {
        officeHome = LocalOfficeUtils.getDefaultOfficeHome();
      }

      if (workingDir == null) {
        workingDir = new File(System.getProperty("java.io.tmpdir"));
      }

      if (processManager == null) {
        processManager = LocalOfficeUtils.findBestProcessManager();
      }

      // Validate the office directories
      LocalOfficeUtils.validateOfficeHome(officeHome);
      LocalOfficeUtils.validateOfficeWorkingDirectory(workingDir);
      LocalOfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);

      // Build the office URLs
      final OfficeUrl[] officeUrls = LocalOfficeUtils.buildOfficeUrls(portNumbers, pipeNames);

      final OfficeProcessManagerPoolConfig config =
          new OfficeProcessManagerPoolConfig(officeHome, workingDir, processManager);
      config.setRunAsArgs(runAsArgs);
      config.setTemplateProfileDir(templateProfileDir);
      config.setKillExistingProcess(killExistingProcess);
      config.setProcessTimeout(processTimeout);
      config.setProcessRetryInterval(processRetryInterval);
      config.setMaxTasksPerProcess(maxTasksPerProcess);
      config.setDisableOpengl(disableOpengl);
      config.setTaskExecutionTimeout(taskExecutionTimeout);
      config.setTaskQueueTimeout(taskQueueTimeout);

      final LocalOfficeManager manager = new LocalOfficeManager(officeUrls, config);
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
    }

    //
    // OfficeProcess
    //

    /**
     * Specifies the pipe names that will be use to communicate with office. An instance of office
     * will be launched for each pipe name.
     *
     * @param pipeNames The pipe names to use.
     * @return This builder instance.
     */
    public Builder pipeNames(final String... pipeNames) {

      Validate.isTrue(
          pipeNames != null && pipeNames.length > 0, "The pipe name list must not be empty");
      this.pipeNames = ArrayUtils.clone(pipeNames);
      return this;
    }

    /**
     * Specifies the port numbers that will be use to communicate with office. An instance of office
     * will be launched for each port number.
     *
     * @param portNumbers The port numbers to use.
     * @return This builder instance.
     */
    public Builder portNumbers(final int... portNumbers) {

      Validate.isTrue(
          portNumbers != null && portNumbers.length > 0, "The port number list must not be empty");
      this.portNumbers = ArrayUtils.clone(portNumbers);
      return this;
    }

    /**
     * Specifies the office home directory (office installation).
     *
     * @param officeHome The new home directory to set.
     * @return This builder instance.
     */
    public Builder officeHome(final File officeHome) {

      this.officeHome = officeHome;
      return this;
    }

    /**
     * Specifies the office home directory (office installation).
     *
     * @param officeHome The new home directory to set.
     * @return This builder instance.
     */
    public Builder officeHome(final String officeHome) {

      return StringUtils.isBlank(officeHome) ? this : officeHome(new File(officeHome));
    }

    /**
     * Provides a specific {@link ProcessManager} implementation to be used when dealing with an
     * office process (retrieve PID, kill process).
     *
     * @param processManager The provided process manager.
     * @return This builder instance.
     */
    public Builder processManager(final ProcessManager processManager) {

      Validate.notNull(processManager, "The process manager must not be null");
      this.processManager = processManager;
      return this;
    }

    /**
     * Provides a custom {@link ProcessManager} implementation, which may not be included in the
     * standard JODConverter distribution.
     *
     * @param processManagerClass Type of the provided process manager. The class must implement the
     *     {@code ProcessManager} interface, must be on the classpath (or more specifically
     *     accessible from the current classloader) and must have a default public constructor (no
     *     argument).
     * @return This builder instance.
     * @see ProcessManager
     * @see AbstractProcessManager
     */
    public Builder processManager(final String processManagerClass) {

      try {
        return StringUtils.isBlank(processManagerClass)
            ? this
            : processManager((ProcessManager) Class.forName(processManagerClass).newInstance());
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
        throw new IllegalArgumentException(
            "Unable to create a Process manager from the specified class name: "
                + processManagerClass,
            ex);
      }
    }

    /**
     * Specifies the sudo arguments that will be used with unix commands.
     *
     * @param runAsArgs The sudo arguments for a unix os.
     * @return This builder instance.
     */
    public Builder runAsArgs(final String... runAsArgs) {

      Validate.isTrue(
          runAsArgs != null && runAsArgs.length > 0, "The runAs argument list must not be empty");
      this.runAsArgs = ArrayUtils.clone(runAsArgs);
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    public Builder templateProfileDir(final File templateProfileDir) {

      this.templateProfileDir = templateProfileDir;
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    public Builder templateProfileDir(final String templateProfileDir) {

      return StringUtils.isBlank(templateProfileDir)
          ? this
          : templateProfileDir(new File(templateProfileDir));
    }

    /**
     * Specifies whether an existing office process is killed when starting a new office process for
     * the same connection string.
     *
     * <p>&nbsp; <b><i>Default</i></b>: true
     *
     * @param killExistingProcess {@code true} to kill existing process when a new process must be
     *     created with the same connection string, {@code false} otherwise.
     * @return This builder instance.
     */
    public Builder killExistingProcess(final boolean killExistingProcess) {

      this.killExistingProcess = killExistingProcess;
      return this;
    }

    //
    // OfficeProcessManager
    //

    /**
     * Specifies the timeout, in milliseconds, when trying to execute an office process call
     * (start/terminate).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param processTimeout the process timeout, in milliseconds.
     * @return This builder instance.
     */
    public Builder processTimeout(final long processTimeout) {

      Validate.inclusiveBetween(
          0,
          Long.MAX_VALUE,
          processTimeout,
          String.format(
              "The processTimeout %s must be greater than or equal to 0", processTimeout));
      this.processTimeout = processTimeout;
      return this;
    }

    /**
     * Specifies the delay, in milliseconds, between each try when trying to execute an office
     * process call (start/terminate).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
     *
     * @param processRetryInterval the retry interval, in milliseconds.
     * @return This builder instance.
     */
    public Builder processRetryInterval(final long processRetryInterval) {

      Validate.inclusiveBetween(
          0,
          OfficeProcessManagerConfig.MAX_PROCESS_RETRY_INTERVAL,
          processRetryInterval,
          String.format(
              "The processRetryInterval %s must be in the inclusive range of %s to %s",
              processRetryInterval, 0, OfficeProcessManagerConfig.MAX_PROCESS_RETRY_INTERVAL));
      this.processRetryInterval = processRetryInterval;
      return this;
    }

    /**
     * Specifies the maximum number of tasks an office process can execute before restarting.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 200
     *
     * @param maxTasksPerProcess The new maximum number of tasks an office process can execute.
     * @return This builder instance.
     */
    public Builder maxTasksPerProcess(final int maxTasksPerProcess) {

      Validate.inclusiveBetween(
          1,
          Integer.MAX_VALUE,
          maxTasksPerProcess,
          String.format("The maxTasksPerProcess %s greater than 0", maxTasksPerProcess));
      this.maxTasksPerProcess = maxTasksPerProcess;
      return this;
    }

    /**
     * Specifies whether OpenGL must be disabled when starting a new office process. Nothing will be
     * done if OpenGL is already disabled according to the user profile used with the office
     * process. If the options is changed, then office must be restarted.
     *
     * <p>&nbsp; <b><i>Default</i></b>: false
     *
     * @param disableOpengl {@code true} to disable OpenGL, {@code false} otherwise.
     * @return This builder instance.
     */
    public Builder disableOpengl(final boolean disableOpengl) {

      this.disableOpengl = disableOpengl;
      return this;
    }
  }
}
