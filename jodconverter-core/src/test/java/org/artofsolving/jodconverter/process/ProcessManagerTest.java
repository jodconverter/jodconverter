//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
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
