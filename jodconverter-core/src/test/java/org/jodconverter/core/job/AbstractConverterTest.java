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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.SimpleOfficeManager;
import org.jodconverter.core.task.OfficeTask;

/** Contains tests for the {@link AbstractConverter} class. */
class AbstractConverterTest {

  @Nested
  class New {

    @Test
    void whenNullManager_ShouldThrowNullPointerException() {
      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  SimpleConverter.builder()
                      .formatRegistry(DefaultDocumentFormatRegistry.getInstance())
                      .build());
    }

    @Test
    void whenNullRegistry_ShouldThrowNullPointerException() {
      assertThatNullPointerException()
          .isThrownBy(
              () -> SimpleConverter.builder().officeManager(SimpleOfficeManager.make()).build());
    }
  }

  @Nested
  class ConvertFile {

    @Test
    void whenKnownExtension_ShouldCreateJobWithSourceFormat(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      final SimpleConverter converter = SimpleConverter.make();
      final SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified job =
          (SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified)
              converter.convert(sourceFile);

      assertThat(job.source.getFormat()).isNotNull();
    }

    @Test
    void whenUnknownExtension_ShouldCreateJobWithoutSourceFormat(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source");
      assertThat(sourceFile.createNewFile()).isTrue();

      final SimpleConverter converter = SimpleConverter.make();
      final SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified job =
          (SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified)
              converter.convert(sourceFile);

      assertThat(job.source.getFormat()).isNull();
    }
  }

  @Nested
  class ConvertStream {

    @Test
    void withDefaultCloseStream_ShouldCreateJobWithCloseStreamSetToTrue(
        @TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
        final SimpleConverter converter = SimpleConverter.make();
        final SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified job =
            (SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified)
                converter.convert(inputStream);
        assertThat(job.source).hasFieldOrPropertyWithValue("closeStream", true);
      }
    }

    @Test
    void withCloseStreamIsFalse_ShouldCreateJobWithCloseStreamSetToFalse(
        @TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
        final SimpleConverter converter = SimpleConverter.make();
        final SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified job =
            (SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified)
                converter.convert(inputStream, false);
        assertThat(job.source).hasFieldOrPropertyWithValue("closeStream", false);
      }
    }

    @Test
    void withManagerNotSupportingFileMaker_ShouldThrowIllegalStateException(
        @TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (InputStream inputStream = Files.newInputStream(sourceFile.toPath())) {
        final SimpleConverter converter =
            SimpleConverter.builder()
                .officeManager(
                    new OfficeManager() {

                      @Override
                      public void execute(
                          @SuppressWarnings("NullableProblems") final OfficeTask task) {
                        // Ignore
                      }

                      @Override
                      public boolean isRunning() {
                        return false;
                      }

                      @Override
                      public void start() {
                        // Ignore
                      }

                      @Override
                      public void stop() {
                        // Ignore
                      }
                    })
                .formatRegistry(DefaultDocumentFormatRegistry.getInstance())
                .build();

        assertThatIllegalStateException()
            .isThrownBy(() -> converter.convert(inputStream))
            .withMessage(
                "An office manager must implements the TemporaryFileMaker "
                    + "interface in order to be able to convert InputStream");
      }
    }
  }

  @Nested
  class GetFormatRegistry {

    @Test
    void shouldReturnExpectedRegistry() {
      assertThat(SimpleConverter.make().getFormatRegistry())
          .isEqualTo(DefaultDocumentFormatRegistry.getInstance());
    }
  }
}
