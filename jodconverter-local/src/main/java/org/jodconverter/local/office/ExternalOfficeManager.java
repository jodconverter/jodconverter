/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.office.AbstractOfficeManagerPool;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.core.util.AssertUtils;

/**
 * {@link org.jodconverter.core.office.OfficeManager} implementation that uses a pool of external
 * office processes to execute conversion tasks.
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
public final class ExternalOfficeManager
    extends AbstractOfficeManagerPool<ExternalOfficeManagerPoolEntry> {

  // The default value for hostName.
  /* default */ static final String DEFAULT_HOSTNAME = "127.0.0.1";
  // The default value for connection on start.
  /* default */ static final boolean DEFAULT_CONNECT_ON_START = true;
  // The default timeout when connecting to office.
  /* default */ static final long DEFAULT_CONNECT_TIMEOUT = 120_000L; // 2 minutes
  // The default delay between each try to connect.
  /* default */ static final long DEFAULT_CONNECT_RETRY_INTERVAL = 250L; // 0.25 secs.
  // The default "fail fast" behavior when a connection attempt is made.
  /* default */ static final boolean DEFAULT_CONNECT_FAIL_FAST = false;
  // The maximum value for the delay between each try to connect.
  /* default */ static final long MAX_CONNECT_RETRY_INTERVAL = 10_000L; // 10 sec.
  // The default maximum number of tasks an office process can execute before reconnecting.
  /* default */ static final int DEFAULT_MAX_TASKS_PER_CONNECTION = 1_000;

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link ExternalOfficeManager} with default configuration.
   *
   * @return A {@link ExternalOfficeManager} with default configuration.
   */
  public static @NonNull ExternalOfficeManager make() {
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
  public static @NonNull ExternalOfficeManager install() {
    return builder().install().build();
  }

  private ExternalOfficeManager(
      final List<OfficeUrl> officeUrls,
      final File workingDir,
      final boolean connectOnStart,
      final long connectTimeout,
      final long connectRetryInterval,
      final boolean connectFailFast,
      final int maxTasksPerConnection,
      final long taskExecutionTimeout,
      final long taskQueueTimeout) {
    super(officeUrls.size(), workingDir, taskQueueTimeout);

    setEntries(
        officeUrls.stream()
            .map(
                officeUrl ->
                    new ExternalOfficeManagerPoolEntry(
                        connectOnStart,
                        maxTasksPerConnection,
                        taskExecutionTimeout,
                        new ExternalOfficeConnectionManager(
                            connectTimeout,
                            connectRetryInterval,
                            connectFailFast,
                            new OfficeConnection(officeUrl))))
            .collect(Collectors.toList()));
  }

  /**
   * A builder for constructing a {@link ExternalOfficeManager}.
   *
   * @see ExternalOfficeManager
   */
  public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

    // OfficeProcessManager
    private List<String> pipeNames;
    private String hostName = DEFAULT_HOSTNAME;
    private List<Integer> portNumbers;
    private List<String> websocketUrls;
    private Boolean connectOnStart = DEFAULT_CONNECT_ON_START;
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private Long connectRetryInterval = DEFAULT_CONNECT_RETRY_INTERVAL;
    private boolean connectFailFast = DEFAULT_CONNECT_FAIL_FAST;
    private int maxTasksPerConnection = DEFAULT_MAX_TASKS_PER_CONNECTION;

    // Private constructor so only LocalOfficeManager can initialize an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public @NonNull ExternalOfficeManager build() {

      // Validate the working directory
      OfficeUtils.validateWorkingDir(workingDir);

      // Build the manager
      final ExternalOfficeManager manager =
          new ExternalOfficeManager(
              LocalOfficeUtils.buildOfficeUrls(hostName, portNumbers, pipeNames, websocketUrls),
              workingDir,
              connectOnStart,
              connectTimeout,
              connectRetryInterval,
              connectFailFast,
              maxTasksPerConnection,
              taskExecutionTimeout,
              taskQueueTimeout);
      if (install) {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
      return manager;
    }

    /**
     * Specifies host name that will be used to communicate with office.
     *
     * @param hostName The host name to use.
     * @return This builder instance.
     */
    public @NonNull Builder hostName(final String hostName) {
      this.hostName = hostName;
      return this;
    }

    /**
     * Specifies the pipe names that will be used to communicate with office. An instance of office
     * will be launched for each pipe name.
     *
     * @param pipeNames The pipe names to use.
     * @return This builder instance.
     */
    public @NonNull Builder pipeNames(final @Nullable String... pipeNames) {

      if (pipeNames != null && pipeNames.length != 0) {
        this.pipeNames = Arrays.asList(pipeNames);
      }
      return this;
    }

    /**
     * Specifies the port numbers that will be used to communicate with office. An instance of
     * office will be launched for each port number.
     *
     * @param portNumbers The port numbers to use.
     * @return This builder instance.
     */
    public @NonNull Builder portNumbers(final int... portNumbers) {

      if (portNumbers != null && portNumbers.length != 0) {
        this.portNumbers = Arrays.stream(portNumbers).boxed().collect(Collectors.toList());
      }
      return this;
    }

    /**
     * Specifies the websocket urls that will be used to communicate with office. An instance of
     * office will be launched for each websocket url.
     *
     * @param websocketUrls The websocket urls to use.
     * @return This builder instance.
     */
    public @NonNull Builder websocketUrls(final @Nullable String... websocketUrls) {

      if (websocketUrls != null && websocketUrls.length != 0) {
        this.websocketUrls = Arrays.asList(websocketUrls);
      }
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
    public @NonNull Builder connectOnStart(final @Nullable Boolean connectOnStart) {

      if (connectOnStart != null) {
        this.connectOnStart = connectOnStart;
      }
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
    public @NonNull Builder connectTimeout(final @Nullable Long connectTimeout) {

      if (connectTimeout != null) {
        AssertUtils.isTrue(
            connectTimeout >= 0L,
            String.format("connectTimeout %s must be greater than or equal to 0", connectTimeout));
        this.connectTimeout = connectTimeout;
      }
      return this;
    }

    /**
     * Specifies the delay, in milliseconds, between each try when trying to connect to office.
     *
     * <p>&nbsp; <b><i>Default</i></b>: 250 (0.25 seconds)
     *
     * @param connectRetryInterval the connection retry interval, in milliseconds.
     * @return This builder instance.
     */
    public @NonNull Builder connectRetryInterval(final @Nullable Long connectRetryInterval) {

      if (connectRetryInterval != null) {
        AssertUtils.isTrue(
            connectRetryInterval >= 0L && connectRetryInterval <= MAX_CONNECT_RETRY_INTERVAL,
            String.format(
                "retryInterval %s must be in the inclusive range of %s to %s",
                connectRetryInterval, 0, MAX_CONNECT_RETRY_INTERVAL));
        this.connectRetryInterval = connectRetryInterval;
      }
      return this;
    }

    /**
     * Controls whether the manager will "fail fast" if the connection to the external process
     * fails. If set to {@code true}, a connection attempt will wait for the task to be completed,
     * and will throw an exception if the connection to the external process fails. If set to {@code
     * false}, the task of connecting to the external process will be submitted and will return
     * immediately, meaning a faster connecting process. Only error logs will be produced if
     * anything goes wrong.
     *
     * <p>&nbsp; <b><i>Default</i></b>: false
     *
     * @param connectFailFast {@code true} to "fail fast", {@code false} otherwise.
     * @return This builder instance.
     */
    public @NonNull Builder connectFailFast(final @Nullable Boolean connectFailFast) {

      if (connectFailFast != null) {
        this.connectFailFast = connectFailFast;
      }
      return this;
    }

    /**
     * Specifies the maximum number of tasks an office process can execute before reconnecting to
     * it. 0 means infinite number of task (will never reconnect).
     *
     * <p>&nbsp; <b><i>Default</i></b>: 1000
     *
     * @param maxTasksPerConnection The new maximum number of tasks an office process can execute.
     * @return This builder instance.
     */
    public @NonNull Builder maxTasksPerConnection(final @Nullable Integer maxTasksPerConnection) {

      if (maxTasksPerConnection != null) {
        AssertUtils.isTrue(
            maxTasksPerConnection >= 0,
            String.format(
                "maxTasksPerConnection %s must be greater than or equal to 0",
                maxTasksPerConnection));
        this.maxTasksPerConnection = maxTasksPerConnection;
      }
      return this;
    }
  }
}
