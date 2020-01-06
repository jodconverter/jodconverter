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

import org.junit.Test;

import com.sun.star.lib.uno.helper.UnoUrl;

public class OfficeUrlTest {

  @Test
  public void ctro_WithPipeName_SameAsOriginalUnoUrl() {

    final OfficeUrl pipeUrl = new OfficeUrl("testPipeName");
    final UnoUrl unoPipeUrl = OfficeUrl.pipe("testPipeName");

    assertThat(pipeUrl.getConnection()).isEqualTo(unoPipeUrl.getConnection());
    assertThat(pipeUrl.getConnectionAndParametersAsString())
        .isEqualTo(unoPipeUrl.getConnectionAndParametersAsString());
    assertThat(pipeUrl.getConnectionParametersAsString())
        .isEqualTo(unoPipeUrl.getConnectionParametersAsString());
    assertThat(pipeUrl.getConnectionParameters()).isEqualTo(unoPipeUrl.getConnectionParameters());
    assertThat(pipeUrl.getProtocol()).isEqualTo(unoPipeUrl.getProtocol());
    assertThat(pipeUrl.getProtocolAndParametersAsString())
        .isEqualTo(unoPipeUrl.getProtocolAndParametersAsString());
    assertThat(pipeUrl.getProtocolParametersAsString())
        .isEqualTo(unoPipeUrl.getProtocolParametersAsString());
    assertThat(pipeUrl.getProtocolParameters()).isEqualTo(unoPipeUrl.getProtocolParameters());
    assertThat(pipeUrl.getRootOid()).isEqualTo(unoPipeUrl.getRootOid());
  }

  @Test
  public void ctro_WithPortNumber_SameAsOriginalUnoUrl() {

    final OfficeUrl pipeUrl = new OfficeUrl(2005);
    final UnoUrl unoPipeUrl = OfficeUrl.socket(2005);

    assertThat(pipeUrl.getConnection()).isEqualTo(unoPipeUrl.getConnection());
    assertThat(pipeUrl.getConnectionAndParametersAsString())
        .isEqualTo(unoPipeUrl.getConnectionAndParametersAsString());
    assertThat(pipeUrl.getConnectionParametersAsString())
        .isEqualTo(unoPipeUrl.getConnectionParametersAsString());
    assertThat(pipeUrl.getConnectionParameters()).isEqualTo(unoPipeUrl.getConnectionParameters());
    assertThat(pipeUrl.getProtocol()).isEqualTo(unoPipeUrl.getProtocol());
    assertThat(pipeUrl.getProtocolAndParametersAsString())
        .isEqualTo(unoPipeUrl.getProtocolAndParametersAsString());
    assertThat(pipeUrl.getProtocolParametersAsString())
        .isEqualTo(unoPipeUrl.getProtocolParametersAsString());
    assertThat(pipeUrl.getProtocolParameters()).isEqualTo(unoPipeUrl.getProtocolParameters());
    assertThat(pipeUrl.getRootOid()).isEqualTo(unoPipeUrl.getRootOid());
  }
}
