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

public class DefaultOfficeManagerITest {

  @Test
  public void install_ShouldSetInstalledOfficeManagerHolder() throws Exception {

    final OfficeManager manager = DefaultOfficeManager.install();

    assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
  }

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager = DefaultOfficeManager.make();

    assertThat(manager).isInstanceOf(DefaultOfficeManager.class);
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

    final OfficeProcessManager officeProcessManager =
        (OfficeProcessManager) FieldUtils.readField(poolEntries[0], "officeProcessManager", true);
    final OfficeProcess officeProcess =
        (OfficeProcess) FieldUtils.readField(officeProcessManager, "process", true);
    final OfficeUrl officeUrl = (OfficeUrl) FieldUtils.readField(officeProcess, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        DefaultOfficeManager.builder()
            .pipeNames("test")
            .portNumbers(2003)
            .officeHome(OfficeUtils.getDefaultOfficeHome())
            .workingDir(System.getProperty("java.io.tmpdir"))
            .templateProfileDir("src/integTest/resources/templateProfileDir")
            .processManager(OfficeUtils.findBestProcessManager())
            .runAsArgs("sudo")
            .killExistingProcess(false)
            .processTimeout(5000)
            .processRetryInterval(1000)
            .taskExecutionTimeout(20000)
            .maxTasksPerProcess(10)
            .taskQueueTimeout(1000)
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

    OfficeProcessManager officeProcessManager =
        (OfficeProcessManager) FieldUtils.readField(poolEntries[0], "officeProcessManager", true);
    OfficeProcess officeProcess =
        (OfficeProcess) FieldUtils.readField(officeProcessManager, "process", true);
    OfficeUrl officeUrl = (OfficeUrl) FieldUtils.readField(officeProcess, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");

    officeProcessManager =
        (OfficeProcessManager) FieldUtils.readField(poolEntries[1], "officeProcessManager", true);
    officeProcess = (OfficeProcess) FieldUtils.readField(officeProcessManager, "process", true);
    officeUrl = (OfficeUrl) FieldUtils.readField(officeProcess, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2003,tcpNoDelay=1");
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        DefaultOfficeManager.builder()
            .officeHome(OfficeUtils.getDefaultOfficeHome().getPath())
            .workingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .processManager(OfficeUtils.findBestProcessManager().getClass().getName())
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
        DefaultOfficeManager.builder()
            .officeHome("   ")
            .workingDir("   ")
            .processManager("   ")
            .templateProfileDir("   ")
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

  @Test(expected = IllegalArgumentException.class)
  public void build_WithProcessManagerClassNotFound_ThrowIllegalArgumentException()
      throws Exception {

    DefaultOfficeManager.builder().processManager("org.jodconverter.notfound.ClassName").build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPipeNames_ThrowIllegalArgumentException() throws Exception {

    DefaultOfficeManager.builder().pipeNames((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPipeNames_ThrowIllegalArgumentException() throws Exception {

    DefaultOfficeManager.builder().pipeNames(new String[0]).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullPortNumbers_ThrowIllegalArgumentException() throws Exception {

    DefaultOfficeManager.builder().portNumbers((int[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyPortNumbers_ThrowIllegalArgumentException() throws Exception {

    DefaultOfficeManager.builder().portNumbers(new int[0]).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithNullRunAsArgs_ThrowIllegalArgumentException() throws Exception {

    DefaultOfficeManager.builder().runAsArgs((String[]) null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void build_WithEmptyRunAsArgs_ThrowIllegalArgumentException() throws Exception {

    DefaultOfficeManager.builder().runAsArgs(new String[0]).build();
  }
}
