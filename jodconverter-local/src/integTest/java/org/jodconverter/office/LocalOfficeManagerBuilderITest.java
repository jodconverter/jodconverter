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

package org.jodconverter.office;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Test;

public class LocalOfficeManagerBuilderITest {

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager = new DefaultOfficeManagerBuilder().build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
    assertThat(config.getRunAsArgs()).isNull();
    assertThat(config.getTemplateProfileDir()).isNull();
    assertThat(config.isKillExistingProcess()).isTrue();
    assertThat(config.getProcessTimeout()).isEqualTo(120000L);
    assertThat(config.getProcessRetryInterval()).isEqualTo(250L);
    assertThat(config.getMaxTasksPerProcess()).isEqualTo(200);
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(120000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(30000L);

    final OfficeUrl[] officeUrls = (OfficeUrl[]) FieldUtils.readField(manager, "officeUrls", true);
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0].getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.PIPE)
            .setPipeNames("test")
            .setPortNumbers(2003)
            .setOfficeHome(LocalOfficeUtils.getDefaultOfficeHome())
            .setWorkingDir(System.getProperty("java.io.tmpdir"))
            .setTemplateProfileDir("src/integTest/resources/templateProfileDir")
            .setProcessManager(LocalOfficeUtils.findBestProcessManager())
            .setRunAsArgs("sudo")
            .setKillExistingProcess(false)
            .setRetryTimeout(5000)
            .setRetryInterval(1000)
            .setMaxTasksPerProcess(10)
            .setTaskExecutionTimeout(20000)
            .setTaskQueueTimeout(1000)
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getTemplateProfileDir().getPath())
        .isEqualTo(new File("src/integTest/resources/templateProfileDir").getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
    assertThat(config.getRunAsArgs()).isEqualTo(new String[] {"sudo"});
    assertThat(config.isKillExistingProcess()).isEqualTo(false);
    assertThat(config.getProcessTimeout()).isEqualTo(5000L);
    assertThat(config.getProcessRetryInterval()).isEqualTo(1000L);
    assertThat(config.getMaxTasksPerProcess()).isEqualTo(10);
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(20000L);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(1000L);

    final OfficeUrl[] officeUrls = (OfficeUrl[]) FieldUtils.readField(manager, "officeUrls", true);
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0].getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setOfficeHome(LocalOfficeUtils.getDefaultOfficeHome().getPath())
            .setWorkingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .setProcessManager(LocalOfficeUtils.findBestProcessManager().getClass().getName())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager().getClass().getName())
        .isEqualTo(LocalOfficeUtils.findBestProcessManager().getClass().getName());
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setOfficeHome("   ")
            .setWorkingDir("   ")
            .setProcessManager("   ")
            .setTemplateProfileDir("   ")
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config =
        (OfficeProcessManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
  }

  @Test
  public void build_WithPipeDefaultConfiguration_ShouldInitializedOfficeManagerWithDefaultPipe()
      throws Exception {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.PIPE)
            .build();

    final OfficeUrl[] officeUrls = (OfficeUrl[]) FieldUtils.readField(manager, "officeUrls", true);
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0]).isInstanceOf(OfficeUrl.class);
    assertThat(officeUrls[0].getConnectionAndParametersAsString()).isEqualTo("pipe,name=office");
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithProcessManagerClassNotFound_ThrowIllegalArgumentException() {

    new DefaultOfficeManagerBuilder()
        .setProcessManager("org.jodconverter.notfound.ClassName")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPipeNames_ThrowIllegalArgumentException() {

    new DefaultOfficeManagerBuilder().setPipeNames((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPipeNames_ThrowIllegalArgumentException() {

    new DefaultOfficeManagerBuilder().setPipeNames().build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPortNumbers_ThrowIllegalArgumentException() {

    new DefaultOfficeManagerBuilder().setPortNumbers((int[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPortNumbers_ThrowIllegalArgumentException() {

    new DefaultOfficeManagerBuilder().setPortNumbers().build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullRunAsArgs_ThrowIllegalArgumentException() {

    new DefaultOfficeManagerBuilder().setRunAsArgs((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyRunAsArgs_ThrowIllegalArgumentException() {

    new DefaultOfficeManagerBuilder().setRunAsArgs().build();
  }
}
