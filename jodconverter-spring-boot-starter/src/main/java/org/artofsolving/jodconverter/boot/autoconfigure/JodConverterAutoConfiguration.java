/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.boot.autoconfigure;

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

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeManager;

@Configuration
@ConditionalOnClass({OfficeDocumentConverter.class})
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

    final Set<Integer> iports = new HashSet<Integer>();

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

    final DefaultOfficeManagerBuilder builder = new DefaultOfficeManagerBuilder();

    if (!StringUtils.isBlank(properties.getOfficeHome())) {
      builder.setOfficeHome(properties.getOfficeHome());
    }

    if (!StringUtils.isBlank(properties.getOfficeHome())) {
      builder.setWorkingDir(new File(properties.getWorkingDir()));
    }

    if (!StringUtils.isBlank(properties.getPortNumbers())) {
      final Set<Integer> ports = buildPortNumbers(properties.getPortNumbers());
      if (!ports.isEmpty()) {
        builder.setPortNumbers(ArrayUtils.toPrimitive(ports.toArray(new Integer[] {})));
      }
    }

    if (!StringUtils.isBlank(properties.getTemplateProfileDir())) {
      builder.setTemplateProfileDir(new File(properties.getTemplateProfileDir()));
    }

    builder.setRetryTimeout(properties.getRetryTimeout());
    builder.setRetryInterval(properties.getRetryInterval());
    builder.setKillExistingProcess(properties.isKillExistingProcess());
    builder.setTaskQueueTimeout(properties.getTaskQueueTimeout());
    builder.setTaskExecutionTimeout(properties.getTaskExecutionTimeout());
    builder.setMaxTasksPerProcess(properties.getMaxTasksPerProcess());
    builder.setRetryInterval(properties.getRetryInterval());

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
  public OfficeDocumentConverter jodConverter(final OfficeManager officeManager) {

    return new OfficeDocumentConverter(officeManager);
  }
}
