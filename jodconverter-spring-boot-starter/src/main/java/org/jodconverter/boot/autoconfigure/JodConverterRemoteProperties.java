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
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

import org.jodconverter.remote.ssl.SslConfig;

/**
 * Configuration of JODConverter Remote: conversions sent to a LibreOffice Online server. The
 * default values are those of {@link org.jodconverter.remote.office.RemoteOfficeManager}.
 *
 * @param enabled Enable JODConverter Remote.
 * @param url The URL to the LibreOffice Online server.
 * @param connectTimeout The timeout until a connection is established. A timeout value of zero is
 *     interpreted as an infinite timeout. A negative value is interpreted as undefined (system
 *     default). A plain number is in milliseconds.
 * @param socketTimeout The socket timeout, which is the timeout for waiting for data or, put
 *     differently, a maximum period inactivity between two consecutive data packets. A timeout
 *     value of zero is interpreted as an infinite timeout. A negative value is interpreted as
 *     undefined (system default). A plain number is in milliseconds.
 * @param poolSize Pool size of the manager.
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
 * @param ssl The SSL configuration of the connection to the LibreOffice Online server, if it uses
 *     HTTPS.
 */
@ConfigurationProperties("jodconverter.remote")
public record JodConverterRemoteProperties(
    boolean enabled,
    @Nullable String url,
    @DefaultValue("60000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration connectTimeout,
    @DefaultValue("120000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration socketTimeout,
    @DefaultValue("1") int poolSize,
    @Nullable String workingDir,
    int taskQueueCapacity,
    @DefaultValue("30000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration taskQueueTimeout,
    @DefaultValue("120000") @DurationUnit(ChronoUnit.MILLIS) @NonNull Duration taskExecutionTimeout,
    @NestedConfigurationProperty @Nullable SslConfig ssl)
    implements JodConverterPoolProperties {}
