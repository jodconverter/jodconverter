/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.local.office;

import java.io.File;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.AbstractOfficeManager;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.StringUtils;

/**
 * {@link org.jodconverter.core.office.OfficeManager} implementation that connects to an external
 * Office process.
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
 * <p>This {@link org.jodconverter.core.office.OfficeManager} implementation basically provides the
 * same behavior as JODConverter 2.x, including using <em>synchronized</em> blocks for serializing
 * office operations.
 */
public final class ExternalOfficeManager extends AbstractOfficeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalOfficeManager.class);

  // The default port number to connect to office.
  public static final int DEFAULT_PORT_NUMBER = 2002;
  // The default pipe name to connect to office.
  public static final String DEFAULT_PIPE_NAME = "office";
  // The default value for connection on start.
  public static final boolean DEFAULT_CONNECT_ON_START = true;
  // The default initial delay when connecting to office.
  public static final long DEFAULT_INITIAL_DELAY = 0L; // No delay
  // The default timeout when connecting to office.
  public static final long DEFAULT_CONNECT_TIMEOUT = 120_000L; // 2 minutes
  // The default delay between each try to connect.
  public static final long DEFAULT_RETRY_INTERVAL = 250L; // 0.25 secs.
  // The maximum value for the delay between each try to connect.
  public static final long MAX_RETRY_INTERVAL = 10_000L; // 10 sec.

  private final boolean connectOnStart;
  private final long connectTimeout;
  private final long retryInterval;
  private final OfficeConnection connection;

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link ExternalOfficeManager} with default configuration.
   *
   * @return A {@link ExternalOfficeManager} with default configuration.
   */
  @NonNull
  public static ExternalOfficeManager make() {
    return builder().build();
  }

  /**
   * Creates a new {@link ExternalOfficeManager} with default configuration. The created manager
   * will then be the unique instance of the {@link
   * org.jodconverter.core.office.InstalledOfficeManagerHolder} class. Note that if the {@code
   * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the owner
   * of this existing manager is responsible to stopped it.
   *
   * @return A {@link ExternalOfficeManager} with default configuration.
   */
  @NonNull
  public static ExternalOfficeManager install() {
    return builder().install().build();
  }

  /**
   * Constructs a new instance of the class with the specified arguments.
   *
   * @param officeUrl The office URL.
   * @param workingDir The directory where temporary files and directories are created.
   * @param connectOnStart Should a connection be attempted on start? If {@code false}, a connection
   *     will only be attempted the first time an {@link org.jodconverter.core.task.OfficeTask} is
   *     executed.
   * @param connectTimeout Timeout after which a connection attempt will fail.
   * @param retryInterval Timeout between each try to connect.
   */
  private ExternalOfficeManager(
      final OfficeUrl officeUrl,
      final File workingDir,
      final Boolean connectOnStart,
      final Long connectTimeout,
      final Long retryInterval) {
    super(workingDir);

    connection = new OfficeConnection(officeUrl);

    this.connectOnStart = connectOnStart == null ? DEFAULT_CONNECT_ON_START : connectOnStart;
    this.connectTimeout = connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
    this.retryInterval = retryInterval == null ? DEFAULT_RETRY_INTERVAL : retryInterval;
  }

  private void connect() throws OfficeException {

    LOGGER.debug("Connecting to external office process...");
    try {
      // TODO: Add configuration field for initial delay
      new ConnectRetryable(connection)
          .execute(DEFAULT_INITIAL_DELAY, retryInterval, connectTimeout);

    } catch (Exception ex) {
      throw new OfficeException("Could not establish connection to external office process", ex);
    }
  }

  @Override
  public void execute(@NonNull final OfficeTask task) throws OfficeException {

    synchronized (connection) {
      if (!isRunning()) {
        connect();
      }
      LOGGER.debug("Executing task: {}", task);
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
    private Integer portNumber;
    private String pipeName;
    private Boolean connectOnStart;
    private Long connectTimeout;
    private Long retryInterval;

    // Private constructor so only LocalOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    @NonNull
    @Override
    public ExternalOfficeManager build() {

      if (workingDir == null) {
        workingDir = OfficeUtils.getDefaultWorkingDir();
      }

      // Validate the office directories
      LocalOfficeUtils.validateOfficeWorkingDirectory(workingDir);

      // Build the manager
      final ExternalOfficeManager manager =
          new ExternalOfficeManager(
              connectionProtocol == OfficeConnectionProtocol.SOCKET
                  ? portNumber == null
                      ? new OfficeUrl(DEFAULT_PORT_NUMBER)
                      : new OfficeUrl(portNumber)
                  : pipeName == null ? new OfficeUrl(DEFAULT_PIPE_NAME) : new OfficeUrl(pipeName),
              workingDir,
              connectOnStart,
              connectTimeout,
              retryInterval);
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
    @NonNull
    public Builder connectionProtocol(@Nullable final OfficeConnectionProtocol connectionProtocol) {

      this.connectionProtocol = connectionProtocol;
      return this;
    }

    /**
     * Specifies the pipe name that will be use to communicate with office.
     *
     * @param pipeName The pipe name to use.
     * @return This builder instance.
     */
    @NonNull
    public Builder pipeName(@Nullable final String pipeName) {

      if (StringUtils.isNotBlank(pipeName)) {
        this.pipeName = pipeName;
      }
      return this;
    }

    /**
     * Specifies the port number that will be use to communicate with office.
     *
     * @param portNumber The port number to use.
     * @return This builder instance.
     */
    @NonNull
    public Builder portNumber(@Nullable final Integer portNumber) {

      this.portNumber = portNumber;
      return this;
    }

    /**
     * Specifies whether a connection must be attempted on {@link #start()}? If <em>false</em>, a
     * connection will only be attempted the first time an {@link OfficeTask} is executed.
     *
     * <p>&nbsp; <b><i>Default</i></b>: true
     *
     * @param connectOnStart {@code true} to connect on start, {@code false} otherwise.
     * @return This builder instance.
     */
    @NonNull
    public Builder connectOnStart(@Nullable final Boolean connectOnStart) {

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
    @NonNull
    public Builder connectTimeout(@Nullable final Long connectTimeout) {

      if (connectTimeout != null) {
        AssertUtils.isTrue(
            connectTimeout >= 0,
            String.format("connectTimeout %s must be greater than or equal to 0", connectTimeout));
      }
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
    @NonNull
    public Builder retryInterval(@Nullable final Long retryInterval) {

      if (retryInterval != null) {
        AssertUtils.isTrue(
            retryInterval >= 0 && retryInterval <= MAX_RETRY_INTERVAL,
            String.format(
                "retryInterval %s must be in the inclusive range of %s to %s",
                retryInterval, 0, MAX_RETRY_INTERVAL));
      }
      this.retryInterval = retryInterval;
      return this;
    }
  }
}
