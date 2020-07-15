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

package org.jodconverter.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.SimpleDocumentFormatRegistry;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeManager;

/** Contains tests for the {@link RemoteConverter} class. */
class RemoteConverterTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private OfficeManager officeManager;

  /** Setup the office manager before each test. */
  @BeforeEach
  public void setUp() {

    officeManager = mock(OfficeManager.class);
  }

  @Nested
  class Make {

    @Test
    void withOfficeManagerInstalled_ShouldSuccess(final @TempDir File testFolder) {

      final OfficeManager manager = InstalledOfficeManagerHolder.getInstance();
      InstalledOfficeManagerHolder.setInstance(officeManager);

      final File targetFile = new File(testFolder, "test.pdf");
      try {
        assertThatCode(() -> RemoteConverter.make().convert(SOURCE_FILE).to(targetFile).execute())
            .doesNotThrowAnyException();
      } finally {
        InstalledOfficeManagerHolder.setInstance(manager);
      }
    }

    @Test
    void withoutOfficeManagerInstalled_ShouldThrowIllegalStateException(
        final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");
      assertThatIllegalStateException()
          .isThrownBy(() -> RemoteConverter.make().convert(SOURCE_FILE).to(targetFile).execute());
    }
  }

  @Nested
  class Builder {

    @Test
    void withCustomFormatRegistry_ShouldUseCustomFormatRegistry() {

      final SimpleDocumentFormatRegistry registry = new SimpleDocumentFormatRegistry();
      registry.addFormat(DefaultDocumentFormatRegistry.DOC);
      registry.addFormat(DefaultDocumentFormatRegistry.PDF);
      final RemoteConverter manager =
          RemoteConverter.builder().officeManager(officeManager).formatRegistry(registry).build();

      assertThat(manager)
          .extracting("formatRegistry")
          .isInstanceOfSatisfying(
              SimpleDocumentFormatRegistry.class,
              simpleDocumentFormatRegistry -> {
                assertThat(simpleDocumentFormatRegistry.getFormatByExtension("txt")).isNull();
                assertThat(simpleDocumentFormatRegistry.getFormatByExtension("doc"))
                    .usingRecursiveComparison()
                    .isEqualTo(DefaultDocumentFormatRegistry.DOC);
                assertThat(simpleDocumentFormatRegistry.getFormatByExtension("pdf"))
                    .usingRecursiveComparison()
                    .isEqualTo(DefaultDocumentFormatRegistry.PDF);
              });
    }
  }

  @Nested
  class Convert {

    @Test
    void withNonTemporaryFileMaker_ShouldThrowIllegalStateExceptionForInputStream(
        final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");
      assertThatIllegalStateException()
          .isThrownBy(
              () -> {
                try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
                  RemoteConverter.make(officeManager)
                      .convert(stream)
                      .as(DefaultDocumentFormatRegistry.TXT)
                      .to(targetFile)
                      .execute();
                }
              })
          .withMessageMatching(".*TemporaryFileMaker.*InputStream.*");
    }

    @Test
    void withNonTemporaryFileMaker_ShouldThrowIllegalStateExceptionForOutputStream(
        final @TempDir File testFolder) {

      final File targetFile = new File(testFolder, "test.pdf");
      assertThatIllegalStateException()
          .isThrownBy(
              () -> {
                try (OutputStream stream = Files.newOutputStream(targetFile.toPath())) {
                  RemoteConverter.make(officeManager)
                      .convert(SOURCE_FILE)
                      .to(stream)
                      .as(DefaultDocumentFormatRegistry.PDF)
                      .execute();
                }
              })
          .withMessageMatching(".*TemporaryFileMaker.*OutputStream.*");
    }
  }
}
