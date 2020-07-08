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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.SimpleOfficeManager;
import org.jodconverter.core.task.OfficeTask;

/** Contains tests for the {@link AbstractConversionJob} class. */
class AbstractConversionJobWithSourceFormatUnspecifiedTest {

  @Nested
  class New {

    @Test
    void whenNullSource_ShouldThrowNullPointerException() {

      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                      null,
                      SimpleOfficeManager.make(),
                      DefaultDocumentFormatRegistry.getInstance()));
    }

    @Test
    void whenNullOfficeManager_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                      new SourceDocumentSpecsFromFile(sourceFile),
                      null,
                      DefaultDocumentFormatRegistry.getInstance()));
    }

    @Test
    void whenNullRegistry_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                      new SourceDocumentSpecsFromFile(sourceFile),
                      SimpleOfficeManager.make(),
                      null));
    }
  }

  @Nested
  class As {

    @Test
    void whenNull_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();
      assertThatNullPointerException()
          .isThrownBy(
              () ->
                  new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                          new SourceDocumentSpecsFromFile(sourceFile),
                          SimpleOfficeManager.make(),
                          DefaultDocumentFormatRegistry.getInstance())
                      .as(null));
    }

    @Test
    void whenNotNull_ShouldSetDocumentFormat(@TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source");

      assertThat(sourceFile.createNewFile()).isTrue();

      final AbstractConversionJobWithSourceFormatUnspecified job =
          new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                  new SourceDocumentSpecsFromFile(sourceFile),
                  SimpleOfficeManager.make(),
                  DefaultDocumentFormatRegistry.getInstance())
              .as(DefaultDocumentFormatRegistry.PDF);
      assertThat(job.source.getFormat()).isEqualTo(DefaultDocumentFormatRegistry.PDF);
    }
  }

  @Nested
  class toFile {

    @Test
    void whenKnownExtension_ShouldCreateJobWithTargetFormat(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      final AbstractConversionJob job =
          new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                  new SourceDocumentSpecsFromFile(sourceFile),
                  SimpleOfficeManager.make(),
                  DefaultDocumentFormatRegistry.getInstance())
              .to(targetFile);

      assertThat(job.target.getFormat()).isNotNull();
    }

    @Test
    void whenUnknownExtension_ShouldCreateJobWithoutTargetFormat(@TempDir final File testFolder)
        throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target");
      assertThat(sourceFile.createNewFile()).isTrue();

      final AbstractConversionJob job =
          new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                  new SourceDocumentSpecsFromFile(sourceFile),
                  SimpleOfficeManager.make(),
                  DefaultDocumentFormatRegistry.getInstance())
              .to(targetFile);

      assertThat(job.target.getFormat()).isNull();
    }
  }

  @Nested
  class convertStream {

    @Test
    void withDefaultCloseStream_ShouldCreateJobWithCloseStreamSetToTrue(
        @TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
        final AbstractConversionJob job =
            new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                    new SourceDocumentSpecsFromFile(sourceFile),
                    SimpleOfficeManager.make(),
                    DefaultDocumentFormatRegistry.getInstance())
                .to(outputStream);
        assertThat(job.target).hasFieldOrPropertyWithValue("closeStream", true);
      }
    }

    @Test
    void withCloseStreamIsFalse_ShouldCreateJobWithCloseStreamSetToFalse(
        @TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
        final AbstractConversionJob job =
            new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                    new SourceDocumentSpecsFromFile(sourceFile),
                    SimpleOfficeManager.make(),
                    DefaultDocumentFormatRegistry.getInstance())
                .to(outputStream, false);
        assertThat(job.target).hasFieldOrPropertyWithValue("closeStream", false);
      }
    }

    @Test
    void withManagerNotSupportingFileMaker_ShouldThrowIllegalStateException(
        @TempDir final File testFolder) throws IOException {

      final File sourceFile = new File(testFolder, "source.txt");
      final File targetFile = new File(testFolder, "target.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
        final SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified job =
            new SimpleConverter.SimpleConversionJobWithSourceFormatUnspecified(
                new SourceDocumentSpecsFromFile(sourceFile),
                new OfficeManager() {

                  @Override
                  public void execute(final OfficeTask task) {}

                  @Override
                  public boolean isRunning() {
                    return false;
                  }

                  @Override
                  public void start() {}

                  @Override
                  public void stop() {}
                },
                DefaultDocumentFormatRegistry.getInstance());

        assertThatIllegalStateException()
            .isThrownBy(() -> job.to(outputStream))
            .withMessage(
                "An office manager must implements the TemporaryFileMaker "
                    + "interface in order to be able to convert to OutputStream");
      }
    }
  }
}
