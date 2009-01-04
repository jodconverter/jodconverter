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
package net.sf.jodconverter.util;

import static net.sf.jodconverter.util.UnixProcessUtils.*;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

@Test(groups="unix")
public class UnixProcessUtilsTest {

    public void getPidAndKillProcess() throws Exception {
        if (OsUtils.isWindows()) {
            //TODO should use testng config instead
            System.out.println("-- test disabled on Windows");
            return;
        }
        Process process = new ProcessBuilder("sleep", "10").start();
        assertTrue(isUnixProcess(process));
        assertTrue(getUnixPid(process) > 0);
        killUnixProcess(process, SIGNAL_TERM);
        Thread.sleep(50);  // give process a chance to exit
        assertTrue(process.exitValue() > 0);
    }

}
