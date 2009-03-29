package net.sf.jodconverter.process;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jodconverter.util.ReflectionUtils;

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

    public String getPid(Process process) {
        try {
            int pid = (Integer) ReflectionUtils.getPrivateField(process, "pid");
            return Integer.toString(pid);
        } catch (Exception exception) {
            throw new RuntimeException("couldn't get pid", exception);
        }
    }

    public void kill(Process process) throws IOException {
        execute("/bin/kill", "-KILL", getPid(process));
    }

    private List<String> execute(String... command) throws IOException {
        Process process = new ProcessBuilder(command).start();
        @SuppressWarnings("unchecked")
        List<String> lines = IOUtils.readLines(process.getInputStream());
        return lines;
    }

}
