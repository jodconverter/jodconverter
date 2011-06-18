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
