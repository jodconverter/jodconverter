/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeManager;

@Configuration
@ConditionalOnClass({DocumentConverter.class})
@ConditionalOnProperty(
  prefix = "jodconverter",
  name = "enabled",
  havingValue = "true",
  matchIfMissing = false
)
@EnableConfigurationProperties(JodConverterProperties.class)
public class JodConverterAutoConfiguration {

  private final JodConverterProperties properties;

  public JodConverterAutoConfiguration(final JodConverterProperties properties) {
    this.properties = properties;
  }

  // Creates the OfficeManager bean.
  private OfficeManager createOfficeManager() {

    final LocalOfficeManager.Builder builder = LocalOfficeManager.builder();

    if (!StringUtils.isBlank(properties.getPortNumbers())) {
      builder.portNumbers(
          ArrayUtils.toPrimitive(
              Stream.of(StringUtils.split(properties.getPortNumbers(), ", "))
                  .map(str -> NumberUtils.toInt(str, 2002))
                  .toArray(Integer[]::new)));
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

    // Starts the manager
    return builder.build();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnMissingBean
  public OfficeManager officeManager() {

    return createOfficeManager();
  }

  // Must appear after the OfficeManager bean creation. Do not reorder this class by name.
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(OfficeManager.class)
  public DocumentConverter jodConverter(final OfficeManager officeManager) {

    return LocalConverter.make(officeManager);
  }
}
