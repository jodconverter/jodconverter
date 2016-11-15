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
package org.artofsolving.jodconverter.process;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.artofsolving.jodconverter.util.PlatformUtils;
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
        ProcessQuery query = new ProcessQuery("sleep", "5s");

        long pid = processManager.findPid(query);
        assertFalse(pid == ProcessManager.PID_NOT_FOUND);
        Number javaPid = (Number) FieldUtils.readDeclaredField(process, "pid", true);
        assertEquals(pid, javaPid.longValue());

        processManager.kill(process, pid);
        assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
    }

    public void macProcessManager() throws Exception {
        if (!PlatformUtils.isMac()) {
            throw new SkipException("MacProcessManager only works on Mac");
        }
        ProcessManager processManager = new MacProcessManager();
        Process process = new ProcessBuilder("sleep", "5s").start();
        ProcessQuery query = new ProcessQuery("sleep", "5s");

        long pid = processManager.findPid(query);
        assertFalse(pid == ProcessManager.PID_NOT_FOUND);
        Number javaPid = (Number) FieldUtils.readDeclaredField(process, "pid", true);

        assertEquals(pid, javaPid.longValue());

        processManager.kill(process, pid);
        assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
    }

    public void windowsProcessManager() throws Exception {
        if (!PlatformUtils.isWindows()) {
            throw new SkipException("WindowsProcessManager only works on Windows");
        }
        ProcessManager processManager = new WindowsProcessManager();
        Process process = new ProcessBuilder("ping", "127.0.0.1", "-n", "5").start();
        ProcessQuery query = new ProcessQuery("ping", "127.0.0.1 -n 5");

        long pid = processManager.findPid(query);
        assertFalse(pid == ProcessManager.PID_NOT_FOUND);
        // Won't work on Windows, skit this assertion
        //Number javaPid = (Number) FieldUtils.readDeclaredField(process, "pid", true);
        //assertEquals(pid, javaPid.longValue());

        processManager.kill(process, pid);
        assertEquals(processManager.findPid(query), ProcessManager.PID_NOT_FOUND);
    }

}
