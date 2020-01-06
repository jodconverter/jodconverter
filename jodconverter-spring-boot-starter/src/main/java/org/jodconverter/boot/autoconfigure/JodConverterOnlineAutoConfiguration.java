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

import java.util.Optional;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.jodconverter.DocumentConverter;
import org.jodconverter.OnlineConverter;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OnlineOfficeManager;
import org.jodconverter.ssl.SslConfig;

/** {@link EnableAutoConfiguration Auto-configuration} for JodConverter online module. */
@Configuration
@ConditionalOnClass(OnlineConverter.class)
@ConditionalOnProperty(
    prefix = "jodconverter.online",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
@EnableConfigurationProperties(JodConverterOnlineProperties.class)
public class JodConverterOnlineAutoConfiguration {

  private final JodConverterOnlineProperties properties;

  public JodConverterOnlineAutoConfiguration(final JodConverterOnlineProperties properties) {
    this.properties = properties;
  }

  // Creates the OfficeManager bean.
  private OfficeManager createOfficeManager() {

    final OnlineOfficeManager.Builder builder = OnlineOfficeManager.builder();

    builder.urlConnection(properties.getUrl());
    builder.poolSize(properties.getPoolSize());
    builder.workingDir(properties.getWorkingDir());
    builder.taskExecutionTimeout(properties.getTaskExecutionTimeout());
    builder.taskQueueTimeout(properties.getTaskQueueTimeout());
    builder.sslConfig(
        Optional.ofNullable(properties.getSsl())
            .map(
                ssl -> {
                  final SslConfig sslConfig = new SslConfig();
                  sslConfig.setEnabled(ssl.isEnabled());
                  sslConfig.setCiphers(ssl.getCiphers());
                  sslConfig.setKeyAlias(ssl.getKeyAlias());
                  sslConfig.setKeyPassword(ssl.getKeyPassword());
                  sslConfig.setKeyStore(ssl.getKeyStore());
                  sslConfig.setKeyStorePassword(ssl.getKeyStorePassword());
                  sslConfig.setKeyStoreType(ssl.getKeyStoreType());
                  sslConfig.setKeyStoreProvider(ssl.getKeyStoreProvider());
                  sslConfig.setEnabledProtocols(ssl.getEnabledProtocols());
                  sslConfig.setTrustStore(ssl.getTrustStore());
                  sslConfig.setTrustStorePassword(ssl.getTrustStorePassword());
                  sslConfig.setTrustStoreType(ssl.getTrustStoreType());
                  sslConfig.setTrustStoreProvider(ssl.getTrustStoreProvider());
                  sslConfig.setProtocol(ssl.getProtocol());
                  sslConfig.setVerifyHostname(ssl.isVerifyHostname());
                  return sslConfig;
                })
            .orElse(null));

    // Starts the manager
    return builder.build();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnMissingBean(name = "onlineOfficeManager")
  public OfficeManager onlineOfficeManager() {

    return createOfficeManager();
  }

  // Must appear after the OfficeManager bean creation. Do not reorder this class by name.
  @Bean
  @ConditionalOnMissingBean(name = "onlineDocumentConverter")
  @ConditionalOnBean(name = "onlineOfficeManager")
  public DocumentConverter onlineDocumentConverter(final OfficeManager onlineOfficeManager) {

    return OnlineConverter.make(onlineOfficeManager);
  }
}
