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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a connection to an office process.
 */
public class ConnectRetryable extends Retryable {

    private static final Integer EXIT_CODE_NEW_INSTALLATION = Integer.valueOf(81);
    private static final Logger logger = LoggerFactory.getLogger(ConnectRetryable.class);

    private OfficeProcess process;
    private OfficeConnection connection;

    /**
     * Creates a new instance of the class for the specified process and connection.
     * 
     * @param process
     *            the office process whose exit code is to be retrieved.
     * @param connection
     *            the office connection to connect.
     */
    public ConnectRetryable(OfficeProcess process, OfficeConnection connection) {
        super();

        this.process = process;
        this.connection = connection;
    }

    @Override
    protected void attempt() throws Exception {

        try {
            // Try to connect
            connection.connect();

        } catch (OfficeConnectionException connectionEx) {

            Integer exitCode = process.getExitCode();
            if (exitCode == null) {

                // Process is running; retry later
                throw new TemporaryException(connectionEx);

            } else if (exitCode.equals(EXIT_CODE_NEW_INSTALLATION)) {

                // Restart and retry later
                // see http://code.google.com/p/jodconverter/issues/detail?id=84
                logger.warn("Office process died with exit code 81; restarting it");
                process.start(true);
                throw new TemporaryException(connectionEx);

            } else {
                throw new OfficeException("Office process died with exit code " + exitCode);
            }
        }
    }
}
