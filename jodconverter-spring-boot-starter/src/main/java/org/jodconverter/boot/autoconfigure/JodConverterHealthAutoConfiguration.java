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
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import org.jodconverter.core.office.OfficeManager;

/**
 * {@link AutoConfiguration Auto-configuration} of a {@link HealthIndicator} for the office
 * managers, when Spring Boot Actuator is on the classpath. The indicator is named {@code
 * jodconverter}.
 */
@AutoConfiguration(
    after = {
      JodConverterLocalAutoConfiguration.class,
      JodConverterExternalAutoConfiguration.class,
      JodConverterRemoteAutoConfiguration.class
    })
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnBean(OfficeManager.class)
@ConditionalOnEnabledHealthIndicator("jodconverter")
public class JodConverterHealthAutoConfiguration {

  /**
   * Creates the health indicator of the office managers.
   *
   * @param officeManagers The office manager beans, by name.
   * @return The health indicator.
   */
  @Bean
  @ConditionalOnMissingBean(name = "jodconverterHealthIndicator")
  /* default */ HealthIndicator jodconverterHealthIndicator(
      final @NonNull Map<String, OfficeManager> officeManagers) {
    return new OfficeManagerHealthIndicator(officeManagers);
  }
}
