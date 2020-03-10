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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractOfficeManagerPool;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.process.ProcessManager;

/**
 * Default {@link org.jodconverter.core.office.OfficeManager} implementation that uses a pool of
 * office processes to execute conversion tasks.
 */
public final class LocalOfficeManager extends AbstractOfficeManagerPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeManager.class);

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link LocalOfficeManager} with default configuration.
   *
   * @return A {@link LocalOfficeManager} with default configuration.
   */
  @NonNull
  public static LocalOfficeManager make() {
    return builder().build();
  }

  /**
   * Creates a new {@link LocalOfficeManager} with default configuration. The created manager will
   * then be the unique instance of the {@link
   * org.jodconverter.core.office.InstalledOfficeManagerHolder} class. Note that if the {@code
   * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the owner
   * of this existing manager is responsible to stopped it.
   *
   * @return A {@link LocalOfficeManager} with default configuration.
   */
  @NonNull
  public static LocalOfficeManager install() {
    return builder().install().build();
  }

  private LocalOfficeManager(
      final List<OfficeUrl> officeUrls,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager,
      final List<String> runAsArgs,
      final File templateProfileDir,
      final Boolean killExistingProcess,
      final Long processTimeout,
      final Long processRetryInterval,
      final Long taskExecutionTimeout,
      final Integer maxTasksPerProcess,
      final Boolean disableOpengl,
      final Long taskQueueTimeout) {
    super(workingDir, officeUrls.size(), taskQueueTimeout);

    setEntries(
        officeUrls.stream()
            .map(
                officeUrl ->
                    new OfficeProcessManagerPoolEntry(
                        officeUrl,
                        officeHome,
                        workingDir,
                        processManager,
                        runAsArgs,
                        templateProfileDir,
                        killExistingProcess,
                        processTimeout,
                        processRetryInterval,
                        taskExecutionTimeout,
                        maxTasksPerProcess,
                        disableOpengl))
            .collect(Collectors.toList()));
  }

  /**
   * A builder for constructing a {@link LocalOfficeManager}.
   *
   * @see LocalOfficeManager
   */
  public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

    // The minimum value for the delay between each try when executing a process call
    // (start/terminate).
    private static final long MIN_PROCESS_RETRY_INTERVAL = 0L; // No delay.
    // The maximum value for the delay between each try when executing a process call
    // (start/terminate).
    private static final long MAX_PROCESS_RETRY_INTERVAL = 10_000L; // 10 sec.

    private List<String> pipeNames;
    private List<Integer> portNumbers;
    private File officeHome;
    private ProcessManager processManager;
    private List<String> runAsArgs;
    private File templateProfileDir;
    private boolean useDefaultOnInvalidTemplateProfileDir;
    private Boolean killExistingProcess;

    // OfficeProcessManager
    private Long processTimeout;
    private Long processRetryInterval;
    private Integer maxTasksPerProcess;
    private Boolean disableOpengl;

    // Private constructor so only LocalOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    @NonNull
    @Override
    public LocalOfficeManager build() {

      // Assign default values for properties that are not set yet.
      if (officeHome == null) {
        officeHome = LocalOfficeUtils.getDefaultOfficeHome();
      }

      if (workingDir == null) {
        workingDir = OfficeUtils.getDefaultWorkingDir();
      }

      if (processManager == null) {
        processManager = LocalOfficeUtils.findBestProcessManager();
      }

      // Validate the office directories
      LocalOfficeUtils.validateOfficeHome(officeHome);
      LocalOfficeUtils.validateOfficeWorkingDirectory(workingDir);
      if (useDefaultOnInvalidTemplateProfileDir) {
        try {
          LocalOfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);
        } catch (IllegalStateException ex) {
          // Use default
          templateProfileDir = null;
          LOGGER.warn("Falling back to default templateProfileDir. Cause: {}", ex.getMessage());
        }
      } else {
        LocalOfficeUtils.validateOfficeTemplateProfileDirectory(templateProfileDir);
      }

      // Build the office URLs
      final LocalOfficeManager manager =
          new LocalOfficeManager(
              LocalOfficeUtils.buildOfficeUrls(portNumbers, pipeNames),
              officeHome,
              workingDir,
              processManager,
              runAsArgs,
              templateProfileDir,
              killExistingProcess,
              processTimeout,
              processRetryInterval,
              taskExecutionTimeout,
              maxTasksPerProcess,
              disableOpengl,
              taskQueueTimeout);
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
    }

    /**
     * Specifies the pipe names that will be use to communicate with office. An instance of office
     * will be launched for each pipe name.
     *
     * @param pipeNames The pipe names to use.
     * @return This builder instance.
     */
    @NonNull
    public Builder pipeNames(@Nullable final String... pipeNames) {

      if (pipeNames != null && pipeNames.length != 0) {
        this.pipeNames = Arrays.asList(pipeNames);
      }
      return this;
    }

    /**
     * Specifies the port numbers that will be use to communicate with office. An instance of office
     * will be launched for each port number.
     *
     * @param portNumbers The port numbers to use.
     * @return This builder instance.
     */
    @NonNull
    public Builder portNumbers(final int... portNumbers) {

      if (portNumbers != null && portNumbers.length != 0) {
        this.portNumbers = Arrays.stream(portNumbers).boxed().collect(Collectors.toList());
      }
      return this;
    }

    /**
     * Specifies the office home directory (office installation).
     *
     * @param officeHome The new home directory to set.
     * @return This builder instance.
     */
    @NonNull
    public Builder officeHome(@Nullable final File officeHome) {

      this.officeHome = officeHome;
      return this;
    }

    /**
     * Specifies the office home directory (office installation).
     *
     * @param officeHome The new home directory to set.
     * @return This builder instance.
     */
    @NonNull
    public Builder officeHome(@Nullable final String officeHome) {

      return StringUtils.isBlank(officeHome) ? this : officeHome(new File(officeHome));
    }

    /**
     * Provides a specific {@link ProcessManager} implementation to be used when dealing with an
     * office process (retrieve PID, kill process).
     *
     * @param processManager The provided process manager.
     * @return This builder instance.
     */
    @NonNull
    public Builder processManager(@Nullable final ProcessManager processManager) {

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
     * @see org.jodconverter.local.process.ProcessManager
     * @see org.jodconverter.local.process.AbstractProcessManager
     */
    @NonNull
    public Builder processManager(@Nullable final String processManagerClass) {

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
    @NonNull
    public Builder runAsArgs(@Nullable final String... runAsArgs) {

      if (runAsArgs != null && runAsArgs.length != 0) {
        this.runAsArgs = Arrays.asList(runAsArgs);
      }
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    @NonNull
    public Builder templateProfileDir(@Nullable final File templateProfileDir) {

      this.templateProfileDir = templateProfileDir;
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    @NonNull
    public Builder templateProfileDir(@Nullable final String templateProfileDir) {

      return StringUtils.isBlank(templateProfileDir)
          ? this
          : templateProfileDir(new File(templateProfileDir));
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created. If
     * the given templateProfileDir is not valid, it will be ignored and the default behavior will
     * be applied.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    @NonNull
    public Builder templateProfileDirOrDefault(@Nullable final File templateProfileDir) {

      if (templateProfileDir != null) {
        this.useDefaultOnInvalidTemplateProfileDir = true;
      }
      this.templateProfileDir = templateProfileDir;
      return this;
    }

    /**
     * Specifies the directory to copy to the temporary office profile directories to be created. If
     * the given templateProfileDir is not valid, it will be ignored and the default behavior will
     * be applied.
     *
     * @param templateProfileDir The new template profile directory.
     * @return This builder instance.
     */
    @NonNull
    public Builder templateProfileDirOrDefault(@Nullable final String templateProfileDir) {

      return StringUtils.isBlank(templateProfileDir)
          ? this
          : templateProfileDirOrDefault(new File(templateProfileDir));
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
    @NonNull
    public Builder killExistingProcess(@Nullable final Boolean killExistingProcess) {

      this.killExistingProcess = killExistingProcess;
      return this;
    }

    /**
     * Specifies the timeout, in milliseconds, when trying to execute an office process call
     * (start/terminate).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param processTimeout The process timeout, in milliseconds.
     * @return This builder instance.
     */
    @NonNull
    public Builder processTimeout(@Nullable final Long processTimeout) {

      if (processTimeout != null) {
        AssertUtils.isTrue(
            processTimeout >= 0,
            String.format("processTimeout %s must be greater than or equal to 0", processTimeout));
      }
      this.processTimeout = processTimeout;
      return this;
    }

    /**
     * Specifies the delay, in milliseconds, between each try when trying to execute an office
     * process call (start/terminate).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
     *
     * @param processRetryInterval The retry interval, in milliseconds.
     * @return This builder instance.
     */
    @NonNull
    public Builder processRetryInterval(@Nullable final Long processRetryInterval) {

      if (processRetryInterval != null) {
        AssertUtils.isTrue(
            processRetryInterval >= MIN_PROCESS_RETRY_INTERVAL
                && processRetryInterval <= MAX_PROCESS_RETRY_INTERVAL,
            String.format(
                "processRetryInterval %s must be in the inclusive range of %s to %s",
                processRetryInterval, MIN_PROCESS_RETRY_INTERVAL, MAX_PROCESS_RETRY_INTERVAL));
      }
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
    @NonNull
    public Builder maxTasksPerProcess(@Nullable final Integer maxTasksPerProcess) {

      if (maxTasksPerProcess != null) {
        AssertUtils.isTrue(
            maxTasksPerProcess >= 1,
            String.format("maxTasksPerProcess %s must be greater than 0", maxTasksPerProcess));
      }
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
    @NonNull
    public Builder disableOpengl(@Nullable final Boolean disableOpengl) {

      this.disableOpengl = disableOpengl;
      return this;
    }
  }
}
