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

package org.jodconverter.online;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.task.AbstractOfficeTask;

/** Contains tests for the {@link OnlineConverter} class. */
public class OnlineConverterTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private OfficeManager officeManager;

  /** Setup the office manager before each test. */
  @BeforeEach
  public void setUp() {

    officeManager = mock(OfficeManager.class);
  }

  @Test
  public void make_WithOfficeManagerInstalled_Success(final @TempDir File testFolder) {

    final OfficeManager manager = InstalledOfficeManagerHolder.getInstance();
    InstalledOfficeManagerHolder.setInstance(officeManager);

    try {
      assertThatCode(
              () ->
                  OnlineConverter.make()
                      .convert(SOURCE_FILE)
                      .to(new File(testFolder, "test.pdf"))
                      .execute())
          .doesNotThrowAnyException();
    } finally {
      InstalledOfficeManagerHolder.setInstance(manager);
    }
  }

  @Test
  public void convert_WithoutOfficeManagerInstalled_ThrowsIllegalStateException(
      final @TempDir File testFolder) {

    assertThatIllegalStateException()
        .isThrownBy(
            () ->
                OnlineConverter.make()
                    .convert(SOURCE_FILE)
                    .to(new File(testFolder, "test.pdf"))
                    .execute());
  }

  @Test
  public void convert_WithNonTemporaryFileMaker_ThrowsIllegalStateExceptionForInputStream(
      final @TempDir File testFolder) {

    assertThatIllegalStateException()
        .isThrownBy(
            () -> {
              try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
                OnlineConverter.make(officeManager)
                    .convert(stream)
                    .as(DefaultDocumentFormatRegistry.TXT)
                    .to(new File(testFolder, "test.pdf"))
                    .execute();
              }
            })
        .withMessageMatching(".*TemporaryFileMaker.*InputStream.*");
  }

  @Test
  public void convert_WithNonTemporaryFileMaker_ThrowsIllegalStateExceptionForOutputStream(
      final @TempDir File testFolder) {

    assertThatIllegalStateException()
        .isThrownBy(
            () -> {
              try (OutputStream stream =
                  Files.newOutputStream(new File(testFolder, "test.pdf").toPath())) {
                OnlineConverter.make(officeManager)
                    .convert(SOURCE_FILE)
                    .to(stream)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();
              }
            })
        .withMessageMatching(".*TemporaryFileMaker.*OutputStream.*");
  }

  @Test
  public void toString_AsExpected() {

    final SourceDocumentSpecs source =
        new SourceDocumentSpecs() {
          @Override
          public void onConsumed(File file) {}

          @Override
          public File getFile() {
            return null;
          }

          @Override
          public DocumentFormat getFormat() {
            return null;
          }
        };

    final AbstractOfficeTask obj =
        new AbstractOfficeTask(source) {
          @Override
          public void execute(OfficeContext context) {
            return;
          }
        };

    Assertions.assertThat(obj.toString()).startsWith("{source=").endsWith("}");
  }
}
