package org.artofsolving.jodconverter.office;

import java.io.File;

public class OfficeManagerConfiguration {

    public static enum ConnectionProtocol { PIPE, SOCKET };

    private File officeHome = OfficeUtils.getDefaultOfficeHome();
    private ConnectionProtocol connectionProtocol = ConnectionProtocol.SOCKET;
    private int[] portNumbers = new int[] { 2002 };
    private String[] pipeNames = new String[] { "office" };
    private File templateProfileDir = null;
    private long taskQueueTimeout = 30000L;  // 30 seconds
    private long taskExecutionTimeout = 120000L;  // 2 minutes
    private int maxTasksPerProcess = 200;

    public OfficeManagerConfiguration setOfficeHome(String officeHome) {
        return setOfficeHome(new File(officeHome));
    }

    public OfficeManagerConfiguration setOfficeHome(File officeHome) {
        this.officeHome = officeHome;
        return this;
    }

    public OfficeManagerConfiguration setConnectionProtocol(ConnectionProtocol connectionProtocol) {
        this.connectionProtocol = connectionProtocol;
        return this;
    }

    public OfficeManagerConfiguration setPortNumber(int portNumber) {
        this.portNumbers = new int[] { portNumber };
        return this;
    }

    public OfficeManagerConfiguration setPortNumbers(int... portNumbers) {
        this.portNumbers = portNumbers;
        return this;
    }

    public OfficeManagerConfiguration setPipeName(String pipeName) {
        this.pipeNames = new String[] { pipeName };
        return this;
    }

    public OfficeManagerConfiguration setPipeNames(String... pipeNames) {
        this.pipeNames = pipeNames;
        return this;
    }

    public OfficeManagerConfiguration setTemplateProfileDir(File templateProfileDir) {
        this.templateProfileDir = templateProfileDir;
        return this;
    }

    public OfficeManagerConfiguration setTaskQueueTimeout(long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
        return this;
    }

    public OfficeManagerConfiguration setTaskExecutionTimeout(long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
        return this;
    }

    public OfficeManagerConfiguration setMaxTasksPerProcess(int maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
        return this;
    }

    public OfficeManager buildOfficeManager() {
        int numInstances = connectionProtocol == ConnectionProtocol.PIPE ? pipeNames.length : portNumbers.length;
        UnoUrl[] unoUrls = new UnoUrl[numInstances];
        for (int i = 0; i < numInstances; i++) {
            unoUrls[i] = (connectionProtocol == ConnectionProtocol.PIPE) ? UnoUrl.pipe(pipeNames[i]) : UnoUrl.socket(portNumbers[i]);
        }
        return new ProcessPoolOfficeManager(officeHome, unoUrls, templateProfileDir, taskQueueTimeout, taskExecutionTimeout, maxTasksPerProcess);
    }

}
