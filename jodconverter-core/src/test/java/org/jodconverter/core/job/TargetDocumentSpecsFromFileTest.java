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

/** Contains tests for the {@link TargetDocumentSpecsFromFile} class. */
class TargetDocumentSpecsFromFileTest {

  @Nested
  class New {

    @Test
    @SuppressWarnings("ConstantConditions")
    void whenFileIsNull_ShouldThrowNullPointerException() {

      assertThatNullPointerException().isThrownBy(() -> new TargetDocumentSpecsFromFile(null));
    }

    @Test
    void whenFileIsNotNull_ShouldCreateSpecsWithExpectedValues(@TempDir final File testFolder) {

      final File file = new File(testFolder, "test.txt");
      final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(file);

      assertThat(specs.getFile()).isEqualTo(file);
    }
  }

  @Nested
  class OnComplete {

    @Test
    void whenFileExists_ShouldNotDeleteFile(@TempDir final File testFolder) throws IOException {

      final File file = new File(testFolder, "test.txt");
      assertThat(file.createNewFile()).isTrue();
      final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(file);

      specs.onComplete(file);

      assertThat(file).exists();
    }
  }

  @Nested
  class OnFailure {

    @Test
    void whenFileExists_ShouldDeleteFile(@TempDir final File testFolder) throws IOException {

      final File targetFile = new File(testFolder, "target.txt");
      assertThat(targetFile.createNewFile()).isTrue();

      final TargetDocumentSpecsFromFile specs = new TargetDocumentSpecsFromFile(targetFile);

      specs.onFailure(targetFile, new IOException());

      // Check that the temp file is deleted
      assertThat(targetFile).doesNotExist();
    }
  }
}
