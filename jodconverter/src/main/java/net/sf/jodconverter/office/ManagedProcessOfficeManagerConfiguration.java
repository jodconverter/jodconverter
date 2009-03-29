package net.sf.jodconverter.office;

public class ManagedProcessOfficeManagerConfiguration extends ManagedOfficeProcessConfiguration {

    public static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120000L;
    public static final int DEFAULT_MAX_TASKS_PER_PROCESS = 200;
    private static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30 * 1000;

    private long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;
    private int maxTasksPerProcess = DEFAULT_MAX_TASKS_PER_PROCESS;
    private long taskQueueTimeout = DEFAULT_TASK_QUEUE_TIMEOUT;

    public ManagedProcessOfficeManagerConfiguration(OfficeConnectionMode connectionMode) {
        super(connectionMode);
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

    public long getTaskQueueTimeout() {
        return taskQueueTimeout;
    }

    public void setTaskQueueTimeout(long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

}
