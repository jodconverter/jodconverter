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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import org.jodconverter.core.office.AbstractOfficeWorkerPool;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.jodconverter.remote.ssl.SslConfig;

/**
 * Checks that the default values written in the properties records are those of the office managers
 * and converters they configure.
 */
class JodConverterPropertiesTest {

  private static <T> T bind(final String prefix, final Class<T> type) {
    return new Binder(new MapConfigurationPropertySource(Map.of()))
        .bindOrCreate(prefix, Bindable.of(type));
  }

  @Test
  void remoteDefaults_ShouldMatchTheManagerAndTheSslConfig() {

    final var properties = bind("jodconverter.remote", JodConverterRemoteProperties.class);

    assertThat(properties.enabled()).isFalse();
    assertThat(properties.connectTimeout().toMillis())
        .isEqualTo(RemoteOfficeManager.DEFAULT_CONNECT_TIMEOUT);
    assertThat(properties.socketTimeout().toMillis())
        .isEqualTo(RemoteOfficeManager.DEFAULT_SOCKET_TIMEOUT);
    assertThat(properties.poolSize()).isEqualTo(RemoteOfficeManager.DEFAULT_POOL_SIZE);
    assertThat(properties.taskQueueCapacity())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_QUEUE_CAPACITY);
    assertThat(properties.taskQueueTimeout().toMillis())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_QUEUE_TIMEOUT);
    assertThat(properties.taskExecutionTimeout().toMillis())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_EXECUTION_TIMEOUT);
    assertThat(properties.ssl()).isNull();
    // Trusting every certificate must be an explicit choice.
    assertThat(new SslConfig().isTrustAll()).isFalse();
  }

  @Test
  void localDefaults_ShouldMatchTheManagerAndTheConverter() {

    final var properties = bind("jodconverter.local", JodConverterLocalProperties.class);

    assertThat(properties.enabled()).isFalse();
    assertThat(properties.hostName()).isEqualTo(LocalOfficeManager.DEFAULT_HOSTNAME);
    assertThat(properties.portNumbers()).isNull();
    assertThat(properties.poolSize()).isNull();
    assertThat(properties.pipeNames()).isEmpty();
    assertThat(properties.processTimeout().toMillis())
        .isEqualTo(LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT);
    assertThat(properties.processRetryInterval().toMillis())
        .isEqualTo(LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL);
    assertThat(properties.afterStartProcessDelay().toMillis())
        .isEqualTo(LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY);
    assertThat(properties.existingProcessAction())
        .isEqualTo(LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION);
    assertThat(properties.startFailFast()).isEqualTo(LocalOfficeManager.DEFAULT_START_FAIL_FAST);
    assertThat(properties.keepAliveOnShutdown())
        .isEqualTo(LocalOfficeManager.DEFAULT_KEEP_ALIVE_ON_SHUTDOWN);
    assertThat(properties.taskQueueCapacity())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_QUEUE_CAPACITY);
    assertThat(properties.taskQueueTimeout().toMillis())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_QUEUE_TIMEOUT);
    assertThat(properties.taskExecutionTimeout().toMillis())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_EXECUTION_TIMEOUT);
    assertThat(properties.maxTasksPerProcess())
        .isEqualTo(LocalOfficeManager.DEFAULT_MAX_TASKS_PER_PROCESS);
    assertThat(properties.applyDefaultLoadProperties())
        .isEqualTo(LocalConverter.DEFAULT_APPLY_DEFAULT_LOAD_PROPS);
    assertThat(properties.useUnsafeQuietUpdate())
        .isEqualTo(LocalConverter.DEFAULT_USE_UNSAFE_QUIET_UPDATE);
    assertThat(properties.loadDocumentMode()).isEqualTo(LocalConverter.DEFAULT_LOAD_DOCUMENT_MODE);
  }

  @Test
  void externalDefaults_ShouldMatchTheManagerAndTheConverter() {

    final var properties = bind("jodconverter.external", JodConverterExternalProperties.class);

    assertThat(properties.enabled()).isFalse();
    assertThat(properties.hostName()).isEqualTo(ExternalOfficeManager.DEFAULT_HOSTNAME);
    assertThat(properties.connectOnStart())
        .isEqualTo(ExternalOfficeManager.DEFAULT_CONNECT_ON_START);
    assertThat(properties.connectTimeout().toMillis())
        .isEqualTo(ExternalOfficeManager.DEFAULT_CONNECT_TIMEOUT);
    assertThat(properties.connectRetryInterval().toMillis())
        .isEqualTo(ExternalOfficeManager.DEFAULT_CONNECT_RETRY_INTERVAL);
    assertThat(properties.connectFailFast())
        .isEqualTo(ExternalOfficeManager.DEFAULT_CONNECT_FAIL_FAST);
    assertThat(properties.maxTasksPerConnection())
        .isEqualTo(ExternalOfficeManager.DEFAULT_MAX_TASKS_PER_CONNECTION);
    assertThat(properties.taskQueueTimeout().toMillis())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_QUEUE_TIMEOUT);
    assertThat(properties.taskExecutionTimeout().toMillis())
        .isEqualTo(AbstractOfficeWorkerPool.DEFAULT_TASK_EXECUTION_TIMEOUT);
    assertThat(properties.applyDefaultLoadProperties())
        .isEqualTo(LocalConverter.DEFAULT_APPLY_DEFAULT_LOAD_PROPS);
    assertThat(properties.loadDocumentMode()).isEqualTo(LocalConverter.DEFAULT_LOAD_DOCUMENT_MODE);
  }

  @Test
  void lenientValues_ShouldBindDurationsAndEnums() {

    final var properties =
        new Binder(
                new MapConfigurationPropertySource(
                    Map.of(
                        "jodconverter.local.process-timeout", "30s",
                        "jodconverter.local.existing-process-action", "connect-or-kill",
                        "jodconverter.local.load-document-mode", "Remote")))
            .bindOrCreate("jodconverter.local", Bindable.of(JodConverterLocalProperties.class));

    assertThat(properties.processTimeout().toMillis()).isEqualTo(30_000L);
    assertThat(properties.existingProcessAction())
        .isEqualTo(org.jodconverter.local.office.ExistingProcessAction.CONNECT_OR_KILL);
    assertThat(properties.loadDocumentMode())
        .isEqualTo(org.jodconverter.local.task.LoadDocumentMode.REMOTE);
  }
}
