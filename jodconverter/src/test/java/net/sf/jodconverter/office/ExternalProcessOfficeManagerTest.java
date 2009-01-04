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
package org.artofsolving.jodconverter.office;

import static org.testng.Assert.*;

import java.io.File;

import org.artofsolving.jodconverter.test.TestUtils;
import org.artofsolving.jodconverter.util.ReflectionUtils;
import org.testng.annotations.Test;

@Test(groups="integration")
public class ExternalProcessOfficeManagerTest {

    public void executeTask() throws Exception {
        File officeHome = TestUtils.getOfficeHome();
        String connectString = ExternalProcessOfficeManager.DEFAULT_CONNECT_STRING;
        File templateProfileDir = TestUtils.getOfficeProfile();
        
        OfficeProcess officeProcess = new OfficeProcess(officeHome, connectString, templateProfileDir);
        officeProcess.start();
        Thread.sleep(2000);
        
        ExternalProcessOfficeManager manager = new ExternalProcessOfficeManager();
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
