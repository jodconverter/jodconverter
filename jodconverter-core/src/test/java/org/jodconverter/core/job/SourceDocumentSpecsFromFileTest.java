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

package org.jodconverter.core.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Contains tests for the {@link SourceDocumentSpecsFromFile} class. */
class SourceDocumentSpecsFromFileTest {

  @Nested
  class New {

    @Test
    void whenFileDoesNotExist_ShouldThrowIllegalArgumentsException(@TempDir final File testFolder) {

      final File file = new File(testFolder, "test.txt");
      assertThatIllegalArgumentException().isThrownBy(() -> new SourceDocumentSpecsFromFile(file));
    }

    @Test
    void whenFileExists_ShouldCreateSpecsWithExpectedValues(@TempDir final File testFolder)
        throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(file);

      assertThat(specs.getFile()).isEqualTo(file);
    }
  }

  @Nested
  class OnConsume {

    @Test
    void whenFileExists_ShouldNotDeleteFile(@TempDir final File testFolder) throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(file);

      specs.onConsumed(file);

      assertThat(file).exists();
    }
  }
}
