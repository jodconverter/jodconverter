package org.artofsolving.jodconverter.office;

import java.io.File;

public class OfficeManagerConfiguration {

    public static enum ConnectionProtocol { PIPE, SOCKET };

    private File officeHome = OfficeUtils.getDefaultOfficeHome();
    private ConnectionProtocol connectionProtocol = ConnectionProtocol.SOCKET;
    private int[] portNumbers = new int[] { 2002 };
    private String[] pipeNames = new String[] { "office" };
    private long taskQueueTimeout = 30000L;

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

    public OfficeManagerConfiguration setPipeName(String pipeName) {
        this.pipeNames = new String[] { pipeName };
        return this;
    }

    public OfficeManagerConfiguration setPipeNames(String... pipeNames) {
        this.pipeNames = pipeNames;
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

    public void setTaskQueueTimeout(long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

    public OfficeManager buildOfficeManager() {
        int numInstances = connectionProtocol == ConnectionProtocol.PIPE ? pipeNames.length : portNumbers.length;
        UnoUrl[] unoUrls = new UnoUrl[numInstances];
        for (int i = 0; i < numInstances; i++) {
            unoUrls[i] = (connectionProtocol == ConnectionProtocol.PIPE) ? UnoUrl.pipe(pipeNames[i]) : UnoUrl.socket(portNumbers[i]);
        }
        return new ProcessPoolOfficeManager(officeHome, unoUrls, taskQueueTimeout);
    }

}
