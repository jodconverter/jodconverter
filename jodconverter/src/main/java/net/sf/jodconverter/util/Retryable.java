package net.sf.jodconverter.util;

public abstract class Retryable {

    /**
     * @throws TemporaryException for an error condition that can be temporary - i.e. retrying later could be successful
     * @throws Exception for all other error conditions
     */
    protected abstract void attempt() throws TemporaryException, Exception;

    public void execute(long interval, long timeout) throws RetryTimeoutException, Exception {
        execute(0L, interval, timeout);
    }

    public void execute(long delay, long interval, long timeout) throws RetryTimeoutException, Exception {
        long start = System.currentTimeMillis();
        if (delay > 0L) {
            sleep(delay);
        }
        while (true) {
            try {
                attempt();
                return;
            } catch (TemporaryException temporaryException) {
                if (System.currentTimeMillis() - start < timeout) {
                    sleep(interval);
                    // continue
                } else {
                    throw new RetryTimeoutException(temporaryException.getCause());
                }
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            // continue
        }
    }

}
