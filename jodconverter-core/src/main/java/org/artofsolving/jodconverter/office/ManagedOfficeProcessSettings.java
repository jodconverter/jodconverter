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

package org.artofsolving.jodconverter.office;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;

import com.sun.star.lib.uno.helper.UnoUrl;

import org.artofsolving.jodconverter.process.ProcessManager;
import org.artofsolving.jodconverter.process.PureJavaProcessManager;

/**
 * Settings for a {@code PooledOfficeManager}.
 *
 * @see PooledOfficeManager
 */
class ManagedOfficeProcessSettings {

  public static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30000L; // 30 seconds
  public static final long DEFAULT_RETRY_TIMEOUT = 120000L; // 2 minutes
  public static final long DEFAULT_RETRY_INTERVAL = 250L; // 0.25 secs.

  public static final long MAX_RETRY_INTERVAL = 10000L; //10 sec.

  private final UnoUrl unoUrl;
  private File officeHome;
  private File workingDir;
  private ProcessManager processManager;
  private String[] runAsArgs;
  private File templateProfileDir;
  private long retryTimeout;
  private long retryInterval;
  private boolean killExistingProcess;

  /**
   * Constructs new settings instance for the specified URL and with default values.
   *
   * @param unoUrl the UNO URL of the settingss.
   */
  public ManagedOfficeProcessSettings(final UnoUrl unoUrl) {
    this(
        unoUrl,
        OfficeUtils.getDefaultOfficeHome(),
        new File(System.getProperty("java.io.tmpdir")),
        new PureJavaProcessManager());
  }

  /** Constructs new settings instance for the specified URL and with the specified values. */
  public ManagedOfficeProcessSettings(
      final UnoUrl unoUrl,
      final File officeHome,
      final File workingDir,
      final ProcessManager processManager) {

    this.unoUrl = unoUrl;
    this.officeHome = officeHome;
    this.workingDir = workingDir;
    this.processManager = processManager;
    this.retryTimeout = DEFAULT_RETRY_TIMEOUT;
    this.retryInterval = DEFAULT_RETRY_INTERVAL;
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
   * Get the retry interval (milliseconds).Used for waiting between office process call tries
   * (start/terminate). Default is 250.
   *
   * @return the retry interval, in milliseconds.
   */
  public long getRetryInterval() {
    return retryInterval;
  }

  /**
   * Set the retry timeout (milliseconds).Used for retrying office process calls (start/terminate).
   * If not set, it defaults to 2 minutes.
   *
   * @return the retry timeout, in milliseconds.
   */
  public long getRetryTimeout() {
    return retryTimeout;
  }

  /**
   * Gets the sudo arguments that will be used with unix commands.
   *
   * @return the sudo arguments.
   */
  public String[] getRunAsArgs() {
    return runAsArgs;
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
   * Set the retry interval (milliseconds).Used for waiting between office process call tries
   * (start/terminate).
   *
   * @param retryInterval the retry interval, in milliseconds.
   */
  public void setRetryInterval(final long retryInterval) {
    this.retryInterval = retryInterval;
  }

  /**
   * Set the retry timeout (milliseconds). Used for retrying office process calls (start/terminate).
   *
   * @param retryTimeout the retry timeout, in milliseconds.
   */
  public void setRetryTimeout(final long retryTimeout) {
    this.retryTimeout = retryTimeout;
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
