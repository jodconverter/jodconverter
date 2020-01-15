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

package org.jodconverter;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeManager;

public class OnlineConverterTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private OfficeManager officeManager;

  /** Setup the office manager before each test. */
  @BeforeEach
  public void setUp() {

    officeManager = mock(OfficeManager.class);
  }

  @Test
  public void convert_WithoutOfficeManagerInstalled_ThrowsIllegalStateException(
      @TempDir File testFolder) {

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
      @TempDir File testFolder) {

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
      @TempDir File testFolder) {

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
}
