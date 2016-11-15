//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
// -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
// -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

/**
 * Object that will attempt to execute a task until it succeeds or that a specific timeout is
 * reached.
 */
abstract class Retryable {

    /**
     * Attempt to execute the task, once.
     * 
     * @throws TemporaryException
     *             for an error condition that can be temporary - i.e. retrying later could be
     *             successful
     * @throws Exception
     *             for all other error conditions
     */
    protected abstract void attempt() throws Exception;

    /**
     * Executes the task without a starting delay.
     * 
     * @param interval
     *            the interval between each attempt.
     * @param timeout
     *            the timeout after which no more attempt will be tried.
     * @throws RetryTimeoutException
     *             if we have reached the timeout.
     * @throws Exception
     *             for all other error conditions
     */
    public void execute(long interval, long timeout) throws RetryTimeoutException, Exception {

        execute(0L, interval, timeout);
    }

    /**
     * Executes the task with a specified starting delay.
     * 
     * @param delay
     *            a delay to wait for before the fist attempt.
     * @param interval
     *            the interval between each attempt.
     * @param timeout
     *            the timeout after which no more attempt will be tried.
     * @throws RetryTimeoutException
     *             if we have reached the timeout.
     * @throws Exception
     *             for all other error conditions
     */
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
