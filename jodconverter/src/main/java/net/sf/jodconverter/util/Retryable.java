//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2008 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, you can find it online
// at http://www.gnu.org/licenses/lgpl-2.1.html.
//
package org.artofsolving.jodconverter.util;

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
