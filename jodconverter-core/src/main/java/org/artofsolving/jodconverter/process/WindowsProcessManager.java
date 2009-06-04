package org.artofsolving.jodconverter.process;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * {@link ProcessManager} implementation for Windows.
 * <p>
 * Requires wmic.exe and taskkill.exe, that should be available at least on
 * Windows XP, Windows Vista, and Windows 7 (except Home versions). 
 */
public class WindowsProcessManager implements ProcessManager {

    private static final Pattern PROCESS_GET_LINE = Pattern.compile("^(.*?)\\s+(\\d+)\\s*$");

    public String findPid(String regex) throws IOException {
        Pattern commandPattern = Pattern.compile(regex);
        for (String line : execute("wmic", "process", "get", "CommandLine,ProcessId")) {
            Matcher lineMatcher = PROCESS_GET_LINE.matcher(line);
            if (lineMatcher.matches()) {
                String commandLine = lineMatcher.group(1);
                String pid = lineMatcher.group(2);
                Matcher commandMatcher = commandPattern.matcher(commandLine);
                if (commandMatcher.find()) {
                    return pid;
                }
            }
        }
        return null;
    }

    public void kill(Process process, String pid) throws IOException {
        execute("taskkill", "/t", "/f", "/pid", pid);
    }

    public boolean isUsable() {
        try {
            execute("wmic", "quit");
            execute("taskkill", "/?");
            return true;
        } catch (IOException ioException) {
            return false;
        }
    }

    private List<String> execute(String... command) throws IOException {
        Process process = new ProcessBuilder(command).start();
        process.getOutputStream().close(); // don't wait for stdin
        @SuppressWarnings("unchecked")
        List<String> lines = IOUtils.readLines(process.getInputStream());
        try {
            process.waitFor();
        } catch (InterruptedException interruptedException) {
            // sorry for the interruption
        }
        return lines;
    }

}
