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

import static org.assertj.core.api.Assertions.*;

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
import org.jodconverter.core.pdf.PdfOptions;

/** Contains tests for the {@link AbstractConversionJob} class. */
class AbstractConversionJobTest {

  @Nested
  class New {

    @Test
    void whenNullSource_ShouldThrowNullPointerException(@TempDir final File testFolder) {

      final var targetFile = new File(testFolder, "target.txt");
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

      final var sourceFile = new File(testFolder, "source.txt");
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

      final var sourceFile = new File(testFolder, "source.txt");
      final var targetFile = new File(testFolder, "target.txt");
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

      final var sourceFile = new File(testFolder, "source.txt");
      final var targetFile = new File(testFolder, "target.txt");

      assertThat(sourceFile.createNewFile()).isTrue();

      final var job =
          new SimpleConverter.SimpleConversionJob(
                  SimpleOfficeManager.make(),
                  new SourceDocumentSpecsFromFile(sourceFile),
                  new TargetDocumentSpecsFromFile(targetFile))
              .as(DefaultDocumentFormatRegistry.PDF);
      assertThat(job.target.getFormat()).isEqualTo(DefaultDocumentFormatRegistry.PDF);
    }
  }

  @Nested
  class With {

    private AbstractConversionJob newJob(final File testFolder, final String targetName)
        throws IOException {
      return newJob(SimpleOfficeManager.make(), testFolder, targetName);
    }

    private AbstractConversionJob newJob(
        final OfficeManager manager, final File testFolder, final String targetName)
        throws IOException {

      final var sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();
      final var target = new TargetDocumentSpecsFromFile(new File(testFolder, targetName));
      target.setDocumentFormat(
          DefaultDocumentFormatRegistry.getFormatByExtension(
              targetName.substring(targetName.lastIndexOf('.') + 1)));
      return new SimpleConverter.SimpleConversionJob(
          manager, new SourceDocumentSpecsFromFile(sourceFile), target);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void whenNull_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final var job = newJob(testFolder, "target.pdf");
      assertThatNullPointerException().isThrownBy(() -> job.with(null));
    }

    @Test
    void whenNotNull_ShouldSetTargetOptions(@TempDir final File testFolder) throws IOException {

      final var options = PdfOptions.archive();
      final var job = newJob(testFolder, "target.pdf");

      assertThat(job.target.getOptions()).isNull();
      assertThat(job.with(options)).isSameAs(job);
      assertThat(job.target.getOptions()).isSameAs(options);
    }

    @Test
    void whenOptionsSupportTargetFormat_ShouldExecute(@TempDir final File testFolder)
        throws IOException, OfficeException {

      final var manager = SimpleOfficeManager.make();
      try {
        manager.start();
        final var job = newJob(manager, testFolder, "target.pdf");
        assertThatCode(() -> job.with(PdfOptions.archive()).execute()).doesNotThrowAnyException();
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    }

    @Test
    void whenOptionsDoNotSupportTargetFormat_ShouldThrowIllegalArgumentException(
        @TempDir final File testFolder) throws IOException {

      final var job = newJob(testFolder, "target.odt");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> job.with(PdfOptions.archive()).execute())
          .withMessage("PdfOptions cannot be applied to a target document of format 'odt'");
    }

    @Test
    void whenJobDoesNotSupportOptions_ShouldThrowUnsupportedOperationException() {

      final ConversionJob job = () -> {};
      assertThatExceptionOfType(UnsupportedOperationException.class)
          .isThrownBy(() -> job.with(PdfOptions.archive()));
    }
  }

  @Nested
  class DefaultTargetOptions {

    // Executes a conversion to the given target with the given converter options, and returns
    // the options the conversion ended up with.
    private TargetOptions execute(
        final File testFolder,
        final String targetName,
        final TargetOptions conversionOptions,
        final TargetOptions... converterOptions)
        throws IOException, OfficeException {

      final var sourceFile = new File(testFolder, "source.txt");
      assertThat(sourceFile.createNewFile()).isTrue();

      final var manager = SimpleOfficeManager.make();
      try {
        manager.start();
        final var builder =
            SimpleConverter.builder()
                .officeManager(manager)
                .formatRegistry(DefaultDocumentFormatRegistry.getInstance());
        for (final var options : converterOptions) {
          builder.defaultTargetOptions(options);
        }
        final var job =
            (AbstractConversionJob)
                builder.build().convert(sourceFile).to(new File(testFolder, targetName));
        if (conversionOptions != null) {
          job.with(conversionOptions);
        }
        job.execute();
        return job.target.getOptions();
      } finally {
        OfficeUtils.stopQuietly(manager);
      }
    }

    @Test
    void withoutOptions_ShouldHaveNoOptions(@TempDir final File testFolder) throws Exception {
      assertThat(execute(testFolder, "target.pdf", null)).isNull();
    }

    @Test
    void whenDefaultOptionsSupportTargetFormat_ShouldUseThem(@TempDir final File testFolder)
        throws Exception {

      final var defaultOptions = PdfOptions.archive();
      assertThat(execute(testFolder, "target.pdf", null, defaultOptions)).isSameAs(defaultOptions);
    }

    @Test
    void whenDefaultOptionsDoNotSupportTargetFormat_ShouldIgnoreThem(@TempDir final File testFolder)
        throws Exception {

      assertThat(execute(testFolder, "target.odt", null, PdfOptions.archive())).isNull();
    }

    @Test
    void whenSeveralDefaultOptionsSupportTargetFormat_ShouldUseTheFirst(
        @TempDir final File testFolder) throws Exception {

      final var first = PdfOptions.archive();
      final var second = PdfOptions.compact();
      assertThat(execute(testFolder, "target.pdf", null, first, second)).isSameAs(first);
    }

    @Test
    void whenConversionHasOptions_ShouldUseThemInsteadOfTheDefaultOnes(
        @TempDir final File testFolder) throws Exception {

      final var conversionOptions = PdfOptions.compact();
      assertThat(execute(testFolder, "target.pdf", conversionOptions, PdfOptions.archive()))
          .isSameAs(conversionOptions);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void whenNullDefaultOptions_ShouldThrowNullPointerException() {
      assertThatNullPointerException()
          .isThrownBy(() -> SimpleConverter.builder().defaultTargetOptions(null));
    }
  }

  @Nested
  class Execute {

    @Test
    void withUnknownTargetFormat_ShouldThrowNullPointerException(@TempDir final File testFolder)
        throws IOException {

      final var sourceFile = new File(testFolder, "source.txt");
      final var targetFile = new File(testFolder, "target");
      assertThat(sourceFile.createNewFile()).isTrue();

      final var job =
          new SimpleConverter.SimpleConversionJob(
              SimpleOfficeManager.make(),
              new SourceDocumentSpecsFromFile(sourceFile),
              new TargetDocumentSpecsFromFile(targetFile));
      assertThatNullPointerException().isThrownBy(job::execute);
    }

    @Test
    void withKnownTargetFormat_ShouldExecute(@TempDir final File testFolder)
        throws IOException, OfficeException {

      final var sourceFile = new File(testFolder, "source.txt");
      final var targetFile = new File(testFolder, "target");
      assertThat(sourceFile.createNewFile()).isTrue();

      final var manager = SimpleOfficeManager.make();
      try {
        manager.start();
        final var job =
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
