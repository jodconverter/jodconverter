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

import java.io.InputStream;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistryInstanceHolder;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.document.JsonDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.process.ProcessManager;

/** {@link EnableAutoConfiguration Auto-configuration} for JodConverter local module. */
@Configuration
@ConditionalOnClass(LocalConverter.class)
@ConditionalOnProperty(prefix = "jodconverter.local", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(JodConverterLocalProperties.class)
public class JodConverterLocalAutoConfiguration {

  private final JodConverterLocalProperties properties;

  /**
   * Creates the local auto configuration.
   *
   * @param properties The local properties.
   */
  public JodConverterLocalAutoConfiguration(@NonNull final JodConverterLocalProperties properties) {
    this.properties = properties;
  }

  // Creates the OfficeManager bean.
  private OfficeManager createOfficeManager(final ProcessManager processManager) {

    final LocalOfficeManager.Builder builder = LocalOfficeManager.builder();

    if (!StringUtils.isBlank(properties.getPortNumbers())) {
      builder.portNumbers(
          Stream.of(properties.getPortNumbers().split("\\s*,\\s*"))
              .mapToInt(
                  str -> {
                    try {
                      return Integer.parseInt(str);
                    } catch (final Exception e) {
                      return 2002;
                    }
                  })
              .toArray());
    }

    builder.officeHome(properties.getOfficeHome());
    builder.workingDir(properties.getWorkingDir());
    builder.templateProfileDir(properties.getTemplateProfileDir());
    builder.killExistingProcess(properties.isKillExistingProcess());
    builder.processTimeout(properties.getProcessTimeout());
    builder.processRetryInterval(properties.getProcessRetryInterval());
    builder.taskExecutionTimeout(properties.getTaskExecutionTimeout());
    builder.maxTasksPerProcess(properties.getMaxTasksPerProcess());
    builder.taskQueueTimeout(properties.getTaskQueueTimeout());
    if (StringUtils.isBlank(properties.getProcessManagerClass())) {
      builder.processManager(processManager);
    } else {
      builder.processManager(properties.getProcessManagerClass());
    }

    // Starts the manager
    return builder.build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "processManager")
  /* default */ ProcessManager processManager() {
    return LocalOfficeUtils.findBestProcessManager();
  }

  @Bean
  @ConditionalOnMissingBean(name = "documentFormatRegistry")
  /* default */ DocumentFormatRegistry documentFormatRegistry(final ResourceLoader resourceLoader)
      throws Exception {

    DocumentFormatRegistry registry;
    if (StringUtils.isBlank(properties.getDocumentFormatRegistry())) {
      try (InputStream in =
          resourceLoader.getResource("classpath:document-formats.json").getInputStream()) {
        registry =
            properties.getFormatOptions() == null
                ? JsonDocumentFormatRegistry.create(in)
                : JsonDocumentFormatRegistry.create(in, properties.getFormatOptions());
      }
    } else {
      try (InputStream in =
          resourceLoader.getResource(properties.getDocumentFormatRegistry()).getInputStream()) {
        registry =
            properties.getFormatOptions() == null
                ? JsonDocumentFormatRegistry.create(in)
                : JsonDocumentFormatRegistry.create(in, properties.getFormatOptions());
      }
    }

    // Set as default.
    DefaultDocumentFormatRegistryInstanceHolder.setInstance(registry);

    // Return it.
    return registry;
  }

  @Bean(name = "localOfficeManager", initMethod = "start", destroyMethod = "stop")
  @ConditionalOnMissingBean(name = "localOfficeManager")
  /* default */ OfficeManager localOfficeManager(final ProcessManager processManager) {

    return createOfficeManager(processManager);
  }

  // Must appear after the localOfficeManager bean creation. Do not reorder this class by name.
  @Bean
  @ConditionalOnMissingBean(name = "localDocumentConverter")
  @ConditionalOnBean(name = {"localOfficeManager", "documentFormatRegistry"})
  /* default */ DocumentConverter localDocumentConverter(
      final OfficeManager localOfficeManager, final DocumentFormatRegistry documentFormatRegistry) {

    return LocalConverter.builder()
        .officeManager(localOfficeManager)
        .formatRegistry(documentFormatRegistry)
        .build();
  }
}
