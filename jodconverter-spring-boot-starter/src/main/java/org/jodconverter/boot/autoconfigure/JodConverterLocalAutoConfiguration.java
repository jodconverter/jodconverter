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

import java.util.HashMap;

import com.sun.star.document.UpdateDocMode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.process.ProcessManager;

/** {@link EnableAutoConfiguration Auto-configuration} for JodConverter local module. */
@AutoConfiguration(after = JodConverterDocumentFormatsAutoConfiguration.class)
@ConditionalOnClass(LocalConverter.class)
@ConditionalOnProperty(prefix = "jodconverter.local", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(JodConverterLocalProperties.class)
public class JodConverterLocalAutoConfiguration {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JodConverterLocalAutoConfiguration.class);

  private final JodConverterLocalProperties properties;

  /**
   * Creates the local autoconfiguration.
   *
   * @param properties The local properties.
   */
  public JodConverterLocalAutoConfiguration(final @NonNull JodConverterLocalProperties properties) {
    this.properties = properties;
  }

  // Creates the OfficeManager bean.
  private OfficeManager createOfficeManager(final ProcessManager processManager) {

    final var builder =
        LocalOfficeManager.builder()
            .officeHome(properties.officeHome())
            .officeExecutable(properties.officeExecutable())
            .hostName(properties.hostName())
            .portNumbers(properties.portNumbers())
            .pipeNames(properties.pipeNames())
            .templateProfileDir(properties.templateProfileDir())
            .existingProcessAction(properties.existingProcessAction())
            .processTimeout(properties.processTimeout().toMillis())
            .processRetryInterval(properties.processRetryInterval().toMillis())
            .afterStartProcessDelay(properties.afterStartProcessDelay().toMillis())
            .startFailFast(properties.startFailFast())
            .keepAliveOnShutdown(properties.keepAliveOnShutdown())
            .maxTasksPerProcess(properties.maxTasksPerProcess())
            .taskRetries(properties.taskRetries());
    properties.applyTo(builder);
    if (properties.poolSize() != null) {
      builder.poolSize(properties.poolSize());
    }
    if (StringUtils.isBlank(properties.processManagerClass())) {
      builder.processManager(processManager);
    } else {
      builder.processManager(properties.processManagerClass());
    }

    // Starts the manager
    return builder.build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "processManager")
  /* default */ ProcessManager processManager() {
    return LocalOfficeUtils.findBestProcessManager();
  }

  @Bean(name = "localOfficeManager", initMethod = "start", destroyMethod = "stop")
  @ConditionalOnMissingBean(name = "localOfficeManager")
  /* default */ OfficeManager localOfficeManager(final ProcessManager processManager) {

    return createOfficeManager(processManager);
  }

  @Bean
  @ConditionalOnMissingBean(name = "localDocumentConverter")
  // The qualifier is required when the remote office manager also exists: since Spring 6.1, a
  // parameter name is no longer used to choose between beans of the same type.
  /* default */ DocumentConverter localDocumentConverter(
      final @Qualifier("localOfficeManager") OfficeManager localOfficeManager,
      final DocumentFormatRegistry documentFormatRegistry,
      final ObjectProvider<PdfOptions> pdfOptions) {

    final var loadProperties = new HashMap<String, Object>();
    if (properties.applyDefaultLoadProperties()) {
      loadProperties.putAll(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      if (properties.useUnsafeQuietUpdate()) {
        loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
      }
    }

    final var builder =
        LocalConverter.builder()
            .officeManager(localOfficeManager)
            .formatRegistry(documentFormatRegistry)
            .loadDocumentMode(properties.loadDocumentMode())
            .loadProperties(loadProperties);
    // Apply the PDF options, from the jodconverter.pdf properties or from the application, to
    // all the conversions to PDF.
    pdfOptions.ifUnique(builder::defaultTargetOptions);
    return builder.build();
  }
}
