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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import org.jodconverter.core.pdf.PdfOptions;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the PDF options that the auto-configured
 * converters apply to all their conversions to PDF. It is active when at least one {@code
 * jodconverter.pdf} property is set.
 *
 * <p>An application can define its own {@link PdfOptions} bean instead: the auto-configured
 * converters use it the same way.
 */
@AutoConfiguration(
    before = {
      JodConverterLocalAutoConfiguration.class,
      JodConverterExternalAutoConfiguration.class,
      JodConverterRemoteAutoConfiguration.class
    })
@EnableConfigurationProperties(JodConverterPdfProperties.class)
public class JodConverterPdfAutoConfiguration {

  private static final String PREFIX = "jodconverter.pdf";

  @Bean
  @ConditionalOnMissingBean(PdfOptions.class)
  @Conditional(PdfPropertiesCondition.class)
  /* default */ PdfOptions jodConverterPdfOptions(
      final @NonNull JodConverterPdfProperties properties) {
    return properties.toPdfOptions();
  }

  /** Matches when at least one {@code jodconverter.pdf} property is set. */
  /* default */ static class PdfPropertiesCondition implements Condition {

    @Override
    public boolean matches(
        final @NonNull ConditionContext context, final @NonNull AnnotatedTypeMetadata metadata) {
      return Binder.get(context.getEnvironment())
          .bind(PREFIX, Bindable.mapOf(String.class, Object.class))
          .isBound();
    }
  }
}
