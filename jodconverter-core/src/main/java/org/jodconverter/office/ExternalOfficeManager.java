/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.office;

/**
 * {@link OfficeManager} implementation that connects to an external Office process.
 *
 * <p>The external Office process needs to be started manually, e.g. from the command line with
 *
 * <pre>
 * soffice -accept="socket,host=127.0.0.1,port=2002;urp;"
 * </pre>
 *
 * <p>Since this implementation does not manage the Office process, it does not support
 * auto-restarting the process if it exits unexpectedly.
 *
 * <p>It will however auto-reconnect to the external process if the latter is manually restarted.
 *
 * <p>This {@link OfficeManager} implementation basically provides the same behavior as JODConverter
 * 2.x, including using <em>synchronized</em> blocks for serializing office operations.
 */
class ExternalOfficeManager implements OfficeManager {

  public static final long DEFAULT_RETRY_TIMEOUT = 30000L; // 30 minutes
  public static final long DEFAULT_RETRY_INTERVAL = 250L; // 0.25 secs.

  private final OfficeConnection connection;
  private final boolean connectOnStart;

  /**
   * Constructs a new instance of the class.
   *
   * @param officeUrl The office URL.
   * @param connectOnStart Should a connection be attempted on {@link #start()}? Default is
   *     <em>true</em>. If <em>false</em>, a connection will only be attempted the first time an
   *     {@link OfficeTask} is executed.
   */
  public ExternalOfficeManager(final OfficeUrl officeUrl, final boolean connectOnStart) {

    connection = new OfficeConnection(officeUrl);
    this.connectOnStart = connectOnStart;
  }

  private void connect() throws OfficeException {

    try {
      new ConnectRetryable(connection).execute(DEFAULT_RETRY_INTERVAL, DEFAULT_RETRY_TIMEOUT);

    } catch (Exception ex) {
      throw new OfficeException("Could not establish connection to external office process", ex);
    }
  }

  @Override
  public void execute(final OfficeTask task) throws OfficeException {

    synchronized (connection) {
      if (!connection.isConnected()) {
        connect();
      }
      task.execute(connection);
    }
  }

  @Override
  public boolean isRunning() {
    return connection.isConnected();
  }

  @Override
  public void start() throws OfficeException {

    if (connectOnStart) {
      synchronized (connection) {
        connect();
      }
    }
  }

  @Override
  public void stop() {

    synchronized (connection) {
      if (connection.isConnected()) {
        connection.disconnect();
      }
    }
  }
}
