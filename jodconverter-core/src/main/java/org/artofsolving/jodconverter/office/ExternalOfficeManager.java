/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.office;

import com.sun.star.lib.uno.helper.UnoUrl;

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
 * <p>This {@link OfficeManager} implementation basically provides the same behaviour as
 * JODConverter 2.x, including using <em>synchronized</em> blocks for serialising office operations.
 */
class ExternalOfficeManager implements OfficeManager {

  private final OfficeConnection connection;
  private final boolean connectOnStart;

  /**
   * Construcst a new instance of the class.
   *
   * @param unoUrl the uno URL.
   * @param connectOnStart should a connection be attempted on {@link #start()}? Default is
   *     <em>true</em>. If <em>false</em>, a connection will only be attempted the first time an
   *     {@link OfficeTask} is executed.
   */
  public ExternalOfficeManager(final UnoUrl unoUrl, final boolean connectOnStart) {

    connection = new OfficeConnection(unoUrl);
    this.connectOnStart = connectOnStart;
  }

  private void connect() throws OfficeException {

    try {
      connection.connect();
    } catch (OfficeConnectionException connectionEx) {
      throw new OfficeException("Could not connect to external office process", connectionEx);
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
