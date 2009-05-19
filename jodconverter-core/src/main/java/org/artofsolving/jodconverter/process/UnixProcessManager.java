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

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * {@link ProcessManager} implementation for *nix systems. Uses the <tt>ps</tt>
 * and <tt>kill</tt> commands.
 * <p>
 * Works for Linux. Works for Solaris too, except that the command line string
 * returned by <tt>ps</tt> there is limited to 80 characters and this affects
 * {@link #findPid(String)}.
 */
public class UnixProcessManager implements ProcessManager {

    private static final Pattern PS_OUTPUT_LINE = Pattern.compile("^\\s*(\\d+)\\s+(.*)$"); 

    protected String[] psCommand() {
        return new String[] { "/bin/ps", "-e", "-o", "pid,args" };
    }

    public String findPid(String regex) throws IOException {
        Pattern commandPattern = Pattern.compile(regex);
        for (String line : execute(psCommand())) {
            Matcher lineMatcher = PS_OUTPUT_LINE.matcher(line);
            if (lineMatcher.matches()) {
                String command = lineMatcher.group(2);
                Matcher commandMatcher = commandPattern.matcher(command);
                if (commandMatcher.find()) {
                    return lineMatcher.group(1);
                }
            }
        }
        return null;
    }

    public void kill(Process process, String pid) throws IOException {
        execute("/bin/kill", "-KILL", pid);
    }

    private List<String> execute(String... command) throws IOException {
        Process process = new ProcessBuilder(command).start();
        @SuppressWarnings("unchecked")
        List<String> lines = IOUtils.readLines(process.getInputStream());
        return lines;
    }

}
