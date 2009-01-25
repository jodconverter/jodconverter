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

import static org.testng.Assert.assertTrue;

import java.io.File;

import net.sf.jodconverter.util.ReflectionUtils;

import org.testng.annotations.Test;

@Test(groups="integration")
public class ExternalProcessOfficeManagerTest {

    public void executeTask() throws Exception {
        File officeHome = OfficeUtils.getDefaultOfficeHome();
        OfficeConnectionMode connectionMode = OfficeConnectionMode.socket(8100);
        File templateProfileDir = OfficeUtils.getDefaultProfileDir();
        
        OfficeProcess officeProcess = new OfficeProcess(connectionMode, officeHome, templateProfileDir);
        officeProcess.start();
        Thread.sleep(2000);
        
        ExternalProcessOfficeManager manager = new ExternalProcessOfficeManager(connectionMode);
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
