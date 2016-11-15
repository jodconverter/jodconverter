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

import static org.artofsolving.jodconverter.process.ProcessManager.PID_NOT_FOUND;
import static org.artofsolving.jodconverter.process.ProcessManager.PID_UNKNOWN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.artofsolving.jodconverter.process.ProcessManager;
import org.artofsolving.jodconverter.process.ProcessQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

/**
 * Represents an office process.
 */
class OfficeProcess {

    private static final Logger logger = LoggerFactory.getLogger(OfficeProcess.class);

    private Process process;
    private long pid = PID_UNKNOWN;
    private final File officeHome;
    private final UnoUrl unoUrl;
    private final String[] runAsArgs;
    private final File templateProfileDir;
    private final File instanceProfileDir;
    private final ProcessManager processManager;
    private final boolean killExistingProcess;

    /**
     * Constructs a new instance of an office process class with the specified settings.
     * 
     * @param settings
     *            the office process settings.
     */
    public OfficeProcess(File officeHome, UnoUrl unoUrl, String[] runAsArgs, File templateProfileDir, File workingDir, ProcessManager processManager, boolean killExistingProcess) {

        this.officeHome = officeHome;
        this.unoUrl = unoUrl;
        this.runAsArgs = runAsArgs;
        this.templateProfileDir = templateProfileDir;
        this.processManager = processManager;
        this.killExistingProcess = killExistingProcess;
        this.instanceProfileDir = getInstanceProfileDir(workingDir, unoUrl);
    }

    /**
     * Deletes the profile directory of the office process.
     */
    public void deleteProfileDir() {

        if (templateProfileDir != null) {
            try {
                FileUtils.deleteDirectory(instanceProfileDir);
            } catch (IOException ex) {
                File oldProfileDir = new File(instanceProfileDir.getParentFile(), instanceProfileDir.getName() + ".old." + System.currentTimeMillis());
                if (instanceProfileDir.renameTo(oldProfileDir)) {
                    logger.warn("Could not delete profileDir: {}; renamed it to {}", ex.getMessage(), oldProfileDir);
                } else {
                    logger.error("Could not delete profileDir: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Kills the office process.
     * 
     * @param retryInterval
     *            internal between each exit code retrieval attempt.
     * @param retryTimeout
     *            timeout after which we won't try again to retrieve the exit code.
     * @return the exit code.
     * @throws IOException
     *             if we are unable to kill the process.
     * @throws RetryTimeoutException
     *             if we are unable to get the exit code of the process.
     */
    public int forciblyTerminate(long retryInterval, long retryTimeout) throws IOException, RetryTimeoutException {

        logger.info("Trying to forcibly terminate process: '{}'{}", unoUrl.getConnectionParametersAsString(), pid != PID_UNKNOWN ? " (pid " + pid + ")" : "");
        processManager.kill(process, pid);
        return getExitCode(retryInterval, retryTimeout);
    }

    /**
     * Gets the exit code of the office process.
     * 
     * @return the exit value of the process. The value 0 indicates normal termination.
     */
    public Integer getExitCode() {

        try {
            return process.exitValue();
        } catch (IllegalThreadStateException exception) {
            return null;
        }
    }

    /**
     * Gets the exit code of the office process. We will try to get the exit code until we succeed
     * or that the specified timeout is reached.
     * 
     * @param retryInterval
     *            internal between each retrieval attempt.
     * @param retryTimeout
     *            timeout after which we won't try again to retrieve the exit code.
     * @return the exit value of the process. The value 0 indicates normal termination.
     */
    public int getExitCode(long retryInterval, long retryTimeout) throws RetryTimeoutException {

        try {
            ExitCodeRetryable retryable = new ExitCodeRetryable(process);
            retryable.execute(retryInterval, retryTimeout);
            return retryable.getExitCode();
        } catch (RetryTimeoutException retryTimeoutException) {
            throw retryTimeoutException;
        } catch (Exception exception) {
            throw new OfficeException("could not get process exit code", exception);
        }
    }

    /**
     * Gets the profile directory of the office process.
     * 
     * @param workingDir
     *            the working direcory
     * @param unoUrl
     *            the uno URL for the process.
     * @return the profile direcotry instance.
     */
    private File getInstanceProfileDir(File workingDir, UnoUrl unoUrl) {

        String dirName = ".jodconverter_" + unoUrl.getConnectionAndParametersAsString().replace(',', '_').replace('=', '-');
        return new File(workingDir, dirName);
    }

    /**
     * Gets whether the office process is running.
     * 
     * @return {@code true} is the office process is running; {@code false otherwise}.
     */
    public boolean isRunning() {

        if (process == null) {
            return false;
        }
        return getExitCode() == null;
    }

    /**
     * Prepare the profile directory of the office process.
     * 
     * @throws OfficeException
     *             if
     */
    private void prepareInstanceProfileDir() throws OfficeException {

        if (instanceProfileDir.exists()) {
            logger.warn("Profile dir '{}' already exists; deleting", instanceProfileDir);
            deleteProfileDir();
        }
        if (templateProfileDir != null) {
            try {
                FileUtils.copyDirectory(templateProfileDir, instanceProfileDir);
            } catch (IOException ioException) {
                throw new OfficeException("failed to create profileDir", ioException);
            }
        }
    }

    /**
     * Starts the office process.
     * 
     * @throws IOException
     *             if an IO error occurs.
     */
    public void start() throws IOException {

        start(false);
    }

    /**
     * Starts the office process.
     * 
     * @param restart
     *            Indicates whether it is a fresh start of a restart after a failure.
     * @throws IOException
     *             if an IO error occurs.
     */
    public void start(boolean restart) throws IOException {

        String acceptString = unoUrl.getConnectionAndParametersAsString() + ";" + unoUrl.getProtocolAndParametersAsString() + ";" + unoUrl.getRootOid();
        ProcessQuery processQuery = new ProcessQuery("soffice", acceptString);
        long existingPid = processManager.findPid(processQuery);

        // Kill the process is already running and the kill switch is on
        if (!(existingPid == PID_NOT_FOUND || existingPid == PID_UNKNOWN) && killExistingProcess) {
            logger.warn("A process with acceptString '{}' is already running; pid {}", acceptString, existingPid);
            processManager.kill(null, existingPid);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            existingPid = processManager.findPid(processQuery);
        }
        if (!(existingPid == PID_NOT_FOUND || existingPid == PID_UNKNOWN)) {
            throw new IllegalStateException(String.format("a process with connectString '%s' is already running; pid %d", acceptString, existingPid));
        }

        // Prepare the instance directory only on first start
        if (!restart) {
            prepareInstanceProfileDir();
        }

        // Create the command used to launch the office process
        List<String> command = new ArrayList<String>();
        File executable = OfficeUtils.getOfficeExecutable(officeHome);
        if (runAsArgs != null) {
            command.addAll(Arrays.asList(runAsArgs));
        }
        command.add(executable.getAbsolutePath());
        command.add("-accept=" + acceptString);
        command.add("-env:UserInstallation=" + OfficeUtils.toUrl(instanceProfileDir));
        command.add("-headless");
        command.add("-nocrashreport");
        command.add("-nodefault");
        command.add("-nofirststartwizard");
        command.add("-nolockcheck");
        command.add("-nologo");
        command.add("-norestore");
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Start the process.
        logger.info("Starting process with acceptString '{}' and profileDir '{}'", unoUrl.getConnectionAndParametersAsString(), instanceProfileDir);
        process = processBuilder.start();
        pid = processManager.findPid(processQuery);
        if (pid == PID_NOT_FOUND) {
            throw new IllegalStateException(String.format("process with acceptString '%s' started but its pid could not be found", acceptString));
        }
        logger.info("Started process{}", pid != PID_UNKNOWN ? "; pid = " + pid : "");
    }
}
