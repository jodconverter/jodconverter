package net.sf.jodconverter.process;

import java.io.IOException;

public interface ProcessManager {

    String getPid(Process process);

    void kill(Process process) throws IOException;

    String findPid(String regex) throws IOException;

}
