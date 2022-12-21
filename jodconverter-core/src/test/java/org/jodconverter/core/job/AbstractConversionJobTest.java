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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.SimpleOfficeManager;

/** Contains tests for the {@link AbstractConversionJob} class. */
class AbstractConversionJobTest {

  @Nested
  class New {

    @Test
    void whenNullSource_ShouldThrowNullPointerException(@TempDir final File testFolder) {

      final File targetFile = new File(testFolder, "target.txt");
      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  new SimpleConverter.SimpleConversionJob(
                      SimpleOfficeManager.make(),
                      null,
                      new TargetDocumentSpecsFromFile(targetFile)));
    }

    @Test
    void whenNullTarget_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  new SimpleConverter.SimpleConversionJob(
                      SimpleOfficeManager.make(),
                      new SourceDocumentSpecsFromFile(sourceFile),
                      null));
    }
  }

  @Nested
  class As {

    @Test
    @SuppressWarnings("ConstantConditions")
    void whenNull_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target.txt");
      assertThat(sourceFile.createNewFile()).isTrue();
      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  new SimpleConverter.SimpleConversionJob(
                          SimpleOfficeManager.make(),
                          new SourceDocumentSpecsFromFile(sourceFile),
                          new TargetDocumentSpecsFromFile(targetFile))
                      .as(null));
    }

    @Test
    void whenNotNull_ShouldSetDocumentFormat(@TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target.txt");

      assertThat(sourceFile.createNewFile()).isTrue();

      final AbstractConversionJob job =
          new SimpleConverter.SimpleConversionJob(
                  SimpleOfficeManager.make(),
                  new SourceDocumentSpecsFromFile(sourceFile),
                  new TargetDocumentSpecsFromFile(targetFile))
              .as(DefaultDocumentFormatRegistry.PDF);
      assertThat(job.target.getFormat()).isEqualTo(DefaultDocumentFormatRegistry.PDF);
    }
  }

  @Nested
  class Execute {

    @Test
    void withUnknownTargetFormat_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target");
      assertThat(sourceFile.createNewFile()).isTrue();

      final AbstractConversionJob job =
          new SimpleConverter.SimpleConversionJob(
              SimpleOfficeManager.make(),
              new SourceDocumentSpecsFromFile(sourceFile),
              new TargetDocumentSpecsFromFile(targetFile));
      assertThatNullPointerException().isThrownBy(job::execute);
    }

    @Test
    void withKnownTargetFormat_ShouldExecute(@TempDir final File testFolder)
        throws IOException, OfficeException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target");
      assertThat(sourceFile.createNewFile()).isTrue();

      final OfficeManager manager = SimpleOfficeManager.make();
      try {
        manager.start();
        final AbstractConversionJob job =
            new SimpleConverter.SimpleConversionJob(
                    manager,
                    new SourceDocumentSpecsFromFile(sourceFile),
                    new TargetDocumentSpecsFromFile(targetFile))
                .as(DefaultDocumentFormatRegistry.PDF);
        assertThatCode(job::execute).doesNotThrowAnyException();
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    }
  }
}
