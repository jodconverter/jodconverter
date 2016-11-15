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
 * A {@code RetryTimeoutException} is thrown by a {@code Retryable} when it is unable to execute its
 * task within a specified amount of time.
 */
class RetryTimeoutException extends Exception {
    private static final long serialVersionUID = -3704437769955257514L;

    /**
     * Constructs a new exception with the specified cause
     * 
     * @param cause
     *            the cause.
     */
    public RetryTimeoutException(Throwable cause) {
        super(cause);
    }
}
