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

import com.sun.star.lib.uno.helper.UnoUrl;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;

/** Contains tests for the {@link OfficeUrl} class. */
class OfficeUrlTest {

  @Test
  void withPipeName_ShouldReturnSameAsOriginalUnoUrl() {

    final OfficeUrl pipeUrl = new OfficeUrl("testPipeName");
    final UnoUrl unoPipeUrl = OfficeUrl.pipe("testPipeName");

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(pipeUrl.getConnection()).isEqualTo(unoPipeUrl.getConnection());
      softly
          .assertThat(pipeUrl.getConnectionAndParametersAsString())
          .isEqualTo(unoPipeUrl.getConnectionAndParametersAsString());
      softly
          .assertThat(pipeUrl.getConnectionParametersAsString())
          .isEqualTo(unoPipeUrl.getConnectionParametersAsString());
      softly
          .assertThat(pipeUrl.getConnectionParameters())
          .isEqualTo(unoPipeUrl.getConnectionParameters());
      softly.assertThat(pipeUrl.getProtocol()).isEqualTo(unoPipeUrl.getProtocol());
      softly
          .assertThat(pipeUrl.getProtocolAndParametersAsString())
          .isEqualTo(unoPipeUrl.getProtocolAndParametersAsString());
      softly
          .assertThat(pipeUrl.getProtocolParametersAsString())
          .isEqualTo(unoPipeUrl.getProtocolParametersAsString());
      softly
          .assertThat(pipeUrl.getProtocolParameters())
          .isEqualTo(unoPipeUrl.getProtocolParameters());
      softly.assertThat(pipeUrl.getRootOid()).isEqualTo(unoPipeUrl.getRootOid());
      // softly.assertThat(pipeUrl.toString()).isEqualTo(unoPipeUrl.toString());
    }
  }

  @Test
  void withPortNumber_ShouldReturnSameAsOriginalUnoUrl() {

    final OfficeUrl pipeUrl = new OfficeUrl(2005);
    final UnoUrl unoPipeUrl = OfficeUrl.socket(2005);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(pipeUrl.getConnection()).isEqualTo(unoPipeUrl.getConnection());
      softly
          .assertThat(pipeUrl.getConnectionAndParametersAsString())
          .isEqualTo(unoPipeUrl.getConnectionAndParametersAsString());
      softly
          .assertThat(pipeUrl.getConnectionParametersAsString())
          .isEqualTo(unoPipeUrl.getConnectionParametersAsString());
      softly
          .assertThat(pipeUrl.getConnectionParameters())
          .isEqualTo(unoPipeUrl.getConnectionParameters());
      softly.assertThat(pipeUrl.getProtocol()).isEqualTo(unoPipeUrl.getProtocol());
      softly
          .assertThat(pipeUrl.getProtocolAndParametersAsString())
          .isEqualTo(unoPipeUrl.getProtocolAndParametersAsString());
      softly
          .assertThat(pipeUrl.getProtocolParametersAsString())
          .isEqualTo(unoPipeUrl.getProtocolParametersAsString());
      softly
          .assertThat(pipeUrl.getProtocolParameters())
          .isEqualTo(unoPipeUrl.getProtocolParameters());
      softly.assertThat(pipeUrl.getRootOid()).isEqualTo(unoPipeUrl.getRootOid());
      // softly.assertThat(pipeUrl.toString()).isEqualTo(unoPipeUrl.toString());
    }
  }
}
