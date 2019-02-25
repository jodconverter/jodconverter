/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

import java.io.File;

import org.apache.commons.lang3.Validate;

import org.jodconverter.task.OfficeTask;

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
public final class ExternalOfficeManager extends AbstractOfficeManager {

  /** The default port number to connect to office. */
  public static final int DEFAULT_PORT_NUMBER = 2002;
  /** The default pipe name to connect to office. */
  public static final String DEFAULT_PIPE_NAME = "office";
  /** The default timeout when connecting to office. */
  public static final long DEFAULT_CONNECT_TIMEOUT = 120000L; // 2 minutes
  /** The default delay between each try to connect. */
  public static final long DEFAULT_RETRY_INTERVAL = 250L; // 0.25 secs.
  /** The maximum value for the delay between each try to connect. */
  public static final long MAX_RETRY_INTERVAL = 10000L; // 10 sec.

  private final OfficeConnection connection;

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link ExternalOfficeManager} with default configuration.
   *
   * @return A {@link ExternalOfficeManager} with default configuration.
   */
  public static ExternalOfficeManager make() {
    return builder().build();
  }

  /**
   * Creates a new {@link ExternalOfficeManager} with default configuration. The created manager
   * will then be the unique instance of the {@link InstalledOfficeManagerHolder} class. Note that
   * if the {@code InstalledOfficeManagerHolder} class already holds an {@code OfficeManager}
   * instance, the owner of this existing manager is responsible to stopped it.
   *
   * @return A {@link ExternalOfficeManager} with default configuration.
   */
  public static ExternalOfficeManager install() {
    return builder().install().build();
  }

  /**
   * Constructs a new instance of the class with the specified arguments.
   *
   * @param officeUrl The office URL.
   * @param config The manager configuration.
   */
  private ExternalOfficeManager(
      final OfficeUrl officeUrl, final ExternalOfficeManagerConfig config) {
    super(config);

    connection = new OfficeConnection(officeUrl);
  }

  private void connect() throws OfficeException {

    try {
      final ExternalOfficeManagerConfig mconfig = (ExternalOfficeManagerConfig) config;
      new ConnectRetryable(connection)
          .execute(mconfig.getRetryInterval(), mconfig.getConnectTimeout());

    } catch (Exception ex) {
      throw new OfficeException("Could not establish connection to external office process", ex);
    }
  }

  @Override
  public void execute(final OfficeTask task) throws OfficeException {

    synchronized (connection) {
      if (!isRunning()) {
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

    if (((ExternalOfficeManagerConfig) config).isConnectOnStart()) {
      synchronized (connection) {
        connect();

        makeTempDir();
      }
    }
  }

  @Override
  public void stop() {

    synchronized (connection) {
      if (isRunning()) {
        try {
          connection.disconnect();
        } finally {
          deleteTempDir();
        }
      }
    }
  }

  /**
   * A builder for constructing a {@link ExternalOfficeManager}.
   *
   * @see ExternalOfficeManager
   */
  public static final class Builder extends AbstractOfficeManagerBuilder<Builder> {

    // OfficeProcessManager
    private OfficeConnectionProtocol connectionProtocol = OfficeConnectionProtocol.SOCKET;
    private int portNumber = DEFAULT_PORT_NUMBER;
    private String pipeName = DEFAULT_PIPE_NAME;
    private boolean connectOnStart = true;
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private long retryInterval = DEFAULT_RETRY_INTERVAL;

    // Private ctor so only LocalOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public ExternalOfficeManager build() {

      if (workingDir == null) {
        workingDir = new File(System.getProperty("java.io.tmpdir"));
      }

      // Validate the office directories
      LocalOfficeUtils.validateOfficeWorkingDirectory(workingDir);

      // Build the manager
      final ExternalOfficeManager manager =
          new ExternalOfficeManager(
              connectionProtocol == OfficeConnectionProtocol.SOCKET
                  ? new OfficeUrl(portNumber)
                  : pipeName != null ? new OfficeUrl(pipeName) : new OfficeUrl(2002),
              new ExternalOfficeManagerConfig(
                  workingDir, connectOnStart, connectTimeout, retryInterval));
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
    }

    /**
     * Specifies the connection protocol.
     *
     * @param connectionProtocol The new protocol to use.
     * @return This builder instance.
     */
    public Builder connectionProtocol(final OfficeConnectionProtocol connectionProtocol) {

      Validate.notNull(connectionProtocol, "The protocol must not be null");
      this.connectionProtocol = connectionProtocol;
      return this;
    }

    /**
     * Specifies the pipe name that will be use to communicate with office.
     *
     * @param pipeName The pipe name to use.
     * @return This builder instance.
     */
    public Builder pipeName(final String pipeName) {

      Validate.notBlank(pipeName, "The pipe name must not be blank");
      this.pipeName = pipeName;
      return this;
    }

    /**
     * Specifies the port number that will be use to communicate with office.
     *
     * @param portNumber The port number to use.
     * @return This builder instance.
     */
    public Builder portNumber(final int portNumber) {

      this.portNumber = portNumber;
      return this;
    }

    /**
     * Specifies whether a connection must be attempted on {@link #start()}? If <em>false</em>, a
     * connection will only be attempted the first time an {@link OfficeTask} is executed.
     *
     * <p>&nbsp; <b><i>Default</i></b>: truw
     *
     * @param connectOnStart {@code true} to connect on start, {@code false} otherwise.
     * @return This builder instance.
     */
    public Builder connectOnStart(final boolean connectOnStart) {

      this.connectOnStart = connectOnStart;
      return this;
    }

    /**
     * Specifies the timeout, in milliseconds, after which a connection attempt will fail.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
     *
     * @param connectTimeout the process timeout, in milliseconds.
     * @return This builder instance.
     */
    public Builder connectTimeout(final long connectTimeout) {

      Validate.inclusiveBetween(
          0,
          Long.MAX_VALUE,
          connectTimeout,
          String.format(
              "The connectTimeout %s must be greater than or equal to 0", connectTimeout));
      this.connectTimeout = connectTimeout;
      return this;
    }

    /**
     * Specifies the delay, in milliseconds, between each try when trying to connect to office.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
     *
     * @param retryInterval the retry interval, in milliseconds.
     * @return This builder instance.
     */
    public Builder retryInterval(final long retryInterval) {

      Validate.inclusiveBetween(
          0,
          MAX_RETRY_INTERVAL,
          retryInterval,
          String.format(
              "The retryInterval %s must be in the inclusive range of %s to %s",
              retryInterval, 0, MAX_RETRY_INTERVAL));
      this.retryInterval = retryInterval;
      return this;
    }
  }
}
