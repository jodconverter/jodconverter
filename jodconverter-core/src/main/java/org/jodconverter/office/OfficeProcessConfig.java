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

import com.sun.star.lib.uno.helper.UnoUrl;

import org.jodconverter.process.ProcessManager;

/**
 * This class holds the configuration of a {@code OfficeProcess}.
 *
 * @see OfficeProcess
 */
class OfficeProcessConfig {

  public static final boolean DEFAULT_KILLING_EXISTING_PROCESS = true;

  private final UnoUrl unoUrl;
  private File officeHome;
  private File workingDir;
  private ProcessManager processManager;
  private String[] runAsArgs;
  private File templateProfileDir;
  private boolean killExistingProcess = DEFAULT_KILLING_EXISTING_PROCESS;

  /**
   * Creates configuration for the specified URL and with default values.
   *
   * @param unoUrl the UNO URL for the configuration.
   */
  public OfficeProcessConfig(final UnoUrl unoUrl) {
    this(unoUrl, null, null, null);
  }

  /**
   * Creates configuration for the specified URL and with the specified values.
   *
   * @param unoUrl the UNO URL for the configuration.
   * @param officeHome home directory of the office installation.
   * @param workingDir working directory to set to office.
   * @param processManager process manager to use to deal with created processes.
   */
  public OfficeProcessConfig(
      final UnoUrl unoUrl,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager) {

    this.unoUrl = unoUrl;
    this.officeHome = officeHome == null ? OfficeUtils.getDefaultOfficeHome() : officeHome;
    this.workingDir =
        workingDir == null ? new File(System.getProperty("java.io.tmpdir")) : workingDir;
    this.processManager =
        processManager == null ? OfficeUtils.findBestProcessManager() : processManager;
    this.killExistingProcess = true;
  }

  /**
   * Gets the office home directory.
   *
   * @return a file instance to the office home directory.
   */
  public File getOfficeHome() {
    return officeHome;
  }

  /**
   * Gets the {@code ProcessManager} implementation used with this office process.
   *
   * @return the provided process manager.
   */
  public ProcessManager getProcessManager() {
    return processManager;
  }

  /**
   * Gets the sudo arguments that will be used with unix commands.
   *
   * @return the sudo arguments.
   */
  public String[] getRunAsArgs() {
    return ArrayUtils.clone(runAsArgs);
  }

  /**
   * Gets the directory to copy to the temporary office profile directories to be created.
   *
   * @return the template profile directory.
   */
  public File getTemplateProfileDir() {
    return templateProfileDir;
  }

  /**
   * Gets the UNO URL of this office process.
   *
   * @return the UNO URL.
   */
  public UnoUrl getUnoUrl() {
    return unoUrl;
  }

  /**
   * Gets the directory where temporary office profiles will be created.
   *
   * <p>Defaults to the system temporary directory as specified by the <code>java.io.tmpdir</code>
   * system property.
   *
   * @return the working directory.
   */
  public File getWorkingDir() {
    return workingDir;
  }

  /**
   * Gets whether we must kill existing office process when an office process already exists for the
   * same connection string. If not set, it defaults to true.
   *
   * @return {@code true} to kill existing process, {@code false} otherwise.
   */
  public boolean isKillExistingProcess() {
    return killExistingProcess;
  }

  /**
   * Sets whether we must kill existing office process when an office process already exists for the
   * same connection string.
   *
   * @param killExistingProcess {@code true} to kill existing process, {@code false} otherwise.
   */
  public void setKillExistingProcess(final boolean killExistingProcess) {
    this.killExistingProcess = killExistingProcess;
  }

  /**
   * Sets the office home directory.
   *
   * @param officeHome the new home directory to set.
   */
  public void setOfficeHome(final File officeHome) {
    this.officeHome = officeHome;
  }

  /**
   * Sets the {@code ProcessManager} implementation to use with this office process.
   *
   * @param processManager the process manager to use.
   */
  public void setProcessManager(final ProcessManager processManager) {
    this.processManager = processManager;
  }

  /**
   * Sets the sudo arguments that will be used with unix commands.
   *
   * @param runAsArgs the sudo arguments for a unix os.
   */
  public void setRunAsArgs(final String... runAsArgs) {
    this.runAsArgs = ArrayUtils.clone(runAsArgs);
  }

  /**
   * Sets the directory to copy to the temporary office profile directories to be created.
   *
   * @param templateProfileDir the new template profile directory.
   */
  public void setTemplateProfileDir(final File templateProfileDir) {
    this.templateProfileDir = templateProfileDir;
  }

  /**
   * Sets the directory where temporary office profiles will be created.
   *
   * @param workingDir the new working directory.
   */
  public void setWorkingDir(final File workingDir) {
    this.workingDir = workingDir;
  }
}
