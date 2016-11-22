package org.artofsolving.jodconverter.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration class for JODConverter.
 */
@ConfigurationProperties(prefix = "jodconverter")
public class JodConverterProperties {

    /**
     * Enable JODConverter, which means that office instances will be launched.
     * <p>
     * If not set, it defaults to false.
     */
    private boolean enabled = false;

    /**
     * Represents the office home directory.
     * <p>
     * If not set, it defaults to an auto detected home directory (most recent version first).
     */
    private String officeHome;

    /**
     * List of ports, separated by commas, used by each JODConverter processing thread. The number of office instances
     * is equal to the number of ports, since 1 office will be launched for each port number.
     * <p>
     * If not set, it defaults to a list with a single port, 2002.
     */
    private String portNumbers = "2002";

    /**
     * Directory where temporary office profiles will be created.
     * <p>
     * If not set, it defaults to the system temporary directory as specified by the <code>java.io.tmpdir</code> system
     * property.
     */
    private String workingDir;

    /**
     * Template profile directory to copy to a created office profile directory when an office processed is launched.
     */
    private String templateProfileDir;

    /**
     * Retry timeout (milliseconds).Used for retrying office process calls (start/terminate).
     * <p>
     * If not set, it defaults to 120000 (2 minutes).
     */
    private long retryTimeout = 120000L;

    /**
     * Retry interval (milliseconds).Used for waiting between office process call tries (start/terminate).
     * <p>
     * If not set, it defaults to 250.
     */
    private long retryInterval = 250;

    /**
     * Indicates whether we must kill existing office process when an office process already exists for the same
     * connection string.
     * <p>
     * If not set, it defaults to true.
     */
    private boolean killExistingProcess = true;

    /**
     * Maximum living time of a task in the conversion queue. The task will be removed from the queue if the waiting
     * time is longer than this timeout.
     * <p>
     * If not set, it defaults to 30000 (30 seconds).
     */
    private long taskQueueTimeout = 30000L;

    /**
     * Maximum time allowed to process a task. If the processing time of a task is longer than this timeout, this task
     * will be aborted and the next task is processed.
     * <p>
     * If not set, it defaults to 120000 (2 minutes).
     */
    private long taskExecutionTimeout = 120000L;

    /**
     * Maximum number of tasks an office process can execute before restarting.
     * <p>
     * If not set, it defaults to 200.
     */
    private int maxTasksPerProcess = 200;

    public boolean isEnabled() {
        return enabled;
    }

    public String getOfficeHome() {
        return officeHome;
    }

    public void setOfficeHome(String officeHome) {
        this.officeHome = officeHome;
    }

    public String getPortNumbers() {
        return portNumbers;
    }

    public void setPortNumbers(String portNumbers) {
        this.portNumbers = portNumbers;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getTemplateProfileDir() {
        return templateProfileDir;
    }

    public void setTemplateProfileDir(String templateProfileDir) {
        this.templateProfileDir = templateProfileDir;
    }

    public long getRetryTimeout() {
        return retryTimeout;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRetryTimeout(long retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public boolean isKillExistingProcess() {
        return killExistingProcess;
    }

    public void setKillExistingProcess(boolean killExistingProcess) {
        this.killExistingProcess = killExistingProcess;
    }

    public long getTaskQueueTimeout() {
        return taskQueueTimeout;
    }

    public void setTaskQueueTimeout(long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

    public long getTaskExecutionTimeout() {
        return taskExecutionTimeout;
    }

    public void setTaskExecutionTimeout(long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

    public int getMaxTasksPerProcess() {
        return maxTasksPerProcess;
    }

    public void setMaxTasksPerProcess(int maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
    }
}
