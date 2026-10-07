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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.AttachedOfficeManager;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for JodConverter attaching to office processes
 * that are started and managed outside of the application, with the {@code jodconverter.attached}
 * properties. The former {@code jodconverter.external} properties still enable it, deprecated, and
 * the beans keep their former names as aliases.
 */
@AutoConfiguration(
    after = {
      JodConverterDocumentFormatsAutoConfiguration.class,
      JodConverterLocalAutoConfiguration.class
    })
@ConditionalOnClass(AttachedOfficeManager.class)
@Conditional(JodConverterAttachedAutoConfiguration.Enabled.class)
@EnableConfigurationProperties({
  JodConverterAttachedProperties.class,
  JodConverterExternalProperties.class
})
@SuppressWarnings("removal")
public class JodConverterAttachedAutoConfiguration {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JodConverterAttachedAutoConfiguration.class);

  private final JodConverterAttachedProperties properties;

  /**
   * Creates the attached autoconfiguration, from the attached properties or, when they are not
   * enabled, from the former external properties.
   *
   * @param attached The attached properties.
   * @param external The former external properties.
   */
  public JodConverterAttachedAutoConfiguration(
      final @NonNull JodConverterAttachedProperties attached,
      final @NonNull JodConverterExternalProperties external) {
    if (attached.enabled()) {
      this.properties = attached;
    } else {
      LOGGER.warn("The jodconverter.external properties are deprecated: use jodconverter.attached");
      this.properties = external.toAttached();
    }
  }

  @Bean(
      name = {"attachedOfficeManager", "externalOfficeManager"},
      initMethod = "start",
      destroyMethod = "stop")
  @ConditionalOnMissingBean(name = {"attachedOfficeManager", "externalOfficeManager"})
  /* default */ OfficeManager attachedOfficeManager() {
    final var builder =
        AttachedOfficeManager.builder()
            .hostName(properties.hostName())
            .portNumbers(properties.portNumbers())
            .pipeNames(properties.pipeNames())
            .websocketUrls(properties.websocketUrls())
            .connectOnStart(properties.connectOnStart())
            .connectTimeout(properties.connectTimeout().toMillis())
            .connectRetryInterval(properties.connectRetryInterval().toMillis())
            .connectFailFast(properties.connectFailFast())
            .maxTasksPerConnection(properties.maxTasksPerConnection());
    properties.applyTo(builder);
    return builder.build();
  }

  @Bean(name = {"attachedDocumentConverter", "externalDocumentConverter"})
  @ConditionalOnMissingBean(name = {"attachedDocumentConverter", "externalDocumentConverter"})
  // The qualifier is required when the local or remote office manager also exists: since Spring
  // 6.1, a parameter name is no longer used to choose between beans of the same type.
  /* default */ DocumentConverter attachedDocumentConverter(
      final @Qualifier("attachedOfficeManager") OfficeManager attachedOfficeManager,
      final DocumentFormatRegistry documentFormatRegistry,
      final ObjectProvider<PdfOptions> pdfOptions) {
    final var builder =
        LocalConverter.builder()
            .officeManager(attachedOfficeManager)
            .formatRegistry(documentFormatRegistry)
            .loadDocumentMode(properties.loadDocumentMode())
            .loadProperties(
                properties.applyDefaultLoadProperties()
                    ? LocalConverter.DEFAULT_LOAD_PROPERTIES
                    : Map.of());
    // Apply the PDF options, from the jodconverter.pdf properties or from the application, to
    // all the conversions to PDF.
    pdfOptions.ifUnique(builder::defaultTargetOptions);
    return builder.build();
  }

  /** Enabled by the attached properties, or by the former external ones. */
  /* default */ static class Enabled extends AnyNestedCondition {

    /* default */ Enabled() {
      super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    /** The attached properties enable the auto-configuration. */
    @ConditionalOnProperty(prefix = "jodconverter.attached", name = "enabled", havingValue = "true")
    /* default */ static class Attached {}

    /** The former external properties enable it too. */
    @ConditionalOnProperty(prefix = "jodconverter.external", name = "enabled", havingValue = "true")
    /* default */ static class External {}
  }
}
