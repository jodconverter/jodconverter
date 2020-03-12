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

package org.jodconverter.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;

/** Contains tests for the {@link JodConverterBean} class with default values. */
public class JodConverterBeanITest {

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws OfficeException {

    final JodConverterBean bean = new JodConverterBean();
    try {
      bean.afterPropertiesSet();

      assertThat(bean)
          .extracting("officeManager")
          .isInstanceOf(LocalOfficeManager.class)
          .extracting("workingDir", "taskQueueTimeout")
          .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

      assertThat(bean)
          .extracting("officeManager.entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .extracting(
                          "taskExecutionTimeout",
                          "maxTasksPerProcess",
                          "officeProcessManager.processTimeout",
                          "officeProcessManager.processRetryInterval",
                          "officeProcessManager.disableOpengl",
                          "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                          "officeProcessManager.process.officeHome",
                          "officeProcessManager.process.processManager",
                          "officeProcessManager.process.runAsArgs",
                          "officeProcessManager.process.templateProfileDir",
                          "officeProcessManager.process.killExistingProcess",
                          "officeProcessManager.process.instanceProfileDir",
                          "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                      .containsExactly(
                          120_000L,
                          200,
                          120_000L,
                          250L,
                          false,
                          "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                          LocalOfficeUtils.getDefaultOfficeHome(),
                          LocalOfficeUtils.findBestProcessManager(),
                          Collections.EMPTY_LIST,
                          null,
                          true,
                          new File(
                              OfficeUtils.getDefaultWorkingDir(),
                              ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                          "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));

    } finally {
      bean.destroy();
    }
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues()
      throws OfficeException {

    final JodConverterBean bean = new JodConverterBean();
    bean.setOfficeHome(LocalOfficeUtils.getDefaultOfficeHome().getAbsolutePath());
    bean.setPortNumbers("2003");
    bean.setWorkingDir(System.getProperty("java.io.tmpdir"));
    bean.setTemplateProfileDir("src/integTest/resources/templateProfileDir");
    bean.setKillExistingProcess(false);
    bean.setProcessTimeout(40_000L);
    bean.setProcessRetryInterval(1_000L);
    bean.setTaskExecutionTimeout(20_000L);
    bean.setMaxTasksPerProcess(10);
    bean.setTaskQueueTimeout(1_000L);
    try {
      bean.afterPropertiesSet();

      assertThat(bean)
          .extracting("officeManager")
          .isInstanceOf(LocalOfficeManager.class)
          .extracting("workingDir", "taskQueueTimeout")
          .containsExactly(OfficeUtils.getDefaultWorkingDir(), 1_000L);

      assertThat(bean)
          .extracting("officeManager.entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .extracting(
                          "taskExecutionTimeout",
                          "maxTasksPerProcess",
                          "officeProcessManager.processTimeout",
                          "officeProcessManager.processRetryInterval",
                          "officeProcessManager.disableOpengl",
                          "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                          "officeProcessManager.process.officeHome",
                          "officeProcessManager.process.processManager",
                          "officeProcessManager.process.runAsArgs",
                          "officeProcessManager.process.templateProfileDir",
                          "officeProcessManager.process.killExistingProcess",
                          "officeProcessManager.process.instanceProfileDir",
                          "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                      .containsExactly(
                          20_000L,
                          10,
                          40_000L,
                          1_000L,
                          false,
                          "socket,host=127.0.0.1,port=2003,tcpNoDelay=1",
                          LocalOfficeUtils.getDefaultOfficeHome(),
                          LocalOfficeUtils.findBestProcessManager(),
                          Collections.EMPTY_LIST,
                          new File("src/integTest/resources/templateProfileDir"),
                          false,
                          new File(
                              OfficeUtils.getDefaultWorkingDir(),
                              ".jodconverter_socket_host-127.0.0.1_port-2003_tcpNoDelay-1"),
                          "socket,host=127.0.0.1,port=2003,tcpNoDelay=1"));

    } finally {
      bean.destroy();
    }
  }

  @Test
  public void build_WithBadPortNumber_ShouldInitializedOfficeManagerWithDefaultValues()
      throws OfficeException {

    final JodConverterBean bean = new JodConverterBean();
    bean.setPortNumbers("potato");
    try {
      bean.afterPropertiesSet();

      assertThat(bean)
          .extracting("officeManager")
          .isInstanceOf(LocalOfficeManager.class)
          .extracting("workingDir", "taskQueueTimeout")
          .containsExactly(OfficeUtils.getDefaultWorkingDir(), 30_000L);

      assertThat(bean)
          .extracting("officeManager.entries")
          .asList()
          .hasSize(1)
          .element(0)
          .satisfies(
              o ->
                  assertThat(o)
                      .extracting(
                          "taskExecutionTimeout",
                          "maxTasksPerProcess",
                          "officeProcessManager.processTimeout",
                          "officeProcessManager.processRetryInterval",
                          "officeProcessManager.disableOpengl",
                          "officeProcessManager.process.officeUrl.connectionAndParametersAsString",
                          "officeProcessManager.process.officeHome",
                          "officeProcessManager.process.processManager",
                          "officeProcessManager.process.runAsArgs",
                          "officeProcessManager.process.templateProfileDir",
                          "officeProcessManager.process.killExistingProcess",
                          "officeProcessManager.process.instanceProfileDir",
                          "officeProcessManager.connection.officeUrl.connectionAndParametersAsString")
                      .containsExactly(
                          120_000L,
                          200,
                          120_000L,
                          250L,
                          false,
                          "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                          LocalOfficeUtils.getDefaultOfficeHome(),
                          LocalOfficeUtils.findBestProcessManager(),
                          Collections.EMPTY_LIST,
                          null,
                          true,
                          new File(
                              OfficeUtils.getDefaultWorkingDir(),
                              ".jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1"),
                          "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));

    } finally {
      bean.destroy();
    }
  }
}
