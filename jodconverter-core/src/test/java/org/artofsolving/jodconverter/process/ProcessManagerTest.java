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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.artofsolving.jodconverter.ReflectionUtils;
import org.artofsolving.jodconverter.util.PlatformUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

@Test
public class ProcessManagerTest {

    public void linuxProcessManager() throws Exception {
        if (!PlatformUtils.isLinux()) {
            throw new SkipException("LinuxProcessManager can only be tested on Linux");
        }

        ProcessManager processManager = new LinuxProcessManager();
        Process process = new ProcessBuilder("sleep", "5s").start();
        ProcessQuery query = new ProcessQuery("sleep", "5s");
        
        long pid = processManager.findPid(query);
        assertFalse(pid == ProcessManager.PID_NOT_FOUND);
        Integer javaPid = (Integer) ReflectionUtils.getPrivateField(process, "pid");
        assertEquals(pid, javaPid.longValue());
        
        processManager.kill(process, pid);
        assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
    }

    public void sigarProcessManager() throws Exception {
        ProcessManager processManager = new SigarProcessManager();
        Process process = new ProcessBuilder("sleep", "5s").start();
        ProcessQuery query = new ProcessQuery("sleep", "5s");
        
        long pid = processManager.findPid(query);
        assertFalse(pid == ProcessManager.PID_NOT_FOUND);
        if (PlatformUtils.isLinux()) {
            Integer javaPid = (Integer) ReflectionUtils.getPrivateField(process, "pid");
            assertEquals(pid, javaPid.longValue());
        }

        processManager.kill(process, pid);
        assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
    }

}
