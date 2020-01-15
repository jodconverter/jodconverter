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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.document.UpdateDocMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.InstalledOfficeManagerHolder;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.LocalConversionTask;

public class LocalConverterTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private OfficeManager officeManager;

  @BeforeEach
  public void setUp() {
    officeManager = mock(OfficeManager.class);
  }

  @Test
  public void convert_WithoutOfficeManagerInstalled_ThrowsIllegalStateException(
      @TempDir File testFolder) {

    final OfficeManager manager = InstalledOfficeManagerHolder.getInstance();
    InstalledOfficeManagerHolder.setInstance(null);

    final File targetFile = new File(testFolder, "test.pdf");
    try {
      assertThatIllegalStateException()
          .isThrownBy(() -> LocalConverter.make().convert(SOURCE_FILE).to(targetFile).execute());
    } finally {
      InstalledOfficeManagerHolder.setInstance(manager);
    }
  }

  @Test
  public void convert_WithCustomLoadProperties_CreateConverterWithExpectedLoadProperties(
      @TempDir File testFolder) throws OfficeException {

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", false);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

    final File targetFile = new File(testFolder, "test.pdf");

    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(officeManager)
                    .loadProperties(loadProperties)
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    // Verify that the office manager has executed a task with the expected properties.
    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(arg.capture());
    assertThat(arg.getValue()).extracting("loadProperties").isEqualTo(loadProperties);
  }

  @Test
  public void convert_WithCustomStoreProperties_CreateConverterWithExpectedStoreProperties(
      @TempDir File testFolder) throws OfficeException {

    final Map<String, Object> filterData = new HashMap<>();
    filterData.put("PageRange", "1");
    final Map<String, Object> storeProperties = new HashMap<>();
    storeProperties.put("FilterData", filterData);
    final File targetFile = new File(testFolder, "test.pdf");

    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(officeManager)
                    .storeProperties(storeProperties)
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    // Verify that the office manager has executed a task with the expected properties.
    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(arg.capture());
    assertThat(arg.getValue()).extracting("storeProperties").isEqualTo(storeProperties);
  }

  @Test
  public void convert_WithNonTemporaryFileMaker_ThrowsIllegalStateExceptionForInputStream(
      @TempDir File testFolder) {

    final File targetFile = new File(testFolder, "test.pdf");
    assertThatIllegalStateException()
        .isThrownBy(
            () -> {
              try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {
                LocalConverter.make(officeManager)
                    .convert(stream)
                    .as(DefaultDocumentFormatRegistry.TXT)
                    .to(targetFile)
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
                LocalConverter.make(officeManager)
                    .convert(SOURCE_FILE)
                    .to(stream)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();
              }
            })
        .withMessageMatching(".*TemporaryFileMaker.*OutputStream.*");
  }
}
