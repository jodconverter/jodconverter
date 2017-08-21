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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.sun.star.document.UpdateDocMode;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.filter.DefaultFilterChain;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.filter.RefreshFilter;
import org.jodconverter.filter.text.PageCounterFilter;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.DefaultConversionTask;

public class OfficeDocumentConverterTest {

  private static final String TEST_OUTPUT_DIR = "build/test-results/";
  private static final File SOURCE_FILE = new File("src/integTest/resources/documents/test.doc");

  private static File outputDir;

  private OfficeManager officeManager;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, OfficeDocumentConverterTest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  /** Setup the office manager before each test. */
  @Before
  public void setUp() {

    officeManager = mock(OfficeManager.class);
  }

  @Test
  public void convert_WithSourceFileAndTargetFile_TaskExecutedWithExpectedProperties()
      throws OfficeException {

    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    final File targetFile = new File(outputDir, "test.pdf");
    converter.convert(SOURCE_FILE, targetFile);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final DefaultConversionTask task = taskArgument.getValue();
    assertThat(task)
        .extracting(
            "source.file",
            "source.documentFormat",
            "target.file",
            "target.documentFormat",
            "filterChain")
        .containsExactly(
            SOURCE_FILE,
            DefaultDocumentFormatRegistry.DOC,
            targetFile,
            DefaultDocumentFormatRegistry.PDF,
            RefreshFilter.CHAIN);
  }

  @Test
  public void convert_WithSourceFileAndTargetFileAndFilterChain_TaskExecutedWithExpectedProperties()
      throws OfficeException {

    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    final FilterChain chain =
        new DefaultFilterChain(new PageCounterFilter(), RefreshFilter.REFRESH);

    final File targetFile = new File(outputDir, "test.pdf");
    converter.convert(chain, SOURCE_FILE, targetFile);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final DefaultConversionTask task = taskArgument.getValue();
    assertThat(task)
        .extracting(
            "source.file",
            "source.documentFormat",
            "target.file",
            "target.documentFormat",
            "filterChain")
        .containsExactly(
            SOURCE_FILE,
            DefaultDocumentFormatRegistry.DOC,
            targetFile,
            DefaultDocumentFormatRegistry.PDF,
            chain);
  }

  @Test
  public void convert_WithSourceFormatAndTargetFormat_TaskExecutedWithExpectedProperties()
      throws OfficeException {

    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    final File targetFile = new File(outputDir, "test.tmp");
    converter.convert(
        SOURCE_FILE,
        targetFile,
        DefaultDocumentFormatRegistry.TXT,
        DefaultDocumentFormatRegistry.PDF);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final DefaultConversionTask task = taskArgument.getValue();
    assertThat(task)
        .extracting(
            "source.file",
            "source.documentFormat",
            "target.file",
            "target.documentFormat",
            "filterChain")
        .containsExactly(
            SOURCE_FILE,
            DefaultDocumentFormatRegistry.TXT,
            targetFile,
            DefaultDocumentFormatRegistry.PDF,
            RefreshFilter.CHAIN);
  }

  @Test
  public void convert_WithDefaultLoadProperties_TaskExecutedWithExpectedLoadProperties()
      throws OfficeException {

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);
    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
    converter.setDefaultLoadProperties(loadProperties);

    final File targetFile = new File(outputDir, "test.pdf");
    converter.convert(SOURCE_FILE, targetFile);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final DefaultConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("defaultLoadProperties").containsExactly(loadProperties);
  }
}
