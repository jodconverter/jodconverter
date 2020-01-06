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

package org.jodconverter.office;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;

import org.jodconverter.process.ProcessManager;

/**
 * This class holds the configuration of an {@link OfficeProcess}.
 *
 * @see OfficeProcess
 */
class OfficeProcessConfig {

  /**
   * The default behavior when we want to start an office process and a process with the same URL
   * already exists.
   */
  public static final boolean DEFAULT_KILL_EXISTING_PROCESS = true;

  private File officeHome;
  private File workingDir;
  private ProcessManager processManager;
  private String[] runAsArgs;
  private File templateProfileDir;
  private boolean killExistingProcess = DEFAULT_KILL_EXISTING_PROCESS;

  /** Creates configuration with default values. */
  public OfficeProcessConfig() {
    this(null, null, null);
  }

  /**
   * Creates configuration with the specified values.
   *
   * @param officeHome The home directory of the office installation.
   * @param workingDir The working directory to set to office.
   * @param processManager The process manager to use to deal with created processes.
   */
  public OfficeProcessConfig(
      final File officeHome, final File workingDir, final ProcessManager processManager) {

    this.officeHome = officeHome == null ? LocalOfficeUtils.getDefaultOfficeHome() : officeHome;
    this.workingDir =
        workingDir == null ? new File(System.getProperty("java.io.tmpdir")) : workingDir;
    this.processManager =
        processManager == null ? LocalOfficeUtils.findBestProcessManager() : processManager;
  }

  /**
   * Gets the office home directory (office installation).
   *
   * @return A file instance to the office home directory.
   */
  public File getOfficeHome() {
    return officeHome;
  }

  /**
   * Gets the {@link ProcessManager} implementation to be used when dealing with an office process
   * (retrieve PID, kill process).
   *
   * @return The provided process manager.
   */
  public ProcessManager getProcessManager() {
    return processManager;
  }

  /**
   * Gets the sudo arguments that will be used with unix commands.
   *
   * @return The sudo arguments.
   */
  public String[] getRunAsArgs() {
    return ArrayUtils.clone(runAsArgs);
  }

  /**
   * Gets the directory where temporary office profile directories will be created. An office
   * profile directory is created per office process launched.
   *
   * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
   * java.io.tmpdir</code> system property.
   *
   * @return The working directory.
   */
  public File getWorkingDir() {
    return workingDir;
  }

  /**
   * Gets the directory to copy to the temporary office profile directories to be created.
   *
   * @return The template profile directory.
   */
  public File getTemplateProfileDir() {
    return templateProfileDir;
  }

  /**
   * Gets whether an existing office process is killed when starting a new office process for the
   * same connection string.
   *
   * <p>&nbsp; <b><i>Default</i></b>: true
   *
   * @return {@code true} to kill existing process, {@code false} otherwise.
   */
  public boolean isKillExistingProcess() {
    return killExistingProcess;
  }

  /**
   * Sets the office home directory.
   *
   * @param officeHome The new home directory to set.
   */
  public void setOfficeHome(final File officeHome) {
    this.officeHome = officeHome;
  }

  /**
   * Sets the {@code ProcessManager} implementation to use with this office process.
   *
   * @param processManager The process manager to use.
   */
  public void setProcessManager(final ProcessManager processManager) {
    this.processManager = processManager;
  }

  /**
   * Sets the directory where temporary office profile directories will be created. An office
   * profile directory is created per office process launched.
   *
   * @param workingDir The new working directory.
   */
  public void setWorkingDir(final File workingDir) {
    this.workingDir = workingDir;
  }

  /**
   * Sets the sudo arguments that will be used with unix commands.
   *
   * @param runAsArgs The sudo arguments for a unix os.
   */
  public void setRunAsArgs(final String... runAsArgs) {
    this.runAsArgs = ArrayUtils.clone(runAsArgs);
  }

  /**
   * Sets the directory to copy to the temporary office profile directories to be created.
   *
   * @param templateProfileDir The new template profile directory.
   */
  public void setTemplateProfileDir(final File templateProfileDir) {
    this.templateProfileDir = templateProfileDir;
  }

  /**
   * Sets whether an existing office process is killed when starting a new office process for the
   * same connection string.
   *
   * @param killExistingProcess {@code true} to kill existing process, {@code false} otherwise.
   */
  public void setKillExistingProcess(final boolean killExistingProcess) {
    this.killExistingProcess = killExistingProcess;
  }
}
