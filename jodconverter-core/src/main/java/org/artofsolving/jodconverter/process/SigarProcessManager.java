//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2011 Mirko Nasato and contributors
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
package org.artofsolving.jodconverter.process;

import java.io.IOException;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;

/**
 * {@link ProcessManager} implementation that uses the SIGAR library.
 * <p>
 * Requires the sigar.jar in the classpath and the appropriate system-specific
 * native library (e.g. <tt>libsigar-x86-linux.so</tt> on Linux x86) available
 * in the <em>java.library.path</em>.
 * <p>
 * See the <a href="http://support.hyperic.com/display/SIGAR">SIGAR site</a>
 * for documentation and downloads.
 */
public class SigarProcessManager implements ProcessManager {

    public long findPid(ProcessQuery query) throws IOException {
        Sigar sigar = new Sigar();
        try {
            long[] pids = ProcessFinder.find(sigar, "State.Name.eq=" + query.getCommand());
            for (int i = 0; i < pids.length; i++) {
                String[] arguments = sigar.getProcArgs(pids[i]);
                if (arguments != null && argumentMatches(arguments, query.getArgument())) {
                    return pids[i];
                }
            }
            return PID_UNKNOWN;
        } catch (SigarException sigarException) {
            throw new IOException("findPid failed", sigarException);
        } finally {
            sigar.close();
        }
    }

    public void kill(Process process, long pid) throws IOException {
        Sigar sigar = new Sigar();
        try {
            sigar.kill(pid, "KILL");
        } catch (SigarException sigarException) {
            throw new IOException("kill failed", sigarException);
        } finally {
            sigar.close();
        }
    }

    private boolean argumentMatches(String[] arguments, String expected) {
        for (String argument : arguments) {
            if (argument.contains(expected)) {
                return true;
            }
        }
        return false;
    }

}
