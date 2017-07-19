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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.jodconverter.DefaultConverter;
import org.jodconverter.DocumentConverter;
import org.jodconverter.office.DefaultOfficeManager;
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

  // Create a set of port numbers from a string
  private Set<Integer> buildPortNumbers(final String str) {

    final Set<Integer> iports = new HashSet<>();

    if (StringUtils.isBlank(str)) {
      return iports;
    }

    final String[] portNumbers = StringUtils.split(str, ", ");
    if (portNumbers.length == 0) {
      return iports;
    }

    for (final String portNumber : portNumbers) {
      if (!StringUtils.isBlank(portNumber)) {
        iports.add(Integer.parseInt(StringUtils.trim(portNumber)));
      }
    }

    return iports;
  }

  // Creates the OfficeManager bean.
  private OfficeManager createOfficeManager() {

    final DefaultOfficeManager.Builder builder = DefaultOfficeManager.builder();

    if (!StringUtils.isBlank(properties.getOfficeHome())) {
      builder.officeHome(properties.getOfficeHome());
    }

    if (!StringUtils.isBlank(properties.getWorkingDir())) {
      builder.workingDir(properties.getWorkingDir());
    }

    if (!StringUtils.isBlank(properties.getPortNumbers())) {
      final Set<Integer> ports = buildPortNumbers(properties.getPortNumbers());
      if (!ports.isEmpty()) {
        builder.portNumbers(ArrayUtils.toPrimitive(ports.toArray(new Integer[] {})));
      }
    }

    if (!StringUtils.isBlank(properties.getTemplateProfileDir())) {
      builder.templateProfileDir(new File(properties.getTemplateProfileDir()));
    }

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

    return DefaultConverter.make(officeManager);
  }
}
