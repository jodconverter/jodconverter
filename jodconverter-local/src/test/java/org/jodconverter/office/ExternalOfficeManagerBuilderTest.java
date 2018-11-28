/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

public class ExternalOfficeManagerBuilderTest {

  @Test
  public void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws Exception {

    final OfficeManager manager = new ExternalOfficeManagerBuilder().build();

    assertThat(manager).isInstanceOf(ExternalOfficeManager.class);
    final ExternalOfficeManagerConfig config =
        (ExternalOfficeManagerConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.isConnectOnStart()).isTrue();
    assertThat(config.getConnectTimeout()).isEqualTo(120000L);
    assertThat(config.getRetryInterval()).isEqualTo(250L);

    final OfficeConnection connection =
        (OfficeConnection) FieldUtils.readField(manager, "connection", true);
    final OfficeUrl officeUrl = (OfficeUrl) FieldUtils.readField(connection, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString())
        .isEqualTo("socket,host=127.0.0.1,port=2002,tcpNoDelay=1");
  }

  @Test
  public void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues()
      throws Exception {

    final OfficeManager manager =
        new ExternalOfficeManagerBuilder()
            .setConnectionProtocol(OfficeConnectionProtocol.PIPE)
            .setPipeName("test")
            .setPortNumber(2003)
            .setConnectOnStart(false)
            .setConnectTimeout(5000)
            .build();

    assertThat(manager).isInstanceOf(ExternalOfficeManager.class);
    final ExternalOfficeManagerConfig config =
        (ExternalOfficeManagerConfig) FieldUtils.readField(manager, "config", true);
    assertThat(config.getWorkingDir().getPath())
        .isEqualTo(new File(System.getProperty("java.io.tmpdir")).getPath());
    assertThat(config.isConnectOnStart()).isFalse();
    assertThat(config.getConnectTimeout()).isEqualTo(5000L);

    final OfficeConnection connection =
        (OfficeConnection) FieldUtils.readField(manager, "connection", true);
    final OfficeUrl officeUrl = (OfficeUrl) FieldUtils.readField(connection, "officeUrl", true);
    assertThat(officeUrl.getConnectionAndParametersAsString()).isEqualTo("pipe,name=test");
  }
}
