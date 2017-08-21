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

package org.jodconverter.office;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Test;

public class DefaultOfficeManagerBuilderITest {

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager = new DefaultOfficeManagerBuilder().build();

    assertThat(manager).isInstanceOf(OfficeManagerPool.class);
    final OfficeManagerPoolConfig config =
        (OfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(OfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(OfficeUtils.findBestProcessManager());
    assertThat(config.getRunAsArgs()).isNull();
    assertThat(config.getTemplateProfileDir()).isNull();
    assertThat(config.isKillExistingProcess()).isTrue();
    assertThat(config.getProcessTimeout()).isEqualTo(120000L);
    assertThat(config.getProcessRetryInterval()).isEqualTo(250L);
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(120000L);
    assertThat(config.getMaxTasksPerProcess()).isEqualTo(200);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(30000L);

    final OfficeManager[] poolEntries =
        (OfficeManager[]) FieldUtils.readField(manager, "entries", true);
    assertThat(poolEntries).hasSize(1);
    assertThat(poolEntries[0]).isInstanceOf(OfficeManagerPoolEntry.class);

    final OfficeProcessManager processManager =
        (OfficeProcessManager) FieldUtils.readField(poolEntries[0], "officeProcessManager", true);
    final OfficeProcess officeProcess =
        (OfficeProcess) FieldUtils.readField(processManager, "process", true);
    final OfficeUrl officeUrl = (OfficeUrl) FieldUtils.readField(officeProcess, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString())
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
            .setOfficeHome(OfficeUtils.getDefaultOfficeHome())
            .setWorkingDir(System.getProperty("java.io.tmpdir"))
            .setTemplateProfileDir("src/integTest/resources/templateProfileDir")
            .setProcessManager(OfficeUtils.findBestProcessManager())
            .setRunAsArgs("sudo")
            .setKillExistingProcess(false)
            .setRetryTimeout(5000)
            .setRetryInterval(1000)
            .setTaskExecutionTimeout(20000)
            .setMaxTasksPerProcess(10)
            .setTaskQueueTimeout(1000)
            .build();

    assertThat(manager).isInstanceOf(OfficeManagerPool.class);
    final OfficeManagerPoolConfig config =
        (OfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(OfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getTemplateProfileDir().getPath())
        .isEqualTo(new File("src/integTest/resources/templateProfileDir").getPath());
    assertThat(config.getProcessManager()).isEqualTo(OfficeUtils.findBestProcessManager());
    assertThat(config.getRunAsArgs()).isEqualTo(new String[] {"sudo"});
    assertThat(config.isKillExistingProcess()).isEqualTo(false);
    assertThat(config.getProcessTimeout()).isEqualTo(5000L);
    assertThat(config.getProcessRetryInterval()).isEqualTo(1000L);
    assertThat(config.getTaskExecutionTimeout()).isEqualTo(20000L);
    assertThat(config.getMaxTasksPerProcess()).isEqualTo(10);
    assertThat(config.getTaskQueueTimeout()).isEqualTo(1000L);

    final OfficeManager[] poolEntries =
        (OfficeManager[]) FieldUtils.readField(manager, "entries", true);
    assertThat(poolEntries).hasSize(2);
    assertThat(poolEntries[0]).isInstanceOf(OfficeManagerPoolEntry.class);

    OfficeProcessManager processManager =
        (OfficeProcessManager) FieldUtils.readField(poolEntries[0], "officeProcessManager", true);
    OfficeProcess officeProcess =
        (OfficeProcess) FieldUtils.readField(processManager, "process", true);
    OfficeUrl officeUrl = (OfficeUrl) FieldUtils.readField(officeProcess, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");

    processManager =
        (OfficeProcessManager) FieldUtils.readField(poolEntries[1], "officeProcessManager", true);
    officeProcess = (OfficeProcess) FieldUtils.readField(processManager, "process", true);
    officeUrl = (OfficeUrl) FieldUtils.readField(officeProcess, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2003,tcpNoDelay=1");
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setOfficeHome(OfficeUtils.getDefaultOfficeHome().getPath())
            .setWorkingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .setProcessManager(OfficeUtils.findBestProcessManager().getClass().getName())
            .build();

    assertThat(manager).isInstanceOf(OfficeManagerPool.class);
    final OfficeManagerPoolConfig config =
        (OfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(OfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager().getClass().getName())
        .isEqualTo(OfficeUtils.findBestProcessManager().getClass().getName());
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

    assertThat(manager).isInstanceOf(OfficeManagerPool.class);
    final OfficeManagerPoolConfig config =
        (OfficeManagerPoolConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(OfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(OfficeUtils.findBestProcessManager());
  }

  @Test
  public void build_WithPipeDefaultConfiguration_ShouldInitializedOfficeManagerWithDefaultPipe()
      throws Exception {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.PIPE)
            .build();

    final OfficeManager[] poolEntries =
        (OfficeManager[]) FieldUtils.readField(manager, "entries", true);
    assertThat(poolEntries).hasSize(1);
    assertThat(poolEntries[0]).isInstanceOf(OfficeManagerPoolEntry.class);

    final OfficeProcessManager processManager =
        (OfficeProcessManager) FieldUtils.readField(poolEntries[0], "officeProcessManager", true);
    final OfficeProcess officeProcess =
        (OfficeProcess) FieldUtils.readField(processManager, "process", true);
    final OfficeUrl officeUrl = (OfficeUrl) FieldUtils.readField(officeProcess, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString()).isEqualTo("pipe,name=office");
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithProcessManagerClassNotFound_ThrowIllegalArgumentException()
      throws Exception {

    new DefaultOfficeManagerBuilder()
        .setProcessManager("org.jodconverter.notfound.ClassName")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPipeNames_ThrowIllegalArgumentException() throws Exception {

    new DefaultOfficeManagerBuilder().setPipeNames((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPipeNames_ThrowIllegalArgumentException() throws Exception {

    new DefaultOfficeManagerBuilder().setPipeNames(new String[0]).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPortNumbers_ThrowIllegalArgumentException() throws Exception {

    new DefaultOfficeManagerBuilder().setPortNumbers((int[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPortNumbers_ThrowIllegalArgumentException() throws Exception {

    new DefaultOfficeManagerBuilder().setPortNumbers(new int[0]).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullRunAsArgs_ThrowIllegalArgumentException() throws Exception {

    new DefaultOfficeManagerBuilder().setRunAsArgs((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyRunAsArgs_ThrowIllegalArgumentException() throws Exception {

    new DefaultOfficeManagerBuilder().setRunAsArgs(new String[0]).build();
  }
}
