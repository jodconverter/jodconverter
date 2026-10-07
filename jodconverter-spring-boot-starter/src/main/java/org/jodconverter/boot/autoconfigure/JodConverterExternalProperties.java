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
 * The former {@code jodconverter.external} properties, read when {@code
 * jodconverter.attached.enabled} is not set: they configure the same {@link
 * org.jodconverter.local.office.AttachedOfficeManager} as {@link JodConverterAttachedProperties}.
 *
 * @param enabled Enable the attached office manager through the former properties.
 * @param hostName The host name of the office processes.
 * @param portNumbers The port numbers of the office processes.
 * @param pipeNames The pipe names of the office processes.
 * @param websocketUrls The websocket URLs of the office processes.
 * @param workingDir The directory where temporary files are created.
 * @param taskQueueCapacity The maximum number of tasks waiting in the queue; 0 means no limit.
 * @param taskQueueTimeout The maximum time a task waits in the queue.
 * @param taskExecutionTimeout The maximum time allowed to execute a task.
 * @param connectOnStart Whether the manager connects to the office processes when it starts.
 * @param connectTimeout The maximum time to connect to an office process.
 * @param connectRetryInterval The delay between two connection attempts.
 * @param connectFailFast Whether the start fails at once when a connection cannot be made.
 * @param maxTasksPerConnection The maximum number of tasks a connection executes before
 *     reconnecting.
 * @param applyDefaultLoadProperties Whether the default load properties are applied.
 * @param loadDocumentMode How the documents are loaded.
 * @deprecated Use the {@code jodconverter.attached} properties, {@link
 *     JodConverterAttachedProperties}.
 */
@Deprecated(since = "5.0", forRemoval = true)
@ConfigurationProperties("jodconverter.external")
public record JodConverterExternalProperties(
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
    @DefaultValue("auto") @NonNull LoadDocumentMode loadDocumentMode) {

  /**
   * Gives these properties as attached properties.
   *
   * @return The same values, as {@link JodConverterAttachedProperties}.
   */
  public @NonNull JodConverterAttachedProperties toAttached() {
    return new JodConverterAttachedProperties(
        enabled,
        hostName,
        portNumbers,
        pipeNames,
        websocketUrls,
        workingDir,
        taskQueueCapacity,
        taskQueueTimeout,
        taskExecutionTimeout,
        connectOnStart,
        connectTimeout,
        connectRetryInterval,
        connectFailFast,
        maxTasksPerConnection,
        applyDefaultLoadProperties,
        loadDocumentMode);
  }
}
