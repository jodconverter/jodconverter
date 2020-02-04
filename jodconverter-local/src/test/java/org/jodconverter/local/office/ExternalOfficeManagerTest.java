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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeManager;

/** Contains tests for the {@link ExternalOfficeManager} class. */
public class ExternalOfficeManagerTest {

  @Test
  public void install_ShouldSetInstalledOfficeManagerHolder() {

    // Ensure we do not replace the current installed manager
    final OfficeManager installedManager = InstalledOfficeManagerHolder.getInstance();
    try {
      final OfficeManager manager = ExternalOfficeManager.install();
      assertThat(InstalledOfficeManagerHolder.getInstance()).isEqualTo(manager);
    } finally {
      InstalledOfficeManagerHolder.setInstance(installedManager);
    }
  }

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues() {

    final OfficeManager manager = ExternalOfficeManager.make();

    assertThat(manager).isInstanceOf(ExternalOfficeManager.class);
    final ExternalOfficeManagerConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.isConnectOnStart()).isTrue();
    assertThat(config.getConnectTimeout()).isEqualTo(120_000L);
    assertThat(config.getRetryInterval()).isEqualTo(250L);

    final OfficeConnection connection = Whitebox.getInternalState(manager, "connection");
    final OfficeUrl officeUrl = Whitebox.getInternalState(connection, "officeUrl");
    assertThat(officeUrl.getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues() {

    final OfficeManager manager =
        ExternalOfficeManager.builder()
            .connectionProtocol(OfficeConnectionProtocol.PIPE)
            .pipeName("test")
            .portNumber(2003)
            .workingDir(System.getProperty("java.io.tmpdir"))
            .connectOnStart(false)
            .connectTimeout(5_000L)
            .retryInterval(1_000L)
            .build();

    assertThat(manager).isInstanceOf(ExternalOfficeManager.class);
    final ExternalOfficeManagerConfig config = Whitebox.getInternalState(manager, "config");
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.isConnectOnStart()).isFalse();
    assertThat(config.getConnectTimeout()).isEqualTo(5_000L);
    assertThat(config.getRetryInterval()).isEqualTo(1_000L);

    final OfficeConnection connection = Whitebox.getInternalState(manager, "connection");
    final OfficeUrl officeUrl = Whitebox.getInternalState(connection, "officeUrl");
    assertThat(officeUrl.getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");
  }

  @Test
  public void config_WithCustomValues_ShouldInitializedConfigWithCustomValues() {

    final ExternalOfficeManagerConfig config =
        new ExternalOfficeManagerConfig(null, true, 5_000L, 1_000L);
    config.setWorkingDir(new File(System.getProperty("java.io.tmpdir")));
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.isConnectOnStart()).isTrue();
    assertThat(config.getConnectTimeout()).isEqualTo(5_000L);
    assertThat(config.getRetryInterval()).isEqualTo(1_000L);
  }
}
