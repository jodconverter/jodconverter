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

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for JodConverter connecting to external office
 * processes, which are started and managed outside of the application.
 */
@AutoConfiguration(after = JodConverterLocalAutoConfiguration.class)
@ConditionalOnClass(ExternalOfficeManager.class)
@ConditionalOnProperty(prefix = "jodconverter.external", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(JodConverterExternalProperties.class)
public class JodConverterExternalAutoConfiguration {

  private final JodConverterExternalProperties properties;

  /**
   * Creates the external autoconfiguration.
   *
   * @param properties The external properties.
   */
  public JodConverterExternalAutoConfiguration(
      final @NonNull JodConverterExternalProperties properties) {
    this.properties = properties;
  }

  @Bean(name = "externalOfficeManager", initMethod = "start", destroyMethod = "stop")
  @ConditionalOnMissingBean(name = "externalOfficeManager")
  /* default */ OfficeManager externalOfficeManager() {

    return ExternalOfficeManager.builder()
        .hostName(properties.getHostName())
        .portNumbers(properties.getPortNumbers())
        .pipeNames(properties.getPipeNames())
        .websocketUrls(properties.getWebsocketUrls())
        .workingDir(properties.getWorkingDir())
        .taskQueueTimeout(properties.getTaskQueueTimeout())
        .taskExecutionTimeout(properties.getTaskExecutionTimeout())
        .connectOnStart(properties.isConnectOnStart())
        .connectTimeout(properties.getConnectTimeout())
        .connectRetryInterval(properties.getConnectRetryInterval())
        .connectFailFast(properties.isConnectFailFast())
        .maxTasksPerConnection(properties.getMaxTasksPerConnection())
        .build();
  }

  // Must appear after the externalOfficeManager bean creation. Do not reorder this class by name.
  @Bean
  @ConditionalOnMissingBean(name = "externalDocumentConverter")
  @ConditionalOnBean(name = "externalOfficeManager")
  // The qualifier is required when the local or remote office manager also exists: since Spring
  // 6.1, a parameter name is no longer used to choose between beans of the same type.
  /* default */ DocumentConverter externalDocumentConverter(
      final @Qualifier("externalOfficeManager") OfficeManager externalOfficeManager,
      final ObjectProvider<DocumentFormatRegistry> documentFormatRegistry,
      final ObjectProvider<PdfOptions> pdfOptions) {

    final var builder =
        LocalConverter.builder()
            .officeManager(externalOfficeManager)
            .loadDocumentMode(properties.getLoadDocumentMode())
            .loadProperties(
                properties.isApplyDefaultLoadProperties()
                    ? LocalConverter.DEFAULT_LOAD_PROPERTIES
                    : Map.of());
    // Use the document formats of the local auto-configuration when it also runs.
    documentFormatRegistry.ifAvailable(builder::formatRegistry);
    // Apply the PDF options, from the jodconverter.pdf properties or from the application, to
    // all the conversions to PDF.
    pdfOptions.ifUnique(builder::defaultTargetOptions);
    return builder.build();
  }
}
