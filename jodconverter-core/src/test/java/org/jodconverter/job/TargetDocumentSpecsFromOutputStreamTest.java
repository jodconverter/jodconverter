/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.TemporaryFileMaker;

public class TargetDocumentSpecsFromOutputStreamTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";
  private static final String TARGET_FILENAME = "test.pdf";

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
  public void ctor_WithNullOutputStream_ThrowsNullPointerException() throws IOException {

    new TargetDocumentSpecsFromOutputStream(null, fileMaker, true);
  }

  @Test
  public void onComplete_WhenIoExceptionCatch_ThrowsDocumentSpecsIoException() throws IOException {

    final File tempFile = testFolder.newFile("onComplete_WhenIoExceptionCatch.txt");
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    final FileOutputStream outputStream = mock(FileOutputStream.class);
    doThrow(IOException.class)
        .when(outputStream)
        .write(isA(byte[].class), isA(int.class), isA(int.class));

    final TargetDocumentSpecsFromOutputStream specs =
        new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

    try {
      specs.onComplete(tempFile);
      fail("onComplete should throw DocumentSpecsIOException");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DocumentSpecsIOException.class);
      assertThat(e).hasCauseInstanceOf(IOException.class);
    }
  }

  @Test
  public void onComplete_WhenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseOutputStream()
      throws IOException {

    final File tempFile = testFolder.newFile("onComplete_WhenCloseStreamIsTrue.txt");
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder.getRoot(), TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, true);

      specs.onComplete(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", true);
    }
  }

  @Test
  public void onConsumed_WhenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = testFolder.newFile("onConsumed_WhenCloseStreamIsFalse.txt");
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder.getRoot(), TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

      specs.onComplete(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void onFailure_WhenCloseStreamIsTrue_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = testFolder.newFile("onFailure_WhenCloseStreamIsTrue.txt");
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder.getRoot(), TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, true);

      specs.onFailure(tempFile, new IOException());

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void onFailure_WhenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseOutputStream()
      throws IOException {

    final File tempFile = testFolder.newFile("onFailure_WhenCloseStreamIsFalse.txt");
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder.getRoot(), TARGET_FILENAME))) {
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);

      specs.onFailure(tempFile, new IOException());

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the OutputStream is not closed.
      assertThat((Object) outputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void ctor_WithValidValues_SpecsCreatedWithExpectedValues() throws IOException {

    final File tempFile = testFolder.newFile("ctor_WithValidValues.txt");
    FileUtils.copyFile(new File(SOURCE_FILE), tempFile);
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileOutputStream outputStream =
        new FileOutputStream(new File(testFolder.getRoot(), TARGET_FILENAME))) {

      final Map<String, Object> storeProperties = new HashMap<>();
      storeProperties.put("Overwrite", true);
      final TargetDocumentSpecsFromOutputStream specs =
          new TargetDocumentSpecsFromOutputStream(outputStream, fileMaker, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.CSV);

      assertThat(specs)
          .extracting("outputStream", "documentFormat")
          .containsExactly(outputStream, DefaultDocumentFormatRegistry.CSV);
    }
  }
}
