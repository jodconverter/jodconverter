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

import org.jodconverter.local.task.LoadDocumentMode;

/**
 * Configuration of the attached office manager: it attaches to office processes that are started
 * and managed outside of the application (another container, a service manager...), on this machine
 * or on another one. The default values are those of {@link
 * org.jodconverter.local.office.AttachedOfficeManager} and {@link
 * org.jodconverter.local.LocalConverter}. These were the {@code jodconverter.external} properties
 * before 5.0.
 *
 * @param enabled Enable the attached office manager, which connects to already running office
 *     processes.
 * @param hostName Host name of the office processes to connect to, used with the port numbers.
 * @param portNumbers List of ports, separated by commas, of the office processes to connect to. One
 *     connection is made for each port number/pipe name/websocket URL. When none of them is set,
 *     the port 2002 is used.
 * @param pipeNames List of pipe names, separated by commas, of the office processes to connect to.
 * @param websocketUrls List of websocket URLs, separated by commas, of the office processes to
 *     connect to.
 * @param workingDir Directory where temporary files will be created. If not set, it defaults to the
 *     system temporary directory as specified by the java.io.tmpdir system property.
 * @param taskQueueCapacity Maximum number of tasks waiting in the conversion queue. A task
 *     submitted while the queue is full fails at once. 0 means no limit.
 * @param taskQueueTimeout Maximum living time of a task in the conversion queue. The task will be
 *     removed from the queue if the waiting time is longer than this timeout. A plain number is in
 *     milliseconds.
 * @param taskExecutionTimeout Maximum time allowed to process a task. If the processing time of a
 *     task is longer than this timeout, this task will be aborted and the next task is processed. A
 *     plain number is in milliseconds.
 * @param connectOnStart Whether the connections are made when the manager starts, or on the first
 *     conversion.
 * @param connectTimeout Timeout after which a connection attempt is considered failed. A plain
 *     number is in milliseconds.
 * @param connectRetryInterval Delay between each connection attempt. A plain number is in
 *     milliseconds.
 * @param connectFailFast Whether the manager start fails when a connection cannot be made, instead
 *     of retrying in the background.
 * @param maxTasksPerConnection Maximum number of tasks executed through a connection before
 *     reconnecting. 0 means an infinite number of tasks (never reconnects).
 * @param applyDefaultLoadProperties Whether the default load properties (Hidden, ReadOnly and
 *     UpdateDocMode NO_UPDATE) are applied when loading a document.
 * @param loadDocumentMode How documents are loaded and stored: local (the office process reads and
 *     writes the files directly), remote (documents are streamed, for an office process running on
 *     another host or container) or auto.
 */
@ConfigurationProperties("jodconverter.attached")
public record JodConverterAttachedProperties(
    boolean enabled,
    @DefaultValue("127.0.0.1") @NonNull String hostName,
    int @Nullable [] portNumbers,
    @Nullable String[] pipeNames,
    @Nullable String[] websocketUrls,
    @Nullable String workingDir,
    int taskQueueCapacity,
    @DefaultValue("30000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration taskQueueTimeout,
    @DefaultValue("120000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration taskExecutionTimeout,
    @DefaultValue("true") boolean connectOnStart,
    @DefaultValue("120000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration connectTimeout,
    @DefaultValue("250") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration connectRetryInterval,
    boolean connectFailFast,
    @DefaultValue("1000") int maxTasksPerConnection,
    @DefaultValue("true") boolean applyDefaultLoadProperties,
    @DefaultValue("auto") @NonNull LoadDocumentMode loadDocumentMode)
    implements JodConverterPoolProperties {}
