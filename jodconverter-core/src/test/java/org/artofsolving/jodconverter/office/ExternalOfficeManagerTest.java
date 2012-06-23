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
package org.artofsolving.jodconverter.office;

import static org.testng.Assert.assertTrue;

import java.io.File;

import org.artofsolving.jodconverter.ReflectionUtils;
import org.artofsolving.jodconverter.process.PureJavaProcessManager;
import org.testng.annotations.Test;

@Test(groups="integration")
public class ExternalOfficeManagerTest {

    public void executeTask() throws Exception {
        UnoUrl unoUrl = UnoUrl.socket(2002);
        OfficeProcess officeProcess = new OfficeProcess(OfficeUtils.getDefaultOfficeHome(), unoUrl,
            null, null, new File(System.getProperty("java.io.tmpdir")), new PureJavaProcessManager());
        officeProcess.start();
        Thread.sleep(2000);
        Integer exitCode = officeProcess.getExitCode();
        if (exitCode != null && exitCode.equals(Integer.valueOf(81))) {
            officeProcess.start(true);
            Thread.sleep(2000);
        }
        
        ExternalOfficeManager manager = new ExternalOfficeManager(unoUrl, true);
        manager.start();
        
        MockOfficeTask task = new MockOfficeTask();
        manager.execute(task);
        assertTrue(task.isCompleted());
        
        manager.stop();
        //TODO replace when OfficeProcess has a forciblyTerminate()
        Process process = (Process) ReflectionUtils.getPrivateField(officeProcess, "process");
        process.destroy();
    }

    //TODO test auto-reconnection

}
