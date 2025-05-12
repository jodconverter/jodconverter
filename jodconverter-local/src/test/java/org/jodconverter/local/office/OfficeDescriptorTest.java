/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Contains tests for the {@link OfficeDescriptor} class. */
class OfficeDescriptorTest {

  @Nested
  class LibreOffice {

    @Test
    void fromExecutablePath_ShouldReturnLibreOfficeAndGnuStyle() {

      final OfficeDescriptor descr =
          OfficeDescriptor.fromExecutablePath("C:\\Program Files\\LibreOffice");
      assertThat(descr.getProduct()).isEqualTo("LibreOffice");
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(true);
    }
  }

  @Nested
  class OpenOffice {

    @Test
    void fromExecutablePath_ShouldReturnOpenOffice() {

      final OfficeDescriptor descr =
          OfficeDescriptor.fromExecutablePath("C:\\Program Files (x86)\\OpenOffice 4");
      assertThat(descr.getProduct()).isEqualTo("OpenOffice");
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(false);
    }
  }

  @Nested
  class InvalidExecutablePath {

    @Test
    void shouldReturnUnknownInformation() {

      final OfficeDescriptor descr =
          OfficeDescriptor.fromExecutablePath("C:\\Program Files (x86)\\Foo");
      assertThat(descr.getProduct()).isEqualTo("???");
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(false);
    }
  }
}
