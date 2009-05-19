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
package org.artofsolving.jodconverter.process;

import org.artofsolving.jodconverter.ReflectionUtils;
import org.artofsolving.jodconverter.util.PlatformUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

@Test
public class ProcessManagerTest {

    public void unixProcessManager() throws Exception {
        if (PlatformUtils.isMac() || PlatformUtils.isWindows()) {
            throw new SkipException("UnixProcessManager only works on Unix");
        }
        ProcessManager processManager = new UnixProcessManager();
        Process process = new ProcessBuilder("sleep", "5s").start();
        String pid = processManager.findPid("sleep 5s");
        Assert.assertNotNull(pid);
        Assert.assertEquals(pid, ReflectionUtils.getPrivateField(process, "pid").toString());
        processManager.kill(process, pid);
        Assert.assertNull(processManager.findPid("sleep 5s"));
    }

    public void macProcessManager() throws Exception {
        if (!PlatformUtils.isMac()) {
            throw new SkipException("MacProcessManager only works on Mac");
        }
        ProcessManager processManager = new MacProcessManager();
        Process process = new ProcessBuilder("sleep", "5s").start();
        String pid = processManager.findPid("sleep 5s");
        Assert.assertNotNull(pid);
        Assert.assertEquals(pid, ReflectionUtils.getPrivateField(process, "pid").toString());
        processManager.kill(process, pid);
        Assert.assertNull(processManager.findPid("sleep 5s"));
    }

}
