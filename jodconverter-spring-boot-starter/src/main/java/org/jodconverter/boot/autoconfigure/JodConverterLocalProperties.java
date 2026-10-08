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

package org.jodconverter.boot.autoconfigure;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.task.LoadDocumentMode;

/**
 * Configuration of JODConverter Local: office processes started and managed by the application. The
 * default values are those of {@link org.jodconverter.local.office.LocalOfficeManager} and {@link
 * org.jodconverter.local.LocalConverter}.
 *
 * @param enabled Enable JODConverter Local, which means that office instances will be launched.
 * @param officeHome Represents the office home directory. If not set, the office installation
 *     directory is auto-detected, the most recent version of LibreOffice first.
 * @param officeExecutable Program that starts the office processes, instead of the executable found
 *     in the office home. Use it for a launcher, such as the one of a snap (/snap/bin/libreoffice)
 *     or an AppImage. When set, the office home is not required.
 * @param hostName Host name that will be used in the --accept argument when starting an office
 *     process. Most of the time, the default will work. But if it doesn't work (unable to connect
 *     to the started process), using 'localhost' as the host name instead may work.
 * @param portNumbers List of ports, separated by commas, used by each JODConverter processing
 *     thread. The number of office instances is equal to the number of port numbers/pipe names,
 *     since 1 office process will be launched for each port number/pipe name. When neither port
 *     numbers, pipe names nor a pool size are set, the port 2002 is used.
 * @param poolSize Number of office processes to start, using free port numbers picked at startup.
 *     An alternative to port numbers and pipe names, which cannot be combined with them.
 * @param pipeNames List of pipe names, separated by commas, used by each JODConverter processing
 *     thread. The number of office instances is equal to the number of port numbers/pipe names,
 *     since 1 office process will be launched for each port number/pipe name.
 * @param workingDir Directory where temporary office profiles will be created. If not set, it
 *     defaults to the system temporary directory as specified by the java.io.tmpdir system
 *     property.
 * @param templateProfileDir Template profile directory to copy to a created office profile
 *     directory when an office processed is launched.
 * @param processManagerClass Class name for explicit office process manager. Type of the provided
 *     process manager. The class must implement the org.jodconverter.local.process.ProcessManager
 *     interface.
 * @param processTimeout Process timeout, used when trying to execute an office process call
 *     (start/connect/terminate). A plain number is in milliseconds.
 * @param processRetryInterval Process retry interval, used for waiting between office process call
 *     tries (start/connect/terminate). A plain number is in milliseconds.
 * @param afterStartProcessDelay Specifies the delay after an attempt to start an office process
 *     before doing anything else. A plain number is in milliseconds.
 * @param existingProcessAction Specifies the action that must be taken when starting a new office
 *     process, and there already is an existing running process for the same connection string.
 * @param startFailFast Controls whether the manager will "fail fast" if an office process cannot be
 *     started or the connection to the started process fails. If set to {@code true}, the start of
 *     a process will wait for the task to be completed, and will throw an exception if the office
 *     process is not started successfully or if the connection to the started process fails,
 *     preventing the application from starting. If set to {@code false}, the task of starting the
 *     process and connecting to it will be submitted and will return immediately, meaning a faster
 *     starting process. Only error logs will be produced if anything goes wrong.
 * @param keepAliveOnShutdown Controls whether the manager will keep the office process alive on
 *     shutdown. If set to {@code true}, the stop task will only disconnect from the office process,
 *     which will stay alive. If set to {@code false}, the office process will be stopped gracefully
 *     (or killed if could not be stopped gracefully).
 * @param taskQueueCapacity Maximum number of tasks waiting in the conversion queue. A task
 *     submitted while the queue is full fails at once. 0 means no limit.
 * @param taskQueueTimeout Maximum living time of a task in the conversion queue. The task will be
 *     removed from the queue if the waiting time is longer than this timeout. A plain number is in
 *     milliseconds.
 * @param taskExecutionTimeout Maximum time allowed to process a task. If the processing time of a
 *     task is longer than this timeout, this task will be aborted and the next task is processed. A
 *     plain number is in milliseconds.
 * @param maxTasksPerProcess Maximum number of tasks an office process can execute before
 *     restarting.
 * @param taskRetries Number of times a conversion is executed again when the office process (or the
 *     connection) executing it is lost. Only the conversions from a file to a file are executed
 *     again. 0 means that such a conversion fails.
 * @param applyDefaultLoadProperties Specifies this converter will apply the default load properties
 *     when loading a source document.
 * @param useUnsafeQuietUpdate Specifies whether this converter will use the unsafe {@code
 *     UpdateDocMode.QUIET_UPDATE} as default for the {@code UpdateDocMode} load property, which was
 *     the default until JODConverter version 4.4.4.
 * @param loadDocumentMode Specifies how a document is loaded/stored when converting a document,
 *     whether it is loaded assuming the office process has access to the file on disk or not. If
 *     not, the conversion process will use stream adapters.
 */
@ConfigurationProperties("jodconverter.local")
public record JodConverterLocalProperties(
    boolean enabled,
    @Nullable String officeHome,
    @Nullable String officeExecutable,
    @DefaultValue(LocalOfficeManager.DEFAULT_HOSTNAME) @NonNull String hostName,
    int @Nullable [] portNumbers,
    @Nullable Integer poolSize,
    @DefaultValue @NonNull String[] pipeNames,
    @Nullable String workingDir,
    @Nullable String templateProfileDir,
    @Nullable String processManagerClass,
    @DefaultValue("120000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration processTimeout,
    @DefaultValue("250") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration processRetryInterval,
    @DefaultValue("0") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration afterStartProcessDelay,
    @DefaultValue("kill") @NonNull ExistingProcessAction existingProcessAction,
    boolean startFailFast,
    boolean keepAliveOnShutdown,
    int taskQueueCapacity,
    @DefaultValue("30000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration taskQueueTimeout,
    @DefaultValue("120000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration taskExecutionTimeout,
    @DefaultValue("200") int maxTasksPerProcess,
    int taskRetries,
    @DefaultValue("true") boolean applyDefaultLoadProperties,
    boolean useUnsafeQuietUpdate,
    @DefaultValue("auto") @NonNull LoadDocumentMode loadDocumentMode)
    implements JodConverterPoolProperties {}
