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

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * {@link ProcessManager} implementation for Linux. Uses the <tt>ps</tt>
 * and <tt>kill</tt> commands.
 * <p>
 * Should Work on Solaris too, except that the command line string
 * returned by <tt>ps</tt> there is limited to 80 characters and this affects
 * {@link #findPid(String)}.
 */
public class LinuxProcessManager implements ProcessManager {

    private static final Pattern PS_OUTPUT_LINE = Pattern.compile("^\\s*(\\d+)\\s+(.*)$"); 

    private String[] runAsArgs;

    public void setRunAsArgs(String... runAsArgs) {
		this.runAsArgs = runAsArgs;
	}

    protected String[] psCommand() {
        return new String[] { "/bin/ps", "-e", "-o", "pid,args" };
    }

    public long findPid(ProcessQuery query) throws IOException {
        String regex = Pattern.quote(query.getCommand()) + ".*" + Pattern.quote(query.getArgument());
        Pattern commandPattern = Pattern.compile(regex);
        for (String line : execute(psCommand())) {
            Matcher lineMatcher = PS_OUTPUT_LINE.matcher(line);
            if (lineMatcher.matches()) {
                String command = lineMatcher.group(2);
                Matcher commandMatcher = commandPattern.matcher(command);
                if (commandMatcher.find()) {
                    return Long.parseLong(lineMatcher.group(1));
                }
            }
        }
        return PID_NOT_FOUND;
    }

    public void kill(Process process, long pid) throws IOException {
    	if (pid <= 0) {
    		throw new IllegalArgumentException("invalid pid: " + pid);
    	}
        execute("/bin/kill", "-KILL", Long.toString(pid));
    }

    private List<String> execute(String... args) throws IOException {
    	String[] command;
    	if (runAsArgs != null) {
    		command = new String[runAsArgs.length + args.length];
    		System.arraycopy(runAsArgs, 0, command, 0, runAsArgs.length);
    		System.arraycopy(args, 0, command, runAsArgs.length, args.length);
    	} else {
    		command = args;
    	}
        Process process = new ProcessBuilder(command).start();
        @SuppressWarnings("unchecked")
        List<String> lines = IOUtils.readLines(process.getInputStream());
        return lines;
    }

}
