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

package org.jodconverter.boot.autoconfigure;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;

/** {@link EnableAutoConfiguration Auto-configuration} for JodConverter remote module. */
@Configuration
@ConditionalOnClass(RemoteConverter.class)
@ConditionalOnProperty(prefix = "jodconverter.remote", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(JodConverterRemoteProperties.class)
public class JodConverterRemoteAutoConfiguration {

  private final JodConverterRemoteProperties properties;

  /**
   * Creates the remote auto configuration.
   *
   * @param properties The remote properties.
   */
  public JodConverterRemoteAutoConfiguration(
      @NonNull final JodConverterRemoteProperties properties) {
    this.properties = properties;
  }

  // Creates the OfficeManager bean.
  private OfficeManager createOfficeManager() {

    AssertUtils.notNull(properties.getUrl(), "urlConnection is required");

    final RemoteOfficeManager.Builder builder = RemoteOfficeManager.builder();

    builder.urlConnection(properties.getUrl());
    builder.poolSize(properties.getPoolSize());
    builder.workingDir(properties.getWorkingDir());
    builder.taskExecutionTimeout(properties.getTaskExecutionTimeout());
    builder.taskQueueTimeout(properties.getTaskQueueTimeout());
    builder.connectTimeout(properties.getConnectTimeout());
    builder.socketTimeout(properties.getSocketTimeout());
    if (properties.getSsl() != null) {
      builder.sslConfig(properties.getSsl().sslConfig());
    }

    // Starts the manager
    return builder.build();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnMissingBean(name = "remoteOfficeManager")
  /* default */ OfficeManager remoteOfficeManager() {

    return createOfficeManager();
  }

  // Must appear after the OfficeManager bean creation. Do not reorder this class by name.
  @Bean
  @ConditionalOnMissingBean(name = "remoteDocumentConverter")
  @ConditionalOnBean(name = "remoteOfficeManager")
  /* default */ DocumentConverter remoteDocumentConverter(final OfficeManager remoteOfficeManager) {

    return RemoteConverter.make(remoteOfficeManager);
  }
}
