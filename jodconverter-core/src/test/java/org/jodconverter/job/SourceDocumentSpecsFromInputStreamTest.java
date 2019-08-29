/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

package org.jodconverter.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.TemporaryFileMaker;

public class SourceDocumentSpecsFromInputStreamTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  private TemporaryFileMaker fileMaker;

  /**
   * Setup the file maker before each test.
   *
   * @throws IOException If an IO error occurs.
   */
  @Before
  public void setUp() throws IOException {

    fileMaker = mock(TemporaryFileMaker.class);
    given(fileMaker.makeTemporaryFile()).willReturn(new File(testFolder.getRoot(), "temp"));
  }

  @Test(expected = NullPointerException.class)
  public void ctor_WithNullInputStream_ThrowsNullPointerException() throws IOException {

    new SourceDocumentSpecsFromInputStream(null, fileMaker, true);
  }

  @Test
  public void getFile_WhenIoExceptionCatch_ThrowsDocumentSpecsIoException() throws IOException {

    given(fileMaker.makeTemporaryFile()).willReturn(testFolder.getRoot());
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(testFolder.getRoot());

    try (FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

      try {
        specs.getFile();
        fail("getFile should throw DocumentSpecsIOException");
      } catch (Exception e) {
        assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
        assertThat(e).hasCauseInstanceOf(IOException.class);
      }
    }
  }

  @Test
  public void onConsumed_WhenIoExceptionCatch_ThrowsDocumentSpecsIoException() throws IOException {

    final File tempFile = testFolder.newFile("onConsumed_WhenIoExceptionCatch.doc");
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    final FileInputStream inputStream = mock(FileInputStream.class);
    doThrow(IOException.class).when(inputStream).close();

    final SourceDocumentSpecsFromInputStream specs =
        new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

    try {
      specs.onConsumed(tempFile);
      fail("onConsumed should throw DocumentSpecsIOException");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
      assertThat(e).hasCauseInstanceOf(IOException.class);
    }
  }

  @Test
  public void onConsumed_WhenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseInputStream()
      throws IOException {

    final File tempFile = testFolder.newFile("onConsumed_WhenCloseStreamIsTrue_.doc");
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

      specs.onConsumed(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the InputStream is closed.
      assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", true);
    }
  }

  @Test
  public void onConsumed_WhenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseInputStream()
      throws IOException {

    final File tempFile = testFolder.newFile("onConsumed_WhenCloseStreamIsFalse.doc");
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

      specs.onConsumed(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the InputStream is closed.
      assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void ctor_WithValidValues_SpecsCreatedWithExpectedValues() throws IOException {

    final File tempFile = testFolder.newFile("ctor_WithValidValues.doc");
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {

      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.ODS);

      assertThat(specs)
          .extracting("inputStream", "documentFormat")
          .containsExactly(inputStream, DefaultDocumentFormatRegistry.ODS);
    }
  }
}
