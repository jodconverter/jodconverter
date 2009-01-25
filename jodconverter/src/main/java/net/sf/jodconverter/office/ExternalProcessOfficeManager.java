//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2009 - Mirko Nasato and Contributors
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package net.sf.jodconverter.office;

import java.net.ConnectException;

/**
 * {@link OfficeManager} implementation that connects to an external
 * Office process.
 * <p>
 * The external Office process needs to be started manually, e.g. from the
 * command line with
 * <pre>
 * soffice -accept="socket,host=127.0.0.1,port=8100;urp;"
 * </pre>
 * <p>
 * Since this implementation does not manage the Office process, it does not
 * support auto-restarting the process if it exits unexpectedly.
 * <p>
 * It will however auto-reconnect to the external process if the latter is
 * manually restarted.
 * <p>
 * This {@link OfficeManager} implementation basically provides the same
 * behaviour as JODConverter 2.x, including using <em>synchronized</em> blocks
 * for serialising office operations.
 */
public class ExternalProcessOfficeManager implements OfficeManager {

    private final OfficeConnection connection;

    private boolean connectOnStart = true;

    public ExternalProcessOfficeManager(OfficeConnectionMode connectionMode) {
        connection = new OfficeConnection(connectionMode);
    }

    /**
     * Should a connection be attempted on {@link #start()}?
     * <p>
     * Default is <em>true</em>. If <em>false</em>, a connection will be
     * attempted the first time an {@link OfficeTask} is executed.
     * 
     * @param connectOnStart
     */
    public void setConnectOnStart(boolean connectOnStart) {
        this.connectOnStart = connectOnStart;
    }

    public void start() throws OfficeException {
        if (connectOnStart) {
            synchronized (connection) {
                connect();
            }
        }
    }

    public void stop() {
        synchronized (connection) {
            if (connection.isConnected()) {
                connection.disconnect();
            }
        }
    }

    public void execute(OfficeTask task) throws OfficeException {
        synchronized (connection) {
            if (!connection.isConnected()) {
                connect();
            }
            task.execute(connection);
        }
    }

    private void connect() {
        try {
            connection.connect();
        } catch (ConnectException connectException) {
            throw new OfficeException("could not connect to external office process", connectException);
        }
    }

}
