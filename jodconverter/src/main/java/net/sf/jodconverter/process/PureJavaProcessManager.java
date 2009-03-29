package net.sf.jodconverter.process;

public class PureJavaProcessManager implements ProcessManager {

    public String findPid(String regex) {
        return null;
    }

    public String getPid(Process process) {
        return null;
    }

    public void kill(Process process) {
        process.destroy();
    }

}
