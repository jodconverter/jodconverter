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

package org.jodconverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.jodconverter.task.DefaultConversionTask;

public class DefaultConverterTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");
  private static final File BAD_SOURCE_FILE =
      new File("src/test/resources/documents/test.unsupportedext");

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

    DefaultConverter.make().convert(SOURCE_FILE).to(targetFile).execute();
  }

  @Test(expected = NullPointerException.class)
  public void convert_WithoutSourceFileFormat_ThrowsNullPointerException() throws Exception {

    final File targetFile = new File(outputDir, "test.pdf");

    DefaultConverter.make(officeManager).convert(BAD_SOURCE_FILE).to(targetFile).execute();
  }

  @Test
  public void
      convert_WithCustomLoadPropertiesFromConverter_CreateConverterWithExpectedLoadProperties()
          throws Exception {

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", false);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

    final File targetFile = new File(outputDir, "test.pdf");

    DefaultConverter.builder()
        .officeManager(officeManager)
        .defaultLoadProperties(loadProperties)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final DefaultConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("defaultLoadProperties").containsExactly(loadProperties);
  }

  @Test
  public void convert_WithCustomLoadPropertiesFromJob_CreateConverterWithExpectedLoadProperties()
      throws Exception {

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", false);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

    final File targetFile = new File(outputDir, "test.pdf");

    DefaultConverter.builder()
        .officeManager(officeManager)
        .build()
        .convert(SOURCE_FILE)
        .loadWith(loadProperties)
        .to(targetFile)
        .execute();

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final DefaultConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("source.customLoadProperties").containsExactly(loadProperties);
  }

  @Test
  public void convert_WithCustomStorePropertiesFromJob_CreateConverterWithExpectedStoreProperties()
      throws Exception {

    final Map<String, Object> filterData = new HashMap<>();
    filterData.put("PageRange", "1");
    final Map<String, Object> customProperties = new HashMap<>();
    customProperties.put("FilterData", filterData);

    final File targetFile = new File(outputDir, "test.pdf");

    DefaultConverter.builder()
        .officeManager(officeManager)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .storeWith(customProperties)
        .execute();

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final DefaultConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("target.customStoreProperties").containsExactly(customProperties);
  }

  @Test
  public void convert_WithNonTemporaryFileMaker_ThrowsIllegalStateExceptionForInputStream()
      throws Exception {

    final File targetFile = new File(outputDir, "test.pdf");

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      DefaultConverter.make(officeManager)
          .convert(inputStream)
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

    try (final FileOutputStream outputStream =
        new FileOutputStream(new File(outputDir, "test.pdf"))) {
      DefaultConverter.make(officeManager)
          .convert(SOURCE_FILE)
          .to(outputStream)
          .as(DefaultDocumentFormatRegistry.PDF)
          .execute();
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(IllegalStateException.class)
          .hasMessageMatching(".*TemporaryFileMaker.*OutputStream.*");
    }
  }
}
