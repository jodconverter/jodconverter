package net.sf.jodconverter.office;


class ManagedOfficeProcessConfiguration extends OfficeProcessConfiguration {

    public static final long DEFAULT_RETRY_TIMEOUT = 30000L;
    public static final long DEFAULT_RETRY_INTERVAL = 250L;

    private long retryTimeout = DEFAULT_RETRY_TIMEOUT;
    private long retryInterval = DEFAULT_RETRY_INTERVAL;

    public ManagedOfficeProcessConfiguration(OfficeConnectionMode connectionMode) {
        super(connectionMode);
    }

    public long getRetryTimeout() {
        return retryTimeout;
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

}
