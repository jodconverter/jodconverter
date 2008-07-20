//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2008 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, you can find it online
// at http://www.gnu.org/licenses/lgpl-2.1.html.
//
package net.sf.jodconverter.office;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.sf.jodconverter.util.NamedThreadFactory;
import net.sf.jodconverter.util.RetryTimeoutException;
import net.sf.jodconverter.util.Retryable;
import net.sf.jodconverter.util.TemporaryException;

import org.apache.commons.io.FileUtils;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.DisposedException;

public class ManagedOfficeProcess {

    private static final ThreadFactory THREAD_FACTORY = new NamedThreadFactory("ManagedOfficeProcessThread");

    private static final long RETRY_INTERVAL = 250;
    private static final long RETRY_TIMEOUT = 30 * 1000;

    private final OfficeProcess process;
    private final OfficeConnection connection;

    private File profileDir;

    private ExecutorService executor = Executors.newSingleThreadExecutor(THREAD_FACTORY);

    private final Logger logger = Logger.getLogger(getClass().getName());

    public ManagedOfficeProcess(File officeHome, File templateProfileDir, String acceptString) throws OfficeException {
        try {
            profileDir = createProfileDir(templateProfileDir, acceptString);
        } catch (IOException ioException) {
            throw new OfficeException("could not create profile dir: " + profileDir, ioException);
        }
        process = new OfficeProcess(officeHome, acceptString, templateProfileDir);
        connection = new OfficeConnection(acceptString);
    }

    private File createProfileDir(File templateProfileDir, String acceptString) throws IOException {
        if (!templateProfileDir.exists()) {
            throw new IllegalArgumentException("template profile dir does not exist: " + templateProfileDir);
        }
        File profileDir = new File(System.getProperty("java.io.tmpdir"), ".jodconverter_" + acceptString);
        if (profileDir.exists()) {
            logger.warning(String.format("profile dir already exists; recreating: '%s'", profileDir));
            FileUtils.deleteDirectory(profileDir);
        }
        FileUtils.copyDirectory(templateProfileDir, profileDir);
        return profileDir;
    }

    public OfficeConnection getConnection() {
        return connection;
    }

    public void startAndWait() throws OfficeException {
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStartProcessAndConnect();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new OfficeException("failed to start and connect", exception);
        }
    }

    public void stopAndWait() throws OfficeException {
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStopProcess();
                doDeleteProfileDir();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new OfficeException("failed to start and connect", exception);
        }
    }

    public void restartAndWait() {
        Future<?> future = executor.submit(new Runnable() {
           public void run() {
               doStopProcess();
               doDeleteProfileDir();
               doStartProcessAndConnect();
            } 
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new OfficeException("failed to restart", exception);
        }
    }

    public void restartDueToTaskTimeout() {
        executor.execute(new Runnable() {
           public void run() {
                doTerminateProcess();
                // will cause unexpected disconnection and subsequent restart
                doDeleteProfileDir();
            } 
        });
    }

    public void restartDueToLostConnection() {
        executor.execute(new Runnable() {
            public void run() {
                doEnsureProcessExited();
                doDeleteProfileDir();
                doStartProcessAndConnect();
            } 
         });
    }

    private void doStartProcessAndConnect() throws OfficeException {
        try {
            process.start();
            new Retryable() {
                protected void attempt() throws TemporaryException, Exception {
                    try {
                        connection.connect();
                    } catch (ConnectException connectException) {
                        throw new TemporaryException(connectException);
                    }
                }
            }.execute(RETRY_INTERVAL, RETRY_TIMEOUT);
        } catch (Exception exception) {
            throw new OfficeException("could not establish connection", exception);
        }
    }

    private void doStopProcess() {
        try {
            XDesktop desktop = UnoUtils.cast(XDesktop.class, connection.getService(UnoUtils.SERVICE_DESKTOP));
            desktop.terminate();
        } catch (DisposedException disposedException) {
            // expected
        }
        doEnsureProcessExited();
    }

    private void doDeleteProfileDir() {
        if (profileDir != null) {
            try {
                FileUtils.deleteDirectory(profileDir);
            } catch (IOException ioException) {
                logger.warning(ioException.getMessage());
            }
        }
    }

    private void doEnsureProcessExited() throws OfficeException {
        try {
            int exitCode = process.getExitCode(RETRY_INTERVAL, RETRY_TIMEOUT);
            logger.info("process exited with code " + exitCode);
        } catch (RetryTimeoutException retryTimeoutException) {
            doTerminateProcess();
        }
    }

    private void doTerminateProcess() {
        try {
            int exitCode = process.forciblyTerminate(RETRY_INTERVAL, RETRY_TIMEOUT);
            logger.info("process forcibly terminated with code " + exitCode);
        } catch (RetryTimeoutException retryTimeoutException2) {
            throw new OfficeException("could not terminate process", retryTimeoutException2);
        }
    }

}
