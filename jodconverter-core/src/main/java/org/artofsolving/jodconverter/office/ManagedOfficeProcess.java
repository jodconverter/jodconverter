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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.DisposedException;

/**
 * A ManagedOfficeProcess is responsible to manage an office process and the connection (bridge) to
 * this office process.
 * 
 * @see OfficeProcess
 * @see OfficeConnection
 */
class ManagedOfficeProcess {

    private static final Logger logger = LoggerFactory.getLogger(ManagedOfficeProcess.class);

    private final OfficeProcess process;
    private final OfficeConnection connection;
    private final ManagedOfficeProcessSettings settings;
    private final ExecutorService executor;

    /**
     * Creates a new instance of the class with the specified settings.
     * 
     * @param settings
     *            the managed office process settings.
     */
    public ManagedOfficeProcess(ManagedOfficeProcessSettings settings) {

        this.settings = settings;
        process = new OfficeProcess(settings.getOfficeHome(), settings.getUnoUrl(), settings.getRunAsArgs(), settings.getTemplateProfileDir(), settings.getWorkingDir(), settings.getProcessManager(), settings.isKillExistingProcess());
        connection = new OfficeConnection(settings.getUnoUrl());
        executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OfficeProcessThread"));
    }

    /**
     * Ensures that the process exited.
     * 
     * @throws OfficeException
     *             if an exception occurs.
     */
    private void doEnsureProcessExited() throws OfficeException {
        logger.trace("> doEnsureProcessExited");

        try {
            int exitCode = process.getExitCode(settings.getRetryInterval(), settings.getRetryTimeout());
            logger.info("process exited with code " + exitCode);
        } catch (RetryTimeoutException retryTimeoutException) {
            doTerminateProcess();
        } finally {
            process.deleteProfileDir();
            logger.trace("< doEnsureProcessExited");
        }
    }

    /**
     * Starts the office process managed by this class and connect to the process.
     */
    private void doStartProcessAndConnect() throws OfficeException {
        logger.trace("> doStartProcessAndConnect");

        try {
            process.start();
            new ConnectRetryable(process, connection).execute(settings.getRetryInterval(), settings.getRetryTimeout());
        } catch (Exception exception) {
            throw new OfficeException("Could not establish connection", exception);
        } finally {
            logger.trace("< doStartProcessAndConnect");
        }
    }

    /**
     * Stops the office process managed by this class.
     */
    private void doStopProcess() {
        logger.trace("> doStopProcess");

        try {
            connection.getDesktop().terminate();
        } catch (DisposedException disposedException) {
            // expected
        } catch (Exception exception) {
            // in case we can't get hold of the desktop
            doTerminateProcess();
        } finally {
            doEnsureProcessExited();
            logger.trace("< doStopProcess");
        }
    }

    /**
     * Ensures that the process exited.
     * 
     * @throws OfficeException
     *             if an exception occurs.
     */
    private void doTerminateProcess() throws OfficeException {
        logger.trace("> doTerminateProcess");

        try {
            int exitCode = process.forciblyTerminate(settings.getRetryInterval(), settings.getRetryTimeout());
            logger.info("process forcibly terminated with code " + exitCode);
        } catch (Exception exception) {
            throw new OfficeException("could not terminate process", exception);
        } finally {
            logger.trace("< doTerminateProcess");
        }
    }

    /**
     * Gets the connection of this managed office process.
     * 
     * @return the {@link OfficeConnection} of this managed office process.
     */
    public OfficeConnection getConnection() {
        return connection;
    }

    /**
     * Gets whether the connection to the office instance is opened.
     */
    public boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * Restarts an office process and wait until we are connected to the retarted process.
     * 
     * @throws OfficeException
     *             if we are not able to restart the office process.
     */
    public void restartAndWait() {
        logger.trace("> restartAndWait");

        logger.info("Restarting and waiting...");
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStopProcess();
                doStartProcessAndConnect();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new OfficeException("Failed to restart", exception);
        } finally {
            logger.trace("< restartAndWait");
        }
    }

    /**
     * Restarts the office process when the connection is lost.
     */
    public void restartDueToLostConnection() {
        logger.trace("> restartDueToLostConnection");

        logger.info("Restarting due to lost connection...");
        executor.execute(new Runnable() {
            public void run() {
                try {
                    doEnsureProcessExited();
                    doStartProcessAndConnect();
                } catch (OfficeException officeException) {
                    logger.error("Could not restart process", officeException);
                } finally {
                    logger.trace("< restartDueToLostConnection");
                }
            }
        });
    }

    /**
     * Restarts the office process when there is a timeout while executing a task.
     */
    public void restartDueToTaskTimeout() {
        logger.trace("> restartDueToTaskTimeout");

        logger.info("Restarting due to task timeout connection...");
        executor.execute(new Runnable() {
            public void run() {
                try {
                    doTerminateProcess();
                    // will cause unexpected disconnection and subsequent restart
                    //doTerminateProcess();
                    //doStartProcessAndConnect();
                    //} catch (OfficeException officeException) {
                    //    logger.error("Could not restart process", officeException);
                } finally {
                    logger.trace("< restartDueToTaskTimeout");
                }
            }
        });
    }

    /**
     * Starts an office process and wait until we are connected to the running process.
     * 
     * @throws OfficeException
     *             if we are not able to start and connect to the office process.
     */
    public void startAndWait() throws OfficeException {
        logger.trace("> startAndWait");

        logger.info("Starting and waiting...");
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStartProcessAndConnect();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new OfficeException("failed to start and connect", exception);
        } finally {
            logger.trace("< startAndWait");
        }
    }

    /**
     * Stop an office process and wait until the process is stopped.
     * 
     * @throws OfficeException
     *             if we are not able to stop the office process.
     */
    public void stopAndWait() throws OfficeException {
        logger.trace("> stopAndWait");

        logger.info("Stopping and waiting...");
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStopProcess();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new OfficeException("failed to stop and connect", exception);
        } finally {
            logger.trace("< stopAndWait");
        }
    }

}
