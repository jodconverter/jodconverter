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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.office.OfficeProcessManagerPoolConfig;
import org.jodconverter.local.office.OfficeUrl;

/** Contains tests for the {@link JodConverterBean} class with default values. */
public class JodConverterBeanITest {

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws OfficeException {

    final JodConverterBean bean = new JodConverterBean();
    try {
      bean.afterPropertiesSet();

      final OfficeManager manager = Whitebox.getInternalState(bean, "officeManager");
      final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
      try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
        softly
            .assertThat(config.getOfficeHome().getPath())
            .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
        softly
            .assertThat(config.getWorkingDir().getPath())
            .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
        softly
            .assertThat(config.getProcessManager())
            .isEqualTo(LocalOfficeUtils.findBestProcessManager());
        softly.assertThat(config.getRunAsArgs()).isNull();
        softly.assertThat(config.getTemplateProfileDir()).isNull();
        softly.assertThat(config.isKillExistingProcess()).isTrue();
        softly.assertThat(config.getProcessTimeout()).isEqualTo(120_000L);
        softly.assertThat(config.getProcessRetryInterval()).isEqualTo(250L);
        softly.assertThat(config.getMaxTasksPerProcess()).isEqualTo(200);
        softly.assertThat(config.isDisableOpengl()).isFalse();
        softly.assertThat(config.getTaskExecutionTimeout()).isEqualTo(120_000L);
        softly.assertThat(config.getTaskQueueTimeout()).isEqualTo(30_000L);
      }

      final OfficeUrl[] officeUrls = Whitebox.getInternalState(manager, "officeUrls");
      assertThat(officeUrls).hasSize(1);
      assertThat(officeUrls[0].getConnectionAndParametersAsString())
          .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");

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

      final OfficeManager manager = Whitebox.getInternalState(bean, "officeManager");
      final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
      try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
        softly
            .assertThat(config.getOfficeHome().getPath())
            .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
        softly
            .assertThat(config.getWorkingDir().getPath())
            .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
        softly
            .assertThat(config.getTemplateProfileDir().getPath())
            .isEqualTo(new File("src/integTest/resources/templateProfileDir").getPath());
        softly
            .assertThat(config.getProcessManager())
            .isEqualTo(LocalOfficeUtils.findBestProcessManager());
        softly.assertThat(config.isKillExistingProcess()).isFalse();
        softly.assertThat(config.getProcessTimeout()).isEqualTo(40_000L);
        softly.assertThat(config.getProcessRetryInterval()).isEqualTo(1_000L);
        softly.assertThat(config.getMaxTasksPerProcess()).isEqualTo(10);
        softly.assertThat(config.isDisableOpengl()).isFalse();
        softly.assertThat(config.getTaskExecutionTimeout()).isEqualTo(20_000L);
        softly.assertThat(config.getTaskQueueTimeout()).isEqualTo(1_000L);
      }

      final OfficeUrl[] officeUrls = Whitebox.getInternalState(manager, "officeUrls");
      assertThat(officeUrls).hasSize(1);
      assertThat(officeUrls[0].getConnectionAndParametersAsString())
          .isEqualTo("socket,host=127.0.0.1,port=2003,tcpNoDelay=1");

    } finally {
      bean.destroy();
    }
  }
}
