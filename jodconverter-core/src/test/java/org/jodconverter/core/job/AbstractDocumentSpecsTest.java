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

package org.jodconverter.core.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;

/** Contains tests for the {@link AbstractDocumentSpecs} class. */
class AbstractDocumentSpecsTest {

  static class TestSpecs extends AbstractDocumentSpecs {
    TestSpecs(final File file) {
      super(file);
    }
  }

  @Nested
  class New {

    @Test
    void whenNull_ShouldThrowNullPointerException() {

      assertThatNullPointerException().isThrownBy(() -> new TestSpecs(null));
    }

    @Test
    void whenNotNull_ShouldCreateSpecsWithExpectedValues(@TempDir final File testFolder)
        throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(file);

      assertThat(specs.getFile()).isEqualTo(file);
    }
  }

  @Nested
  class SetDocumentFormat {

    @Test
    void whenNull_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(file);

      assertThatNullPointerException().isThrownBy(() -> specs.setDocumentFormat(null));
    }

    @Test
    void whenNotNull_ShouldAssignExpectedDocumentFormat(@TempDir final File testFolder)
        throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(file);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);

      assertThat(specs.getFormat()).isEqualTo(DefaultDocumentFormatRegistry.TXT);
    }
  }

  @Nested
  class ToString {

    @Test
    void whenDocumentFormatIsNull_ShouldReturnStringWithNullDocumentFormat(
        @TempDir final File testFolder) throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(file);

      assertThat(specs.toString()).contains("file=test.txt", "format=null");
    }

    @Test
    void whenNotNull_ShouldAssignExpectedDocumentFormat(@TempDir final File testFolder)
        throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(file);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);

      assertThat(specs.toString()).contains("file=test.txt", "format=txt");
    }
  }
}
