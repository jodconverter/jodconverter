package net.sf.jodconverter.office;

import java.io.File;

class ManagedOfficeProcessConfiguration extends OfficeProcessConfiguration {

    public static final long DEFAULT_RETRY_TIMEOUT = 30000L;
    public static final long DEFAULT_RETRY_INTERVAL = 250L;

    private File templateProfileDir = OfficeUtils.getDefaultProfileDir();
    private long retryTimeout = DEFAULT_RETRY_TIMEOUT;
    private long retryInterval = DEFAULT_RETRY_INTERVAL;

    public ManagedOfficeProcessConfiguration(OfficeConnectionMode connectionMode) {
        super(connectionMode);
    }

    public File getTemplateProfileDir() {
        return templateProfileDir;
    }

    public void setTemplateProfileDir(File templateProfileDir) {
        this.templateProfileDir = templateProfileDir;
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
