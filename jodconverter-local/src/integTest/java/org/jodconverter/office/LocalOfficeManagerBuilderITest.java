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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

public class LocalOfficeManagerBuilderITest {

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = new DefaultOfficeManagerBuilder().build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
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

    final OfficeUrl[] officeUrls = Whitebox.getInternalState(manager, "officeUrls");
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0].getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues() {

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
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
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

    final OfficeUrl[] officeUrls = Whitebox.getInternalState(manager, "officeUrls");
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0].getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");
  }

  @Test
  public void build_WithValuesAsString_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setOfficeHome(LocalOfficeUtils.getDefaultOfficeHome().getPath())
            .setWorkingDir(new File(System.getProperty("java.io.tmpdir")).getPath())
            .setProcessManager(LocalOfficeUtils.findBestProcessManager().getClass().getName())
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager().getClass().getName())
        .isEqualTo(LocalOfficeUtils.findBestProcessManager().getClass().getName());
  }

  @Test
  public void build_WithEmptyValuesAsString_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setOfficeHome("   ")
            .setWorkingDir("   ")
            .setProcessManager("   ")
            .setTemplateProfileDir("   ")
            .build();

    assertThat(manager).isInstanceOf(AbstractOfficeManagerPool.class);
    final OfficeProcessManagerPoolConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getOfficeHome().getPath())
        .isEqualTo(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.getProcessManager()).isEqualTo(LocalOfficeUtils.findBestProcessManager());
  }

  @Test
  public void build_WithPipeDefaultConfiguration_ShouldInitializedOfficeManagerWithDefaultPipe() {

    final OfficeManager manager =
        new DefaultOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.PIPE)
            .build();

    final OfficeUrl[] officeUrls = Whitebox.getInternalState(manager, "officeUrls");
    assertThat(officeUrls).hasSize(1);
    assertThat(officeUrls[0]).isInstanceOf(OfficeUrl.class);
    assertThat(officeUrls[0].getConnectionAndParametersAsString()).isEqualTo("pipe,name=office");
  }

  @Test
  public void build_WithProcessManagerClassNotFound_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new DefaultOfficeManagerBuilder()
                    .setProcessManager("org.jodconverter.notfound.ClassName")
                    .build());
  }

  @Test
  public void build_WithNullPipeNames_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DefaultOfficeManagerBuilder().setPipeNames((String[]) null).build());
  }

  @Test
  public void build_WithEmptyPipeNames_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DefaultOfficeManagerBuilder().setPipeNames().build());
  }

  @Test
  public void build_WithNullPortNumbers_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DefaultOfficeManagerBuilder().setPortNumbers((int[]) null).build());
  }

  @Test
  public void build_WithEmptyPortNumbers_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DefaultOfficeManagerBuilder().setPortNumbers().build());
  }

  @Test
  public void build_WithNullRunAsArgs_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DefaultOfficeManagerBuilder().setRunAsArgs((String[]) null).build());
  }

  @Test
  public void build_WithEmptyRunAsArgs_ThrowIllegalArgumentException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DefaultOfficeManagerBuilder().setRunAsArgs().build());
  }
}
