/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import com.sun.star.document.UpdateDocMode;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.filter.DefaultFilterChain;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.filter.RefreshFilter;
import org.jodconverter.filter.text.PageCounterFilter;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.LocalConversionTask;

public class OfficeDocumentConverterTest {

  private static final File SOURCE_FILE = new File("src/integTest/resources/documents/test.doc");

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  private OfficeManager officeManager;

  /** Setup the office manager before each test. */
  @Before
  public void setUp() {

    officeManager = mock(OfficeManager.class);
  }

  @Test
  public void convert_WithSourceFileAndTargetFile_TaskExecutedWithExpectedProperties()
      throws OfficeException {

    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    final File targetFile = new File(testFolder.getRoot(), "test.pdf");
    converter.convert(SOURCE_FILE, targetFile);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<LocalConversionTask> taskArgument =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final LocalConversionTask task = taskArgument.getValue();
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
      throws Exception {

    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    final FilterChain chain = new DefaultFilterChain(new PageCounterFilter());

    final File targetFile = new File(testFolder.getRoot(), "test.pdf");
    converter.convert(chain, SOURCE_FILE, targetFile);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<LocalConversionTask> taskArgument =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final LocalConversionTask task = taskArgument.getValue();
    assertThat(task)
        .extracting(
            "source.file",
            "source.documentFormat",
            "target.file",
            "target.documentFormat",
            "filterChain.filters")
        .containsExactly(
            SOURCE_FILE,
            DefaultDocumentFormatRegistry.DOC,
            targetFile,
            DefaultDocumentFormatRegistry.PDF,
            FieldUtils.readField(chain, "filters", true));
  }

  @Test
  public void convert_WithSourceFormatAndTargetFormat_TaskExecutedWithExpectedProperties()
      throws OfficeException {

    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);

    final File targetFile = new File(testFolder.getRoot(), "test.tmp");
    converter.convert(
        SOURCE_FILE,
        targetFile,
        DefaultDocumentFormatRegistry.TXT,
        DefaultDocumentFormatRegistry.PDF);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<LocalConversionTask> taskArgument =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final LocalConversionTask task = taskArgument.getValue();
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

    final File targetFile = new File(testFolder.getRoot(), "test.pdf");
    converter.convert(SOURCE_FILE, targetFile);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<LocalConversionTask> taskArgument =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final LocalConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("loadProperties").containsExactly(loadProperties);
  }

  @Test
  public void convert_WithDefaultLoadPropertiesSetTwice_TaskExecutedWithExpectedLoadProperties()
      throws OfficeException {

    final Map<String, Object> loadProperties1 = new HashMap<>();
    loadProperties1.put("Hidden", false);
    loadProperties1.put("ReadOnly", false);
    loadProperties1.put("UpdateDocMode", UpdateDocMode.ACCORDING_TO_CONFIG);

    final Map<String, Object> loadProperties2 = new HashMap<>();
    loadProperties2.put("Hidden", true);
    loadProperties2.put("ReadOnly", true);
    loadProperties2.put("UpdateDocMode", UpdateDocMode.FULL_UPDATE);

    final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
    converter.setDefaultLoadProperties(loadProperties1);
    converter.setDefaultLoadProperties(loadProperties2);

    final File targetFile = new File(testFolder.getRoot(), "test.pdf");
    converter.convert(SOURCE_FILE, targetFile);

    // Verify that the office manager has executed a task
    // with the expected properties.
    final ArgumentCaptor<LocalConversionTask> taskArgument =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(1)).execute(taskArgument.capture());
    final LocalConversionTask task = taskArgument.getValue();
    assertThat(task).extracting("loadProperties").containsExactly(loadProperties2);
  }
}
