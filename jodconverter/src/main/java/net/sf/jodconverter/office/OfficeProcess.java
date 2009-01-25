//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2009 - Mirko Nasato and Contributors
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
package net.sf.jodconverter.office;

import static net.sf.jodconverter.util.UnixProcessUtils.SIGNAL_KILL;
import static net.sf.jodconverter.util.UnixProcessUtils.getUnixPid;
import static net.sf.jodconverter.util.UnixProcessUtils.isUnixProcess;
import static net.sf.jodconverter.util.UnixProcessUtils.killUnixProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.jodconverter.util.OsUtils;
import net.sf.jodconverter.util.RetryTimeoutException;
import net.sf.jodconverter.util.Retryable;
import net.sf.jodconverter.util.TemporaryException;
import net.sf.jodconverter.util.UnixProcessException;

import org.apache.commons.io.FileUtils;

public class OfficeProcess {

    private final File officeHome;
    private final OfficeConnectionMode connectionMode;
    private final File profileDir;

    private Process process;

    private final Logger logger = Logger.getLogger(getClass().getName());

    public OfficeProcess(OfficeConnectionMode connectionMode, File officeHome) {
        this(connectionMode, officeHome, null);
    }

    public OfficeProcess(OfficeConnectionMode connectionMode, File officeHome, File profileDir) {
        this.connectionMode = connectionMode;
        this.officeHome = officeHome;
        this.profileDir = profileDir;
    }

    public void start() throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(new File(officeHome, getExecutablePath()).getAbsolutePath());
        command.add("-accept=" + connectionMode.getAcceptString() + ";urp;");
        if (profileDir != null) {
            command.add("-env:UserInstallation=" + OfficeUtils.toUrl(profileDir));
        }
        command.add("-headless");
        command.add("-nocrashreport");
        command.add("-nodefault");
        //command.add("-nofirststartwizard");
        command.add("-nolockcheck");
        command.add("-nologo");
        command.add("-norestore");
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (OsUtils.isWindows()) {
            addBasisAndUrePaths(processBuilder);
        }
        logger.info(String.format("starting process with acceptString '%s' and profileDir '%s'", connectionMode, profileDir));
        process = processBuilder.start();
        int pid = -1;
        if (isUnixProcess(process)) {
            try {
                pid = getUnixPid(process);
            } catch (UnixProcessException unixProcessException) {
                // pass
            }
        }
        logger.info("started process; pid " + pid);
    }

    private String getExecutablePath() {
        if (OsUtils.isMac()) {
            return "MacOS/soffice.bin";
        } else {
            return "program/soffice.bin";
        }
    }

    private void addBasisAndUrePaths(ProcessBuilder processBuilder) throws IOException {
        // see http://wiki.services.openoffice.org/wiki/ODF_Toolkit/Efforts/Three-Layer_OOo
        File basisLink = new File(officeHome, "basis-link");
        if (!basisLink.isFile()) {
            logger.fine("no %OFFICE_HOME%/basis-link found; assuming it's OOo 2.x and we don't need to append URE and Basic paths");
            return;
        }
        String basisLinkText = FileUtils.readFileToString(basisLink).trim();
        File basisHome = new File(officeHome, basisLinkText);
        File basisProgram = new File(basisHome, "program");
        File ureLink = new File(basisHome, "ure-link");
        String ureLinkText = FileUtils.readFileToString(ureLink).trim();
        File ureHome = new File(basisHome, ureLinkText);
        File ureBin = new File(ureHome, "bin");
        Map<String,String> environment = processBuilder.environment();
        // Windows environment variables are case insensitive but Java maps are not :-/
        // so let's make sure we modify the existing key
        String pathKey = "PATH";
        for (String key : environment.keySet()) {
            if ("PATH".equalsIgnoreCase(key)) {
                pathKey = key;
            }
        }
        String path = environment.get(pathKey) + ";" + ureBin.getAbsolutePath() + ";" + basisProgram.getAbsolutePath();
        logger.fine(String.format("setting %s to \"%s\"", pathKey, path));
        environment.put(pathKey, path);
    }

    public boolean isRunning() {
        if (process == null) {
            return false;
        }
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException exception) {
            return true;
        }
    }

    private class ExitCodeRetryable extends Retryable {
        
        private int exitCode;
        
        protected void attempt() throws TemporaryException, Exception {
            try {
                exitCode = process.exitValue();
            } catch (IllegalThreadStateException illegalThreadStateException) {
                throw new TemporaryException(illegalThreadStateException);
            }
        }
        
        public int getExitCode() {
            return exitCode;
        }

    }
    
    public int getExitCode(long retryInterval, long retryTimeout) throws RetryTimeoutException {
        try {
            ExitCodeRetryable retryable = new ExitCodeRetryable();
            retryable.execute(retryInterval, retryTimeout);
            return retryable.getExitCode();
        } catch (RetryTimeoutException retryTimeoutException) {
            throw retryTimeoutException;
        } catch (Exception exception) {
            throw new OfficeException("could not get process exit code", exception);
        }
    }

    public int forciblyTerminate(long retryInterval, long retryTimeout) throws RetryTimeoutException {
        logger.info(String.format("trying to forcibly terminate process: '%s'", connectionMode));
        process.destroy();
        try {
            return getExitCode(retryInterval, retryTimeout);
        } catch (RetryTimeoutException retryTimeoutException) {
            if (isUnixProcess(process)) {
                logger.warning(String.format("process still did not exit; sending KILL signal"));
                try {
                    killUnixProcess(process, SIGNAL_KILL);
                    return getExitCode(retryInterval, retryTimeout);
                } catch (UnixProcessException unixProcessException) {
                    logger.warning("could not kill process: " + unixProcessException.getMessage());
                    throw retryTimeoutException;
                }
            } else {
                throw retryTimeoutException;
            }
        }
    }

}
