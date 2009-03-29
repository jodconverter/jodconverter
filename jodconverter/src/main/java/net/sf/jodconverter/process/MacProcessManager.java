package net.sf.jodconverter.process;

public class MacProcessManager extends UnixProcessManager {

    protected String[] psCommand() {
        return new String[] { "/bin/ps", "-e", "-o", "pid,command" };
    }

}
