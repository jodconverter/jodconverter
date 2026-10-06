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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

import org.jodconverter.core.document.DefaultDocumentFormatRegistryInstanceHolder;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.document.JsonDocumentFormatRegistry;
import org.jodconverter.core.util.StringUtils;

/**
 * Auto-configuration of the {@link DocumentFormatRegistry} shared by the converters of the starter,
 * from the {@code jodconverter.document-formats} properties.
 */
@AutoConfiguration
@ConditionalOnClass(JsonDocumentFormatRegistry.class)
@EnableConfigurationProperties(JodConverterDocumentFormatsProperties.class)
public class JodConverterDocumentFormatsAutoConfiguration {

  private static final String DEFAULT_FORMATS_PATH = "classpath:document-formats.json";
  private static final String CUSTOM_FORMATS_PATH = "classpath:custom-document-formats.json";

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JodConverterDocumentFormatsAutoConfiguration.class);

  private final JodConverterDocumentFormatsProperties properties;

  public JodConverterDocumentFormatsAutoConfiguration(
      final @NonNull JodConverterDocumentFormatsProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean(DocumentFormatRegistry.class)
  /* default */ DocumentFormatRegistry documentFormatRegistry(final ResourceLoader resourceLoader)
      throws Exception {

    // Load the json resource containing the document formats.
    final var registryResourceName =
        StringUtils.isBlank(properties.getRegistry())
            ? DEFAULT_FORMATS_PATH
            : properties.getRegistry();
    LOGGER.debug("Loading document formats registry from resource [{}]", registryResourceName);
    try (var in = resourceLoader.getResource(registryResourceName).getInputStream()) {

      // Create the registry.
      final var registry =
          properties.getOptions() == null
              ? JsonDocumentFormatRegistry.create(in)
              : JsonDocumentFormatRegistry.create(in, properties.getOptions());

      // Load the custom formats, if any.
      final var resource = resourceLoader.getResource(CUSTOM_FORMATS_PATH);
      if (resource.exists()) {
        LOGGER.debug(
            "Loading custom document formats registry from resource [{}]", CUSTOM_FORMATS_PATH);
        registry.addRegistry(JsonDocumentFormatRegistry.create(resource.getInputStream()));
      }

      // Set as default, for the code that uses DefaultDocumentFormatRegistry.getInstance().
      DefaultDocumentFormatRegistryInstanceHolder.setInstance(registry);

      return registry;
    }
  }
}
