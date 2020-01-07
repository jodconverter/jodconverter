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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import com.sun.star.document.UpdateDocMode;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.LocalConversionTask;

public class LocalConverterTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();
  private static File outputDir;

  private OfficeManager officeManager;

  /**
   * Creates a output directory for our tests.
   *
   * @throws IOException If an IO error occurs.
   */
  @BeforeClass
  public static void setUpClass() throws IOException {

    // Creates an output directory for the class
    outputDir = testFolder.newFolder();
  }

  /** Setup the office manager before each test. */
  @Before
  public void setUp() {

    officeManager = mock(OfficeManager.class);
  }

  @Test(expected = IllegalStateException.class)
  public void convert_WithoutOfficeManagerInstalled_ThrowsIllegalStateException() throws Exception {

    final File targetFile = new File(outputDir, "test.pdf");

    LocalConverter.make().convert(SOURCE_FILE).to(targetFile).execute();
  }

  @Test
  public void convert_WithCustomLoadProperties_CreateConverterWithExpectedLoadProperties()
      throws Exception {

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", false);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

    final File targetFile = new File(outputDir, "test.pdf");

    LocalConverter.builder()
        .officeManager(officeManager)
        .loadProperties(loadProperties)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<LocalConversionTask> taskArgument =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final LocalConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("loadProperties").isEqualTo(loadProperties);
  }

  @Test
  public void convert_WithCustomStoreProperties_CreateConverterWithExpectedStoreProperties()
      throws Exception {

    final Map<String, Object> filterData = new HashMap<>();
    filterData.put("PageRange", "1");
    final Map<String, Object> storeProperties = new HashMap<>();
    storeProperties.put("FilterData", filterData);

    final File targetFile = new File(outputDir, "test.pdf");

    LocalConverter.builder()
        .officeManager(officeManager)
        .storeProperties(storeProperties)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<LocalConversionTask> taskArgument =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final LocalConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("storeProperties").isEqualTo(storeProperties);
  }

  @Test
  public void convert_WithNonTemporaryFileMaker_ThrowsIllegalStateExceptionForInputStream()
      throws Exception {

    final File targetFile = new File(outputDir, "test.pdf");

    try (InputStream stream = Files.newInputStream(targetFile.toPath())) {
      LocalConverter.make(officeManager)
          .convert(stream)
          .as(DefaultDocumentFormatRegistry.TXT)
          .to(targetFile)
          .execute();
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(IllegalStateException.class)
          .hasMessageMatching(".*TemporaryFileMaker.*InputStream.*");
    }
  }

  @Test
  public void convert_WithNonTemporaryFileMaker_ThrowsIllegalStateExceptionForOutputStream()
      throws Exception {

    try (OutputStream stream = Files.newOutputStream(new File(outputDir, "test.pdf").toPath())) {
      LocalConverter.make(officeManager)
          .convert(SOURCE_FILE)
          .to(stream)
          .as(DefaultDocumentFormatRegistry.PDF)
          .execute();
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(IllegalStateException.class)
          .hasMessageMatching(".*TemporaryFileMaker.*OutputStream.*");
    }
  }
}
