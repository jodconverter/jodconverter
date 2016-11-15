//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
// -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
// -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import java.io.File;

import org.apache.commons.lang3.Validate;
import org.artofsolving.jodconverter.process.ProcessManager;

import com.sun.star.lib.uno.helper.UnoUrl;

/**
 * Helper class used to creates {@code ProcessPoolOfficeManager} instances.
 */
public class DefaultOfficeManagerBuilder {

    private File officeHome;
    private OfficeConnectionProtocol connectionProtocol;
    private int[] portNumbers;
    private String[] pipeNames;
    private String[] runAsArgs;
    private File workingDir;
    private File templateProfileDir;
    private long taskQueueTimeout;
    private long taskExecutionTimeout;
    private long retryTimeout;
    private long retryInterval;
    private int maxTasksPerProcess;
    private boolean killExistingProcess;
    private ProcessManager processManager;

    /**
     * Creates a new instance of the class.
     */
    public DefaultOfficeManagerBuilder() {

        officeHome = OfficeUtils.getDefaultOfficeHome();
        connectionProtocol = OfficeConnectionProtocol.SOCKET;
        portNumbers = new int[] { 2002 };
        pipeNames = new String[] { "office" };
        workingDir = new File(System.getProperty("java.io.tmpdir"));
        taskQueueTimeout = ManagedOfficeProcessSettings.DEFAULT_TASK_QUEUE_TIMEOUT;
        taskExecutionTimeout = PooledOfficeManagerSettings.DEFAULT_TASK_EXECUTION_TIMEOUT;
        retryTimeout = ManagedOfficeProcessSettings.DEFAULT_RETRY_TIMEOUT;
        retryInterval = ManagedOfficeProcessSettings.DEFAULT_RETRY_INTERVAL;
        maxTasksPerProcess = PooledOfficeManagerSettings.DEFAULT_MAX_TASK_PER_PROCESS;
        killExistingProcess = true;
    }

    /**
     * Builds a {@code ProcessPoolOfficeManager} with the current configuration.
     * 
     * @return the created OfficeManager
     * @throws IllegalStateException
     */
    public OfficeManager build() throws IllegalStateException {

        if (officeHome == null) {
            throw new IllegalStateException("officeHome not set and could not be auto-detected");
        } else if (!officeHome.isDirectory()) {
            throw new IllegalStateException("officeHome doesn't exist or is not a directory: " + officeHome);
        } else if (!OfficeUtils.getOfficeExecutable(officeHome).isFile()) {
            throw new IllegalStateException("invalid officeHome: it doesn't contain soffice.bin: " + officeHome);
        }
        if (templateProfileDir != null && !isValidProfileDir(templateProfileDir)) {
            throw new IllegalStateException("templateProfileDir doesn't appear to contain a user profile: " + templateProfileDir);
        }
        if (!workingDir.isDirectory()) {
            throw new IllegalStateException("workingDir doesn't exist or is not a directory: " + workingDir);
        }

        if (processManager == null) {
            processManager = OfficeUtils.findBestProcessManager();
        }

        // TODO: Constants
        if (retryInterval > ManagedOfficeProcessSettings.MAX_RETRY_INTERVAL) {
            throw new IllegalStateException("retryInterval cannot be grater than " + ManagedOfficeProcessSettings.MAX_RETRY_INTERVAL + ", was: " + retryInterval);
        }

        int numInstances = connectionProtocol == OfficeConnectionProtocol.PIPE ? pipeNames.length : portNumbers.length;
        UnoUrl[] unoUrls = new UnoUrl[numInstances];
        for (int i = 0; i < numInstances; i++) {
            unoUrls[i] = (connectionProtocol == OfficeConnectionProtocol.PIPE) ? UnoUrlUtils.pipe(pipeNames[i]) : UnoUrlUtils.socket(portNumbers[i]);
        }
        //@formatter:off
        return new ProcessPoolOfficeManager(
                // ManageOfficeProcess
                unoUrls,
                officeHome,
                workingDir,
                processManager,
                runAsArgs,
                templateProfileDir,
                retryTimeout,
                retryInterval,
                killExistingProcess,
                // ProcessPoolOfficeManager
                taskQueueTimeout,
                // PooledOfficeManager
                taskExecutionTimeout,
                maxTasksPerProcess);
        //@formatter:on
    }

    private boolean isValidProfileDir(File profileDir) {
        return new File(profileDir, "user").isDirectory();
    }

    /**
     * Sets the connection protocol.
     * 
     * @param connectionProtocol
     *            the new protocol to set.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setConnectionProtocol(OfficeConnectionProtocol connectionProtocol) throws NullPointerException {

        Validate.notNull(connectionProtocol);
        this.connectionProtocol = connectionProtocol;
        return this;
    }

    /**
     * Used for killing existing office process when an office process already exists for the same
     * connection string. If not set, it defaults to true
     * 
     * @param killExistingProcess
     *            {@code true} to kill existing process when a new process must be created with the
     *            same command line, {@code false} otherwise.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setKillExistingProcess(boolean killExistingProcess) {

        this.killExistingProcess = killExistingProcess;
        return this;
    }

    /**
     * Sets the maximum number of tasks an office process can execute before restarting.
     * 
     * @param maxTasksPerProcess
     *            the new value to set.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setMaxTasksPerProcess(int maxTasksPerProcess) {

        this.maxTasksPerProcess = maxTasksPerProcess;
        return this;
    }

    /**
     * Sets the office home directory.
     * 
     * @param officeHome
     *            the new home directory to set.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setOfficeHome(File officeHome) {

        Validate.notNull(officeHome);
        Validate.isTrue(officeHome.isDirectory(), "The office home must exist and be a directory");
        this.officeHome = officeHome;
        return this;
    }

    /**
     * Sets the home office directory.
     * 
     * @param officeHome
     *            the new home directory to set.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setOfficeHome(String officeHome) {

        Validate.notNull(officeHome);
        return setOfficeHome(new File(officeHome));
    }

    /**
     * Sets the pipe name that will be use to communicate with office.
     * 
     * @param pipeName
     *            the pipe name to use.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setPipeName(String pipeName) throws NullPointerException {

        Validate.notNull(pipeName);
        return setPipeNames(new String[] { pipeName });
    }

    /**
     * Sets the list of pipe names that will be use to communicate with office. An instance of
     * office will be launched for each pipe name.
     * 
     * @param pipeNames
     *            the pipe names to use.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setPipeNames(String... pipeNames) throws NullPointerException, IllegalArgumentException {

        Validate.notNull(pipeNames);
        Validate.isTrue(portNumbers.length > 0, "The pipe name list must not be empty");
        this.pipeNames = pipeNames;
        return this;
    }

    /**
     * Sets the port number that will be use to communicate with office.
     * 
     * @param portNumber
     *            the port number to use.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setPortNumber(int portNumber) {

        return setPortNumbers(new int[] { portNumber });
    }

    /**
     * Sets the list of port numbers that will be use to communicate with office. An instance of
     * office will be launched for each port number.
     * 
     * @param portNumbers
     *            the port numbers to use.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setPortNumbers(int... portNumbers) throws NullPointerException, IllegalArgumentException {

        Validate.notNull(portNumbers);
        Validate.isTrue(portNumbers.length > 0, "The port number list must not be empty");
        this.portNumbers = portNumbers;
        return this;
    }

    /**
     * Provides a specific {@code ProcessManager} implementation.
     * 
     * @param processManager
     *            the provided process manager.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setProcessManager(ProcessManager processManager) {

        Validate.notNull(processManager);
        this.processManager = processManager;
        return this;
    }

    /**
     * Retry interval set in milliseconds. Used for waiting between office process call attempts
     * (start/terminate). If not set, it defaults to 0.25 secs.
     * 
     * @param retryInterval
     *            the retry interval, in milliseconds.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setRetryInterval(long retryInterval) {

        this.retryInterval = retryInterval;
        return this;
    }

    /**
     * Retry timeout set in milliseconds. Used for retrying office process calls (start/terminate).
     * If not set, it defaults to 2 minutes.
     * 
     * @param retryTimeout
     *            the retry timeout, in milliseconds.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setRetryTimeout(long retryTimeout) {

        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * Sets the sudo arguments that will be used with unix commands.
     * 
     * @param runAsArgs
     *            the sudo arguments for a unix os.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setRunAsArgs(String... runAsArgs) {

        this.runAsArgs = runAsArgs;
        return this;
    }

    /**
     * Sets the maximum time allowed to process a task. If the processing time of a task is longer
     * than this timeout, this task will be aborted and the next task is processed. Default is
     * 120000 (2 minutes).
     * 
     * @param taskExecutionTimeout
     *            the new timeout value.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setTaskExecutionTimeout(long taskExecutionTimeout) {

        this.taskExecutionTimeout = taskExecutionTimeout;
        return this;
    }

    /**
     * The maximum living time of a task in the conversion queue. The task will be removed from the
     * queue if the waiting time is longer than this timeout. Default is 30000 (30 seconds).
     * 
     * @param taskQueueTimeout
     *            the new timeout value.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setTaskQueueTimeout(long taskQueueTimeout) {

        this.taskQueueTimeout = taskQueueTimeout;
        return this;
    }

    /**
     * Sets the directory to copy to the temporary office profile directories to be created.
     * 
     * @param templateProfileDir
     *            the new template profile directory.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setTemplateProfileDir(File templateProfileDir) throws IllegalArgumentException {

        if (templateProfileDir != null) {
            Validate.isTrue(templateProfileDir.isDirectory(), "The template profile directory must exist and be a directory");
        }
        this.templateProfileDir = templateProfileDir;
        return this;
    }

    /**
     * Sets the directory where temporary office profiles will be created.
     * <p>
     * Defaults to the system temporary directory as specified by the <code>java.io.tmpdir</code>
     * system property.
     * 
     * @param workingDir
     *            the new directory to set.
     * @return the updated configuration.
     */
    public DefaultOfficeManagerBuilder setWorkingDir(File workingDir) {

        this.workingDir = workingDir;
        return this;
    }

}
