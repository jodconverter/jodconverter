//
// JODConverter - Java OpenDocument Converter
// Copyright 2009 Art of Solving Ltd
// Copyright 2004-2009 Mirko Nasato
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package org.artofsolving.jodconverter.office;

import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.DisposedException;

class ManagedOfficeProcess {

    private final ManagedOfficeProcessSettings settings;

    private final OfficeProcess process;
    private final OfficeConnection connection;

    private ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OfficeProcessThread"));

    private final Logger logger = Logger.getLogger(getClass().getName());

    public ManagedOfficeProcess(ManagedOfficeProcessSettings settings) throws OfficeException {
        this.settings = settings;
        process = new OfficeProcess(settings.getOfficeHome(), settings.getUnoUrl(), settings.getTemplateProfileDir(), settings.getProcessManager());
        connection = new OfficeConnection(settings.getUnoUrl());
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
            } 
        });
    }

    public void restartDueToLostConnection() {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    doEnsureProcessExited();
                    doStartProcessAndConnect();
                } catch (OfficeException officeException) {
                    logger.log(Level.SEVERE, "could not restart process", officeException);
                }
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
            }.execute(settings.getRetryInterval(), settings.getRetryTimeout());
        } catch (Exception exception) {
            throw new OfficeException("could not establish connection", exception);
        }
    }

    private void doStopProcess() {
        try {
            XDesktop desktop = OfficeUtils.cast(XDesktop.class, connection.getService(OfficeUtils.SERVICE_DESKTOP));
            desktop.terminate();
        } catch (DisposedException disposedException) {
            // expected
        } catch (Exception exception) {
            // in case we can't get hold of the desktop
            doTerminateProcess();
        }
        doEnsureProcessExited();
    }

    private void doEnsureProcessExited() throws OfficeException {
        try {
            int exitCode = process.getExitCode(settings.getRetryInterval(), settings.getRetryTimeout());
            logger.info("process exited with code " + exitCode);
        } catch (RetryTimeoutException retryTimeoutException) {
            doTerminateProcess();
        }
        process.deleteProfileDir();
    }

    private void doTerminateProcess() throws OfficeException {
        try {
            int exitCode = process.forciblyTerminate(settings.getRetryInterval(), settings.getRetryTimeout());
            logger.info("process forcibly terminated with code " + exitCode);
        } catch (Exception exception) {
            throw new OfficeException("could not terminate process", exception);
        }
    }

}
