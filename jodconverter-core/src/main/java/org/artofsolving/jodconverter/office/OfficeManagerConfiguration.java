package org.artofsolving.jodconverter.office;

import java.io.File;

import org.artofsolving.jodconverter.process.ProcessManager;
import org.artofsolving.jodconverter.process.PureJavaProcessManager;

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
    private ProcessManager processManager = new PureJavaProcessManager();

    public OfficeManagerConfiguration setOfficeHome(String officeHome) throws NullPointerException, IllegalArgumentException {
        checkArgumentNotNull("officeHome", officeHome);
        return setOfficeHome(new File(officeHome));
    }

    public OfficeManagerConfiguration setOfficeHome(File officeHome) throws NullPointerException, IllegalArgumentException  {
        checkArgumentNotNull("officeHome", officeHome);
        checkArgument("officeHome", officeHome.isDirectory(), "must exist and be a directory");
        this.officeHome = officeHome;
        return this;
    }

    public OfficeManagerConfiguration setConnectionProtocol(ConnectionProtocol connectionProtocol) throws NullPointerException {
        checkArgumentNotNull("connectionProtocol", connectionProtocol);
        this.connectionProtocol = connectionProtocol;
        return this;
    }

    public OfficeManagerConfiguration setPortNumber(int portNumber) {
        this.portNumbers = new int[] { portNumber };
        return this;
    }

    public OfficeManagerConfiguration setPortNumbers(int... portNumbers) throws NullPointerException, IllegalArgumentException {
        checkArgumentNotNull("portNumbers", portNumbers);
        checkArgument("portNumbers", portNumbers.length > 0, "must not be empty");
        this.portNumbers = portNumbers;
        return this;
    }

    public OfficeManagerConfiguration setPipeName(String pipeName) throws NullPointerException {
        checkArgumentNotNull("pipeName", pipeName);
        this.pipeNames = new String[] { pipeName };
        return this;
    }

    public OfficeManagerConfiguration setPipeNames(String... pipeNames) throws NullPointerException, IllegalArgumentException {
        checkArgumentNotNull("pipeNames", pipeNames);
        checkArgument("pipeNames", pipeNames.length > 0, "must not be empty");
        this.pipeNames = pipeNames;
        return this;
    }

    public OfficeManagerConfiguration setTemplateProfileDir(File templateProfileDir) throws IllegalArgumentException {
        if (templateProfileDir != null) {
            checkArgument("templateProfileDir", templateProfileDir.isDirectory(), "must exist and be a directory");
        }
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

    public OfficeManagerConfiguration setProcessManager(ProcessManager processManager) throws NullPointerException {
        checkArgumentNotNull("processManager", processManager);
        this.processManager = processManager;
        return this;
    }

    public OfficeManager buildOfficeManager() {
        int numInstances = connectionProtocol == ConnectionProtocol.PIPE ? pipeNames.length : portNumbers.length;
        UnoUrl[] unoUrls = new UnoUrl[numInstances];
        for (int i = 0; i < numInstances; i++) {
            unoUrls[i] = (connectionProtocol == ConnectionProtocol.PIPE) ? UnoUrl.pipe(pipeNames[i]) : UnoUrl.socket(portNumbers[i]);
        }
        return new ProcessPoolOfficeManager(officeHome, unoUrls, templateProfileDir,
                taskQueueTimeout, taskExecutionTimeout, maxTasksPerProcess, processManager);
    }

    private void checkArgumentNotNull(String argName, Object argValue) throws NullPointerException {
        if (argValue == null) {
            throw new NullPointerException(argName + " must not be null");
        }
    }

    private void checkArgument(String argName, boolean condition, String message) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(argName + " " + message);
        }
    }

}
