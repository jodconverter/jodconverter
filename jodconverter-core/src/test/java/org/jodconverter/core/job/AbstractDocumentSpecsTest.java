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
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.TemporaryFileMaker;

/** Contains tests for the {@link AbstractDocumentSpecs} class. */
class AbstractDocumentSpecsTest {

  @Test
  void isRepeatable_ShouldBeTrueForFilesAndFalseForStreamsAndByDefault(
      final @TempDir File testFolder) throws IOException {

    final var file = new File(testFolder, "test.txt");
    assertThat(file.createNewFile()).isTrue();
    final TemporaryFileMaker fileMaker = extension -> new File(testFolder, "temp." + extension);

    assertThat(new SourceDocumentSpecsFromFile(file).isRepeatable()).isTrue();
    assertThat(new TargetDocumentSpecsFromFile(file).isRepeatable()).isTrue();
    assertThat(
            new SourceDocumentSpecsFromInputStream(
                    new ByteArrayInputStream(new byte[0]), fileMaker, true)
                .isRepeatable())
        .isFalse();
    assertThat(
            new TargetDocumentSpecsFromOutputStream(new ByteArrayOutputStream(), fileMaker, true)
                .isRepeatable())
        .isFalse();
    final DocumentSpecs custom = mock(DocumentSpecs.class, CALLS_REAL_METHODS);
    assertThat(custom.isRepeatable()).isFalse();
  }

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

      final var file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final var specs = new SourceDocumentSpecsFromFile(file);

      assertThat(specs.getFile()).isEqualTo(file);
    }
  }

  @Nested
  class SetDocumentFormat {

    @Test
    void whenNull_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final var file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final var specs = new SourceDocumentSpecsFromFile(file);

      assertThatNullPointerException().isThrownBy(() -> specs.setDocumentFormat(null));
    }

    @Test
    void whenNotNull_ShouldAssignExpectedDocumentFormat(@TempDir final File testFolder)
        throws IOException {

      final var file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final var specs = new SourceDocumentSpecsFromFile(file);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);

      assertThat(specs.getFormat()).isEqualTo(DefaultDocumentFormatRegistry.TXT);
    }
  }

  @Nested
  class ToString {

    @Test
    void whenDocumentFormatIsNull_ShouldReturnStringWithNullDocumentFormat(
        @TempDir final File testFolder) throws IOException {

      final var file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final var specs = new SourceDocumentSpecsFromFile(file);

      assertThat(specs.toString()).contains("file=test.txt", "format=null");
    }

    @Test
    void whenNotNull_ShouldAssignExpectedDocumentFormat(@TempDir final File testFolder)
        throws IOException {

      final var file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final var specs = new SourceDocumentSpecsFromFile(file);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.TXT);

      assertThat(specs.toString()).contains("file=test.txt", "format=txt");
    }
  }
}
