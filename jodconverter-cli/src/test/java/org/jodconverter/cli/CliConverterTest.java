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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import org.jodconverter.cli.util.ExitException;
import org.jodconverter.cli.util.NoExitSecurityManager;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.InstalledOfficeManagerHolder;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.DefaultConversionTask;

public class CliConverterTest {

  //private static final String CONFIG_DIR = "src/integTest/resources/config";
  private static final String SOURCE_DIR = "src/integTest/resources/documents/";
  private static final String SOURCE_FILENAME_1 = "test1.doc";
  private static final String SOURCE_FILENAME_2 = "test2.doc";
  private static final String SOURCE_FILE_1 = SOURCE_DIR + SOURCE_FILENAME_1;
  private static final String SOURCE_FILE_2 = SOURCE_DIR + SOURCE_FILENAME_2;
  private static final String TARGET_FORMAT = "pdf";
  private static final String TARGET_FILENAME_1 = "test1.pdf";
  private static final String TARGET_FILENAME_2 = "test2.pdf";
  private static final String SOURCE_DIR_TARGET_FILE_1 = SOURCE_DIR + TARGET_FILENAME_1;
  private static final String SOURCE_DIR_TARGET_FILE_2 = SOURCE_DIR + TARGET_FILENAME_2;
  private static final String OUTPUT_DIR =
      "test-output/" + CliConverterTest.class.getSimpleName() + "/";
  private static final String OUTPUT_DIR_TARGET_FILE_1 = OUTPUT_DIR + TARGET_FILENAME_1;
  private static final String OUTPUT_DIR_TARGET_FILE_2 = OUTPUT_DIR + TARGET_FILENAME_2;

  @BeforeClass
  public static void setUpClass() {

    // Ensure we start with a fresh output directory
    final File outputDir = new File(OUTPUT_DIR);
    FileUtils.deleteQuietly(outputDir);
    outputDir.mkdirs();

    // Don't allow the program to exit the VM and redirect
    // console streams.
    System.setOut(new SystemLogHandler(System.out));
    System.setErr(new SystemLogHandler(System.err));
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @AfterClass
  public static void tearDownClass() {

    // Delete the output directory
    FileUtils.deleteQuietly(new File(OUTPUT_DIR));

    // Restore security manager
    System.setSecurityManager(null);
  }

  private OfficeManager officeManager;
  private CliConverter converter;
  private DocumentFormatRegistry registry;

  @Test
  public void convert_DirnamesToTarget_NoTaskExecuted() throws Exception {

    converter.convert(new String[] {SOURCE_DIR}, new String[] {TARGET_FILENAME_1});
    verify(officeManager, times(0)).execute(isA(DefaultConversionTask.class));
  }

  @Test
  public void convert_FilenamesToDirnames_NoTaskExecuted() throws Exception {

    converter.convert(
        new String[] {SOURCE_FILE_1, SOURCE_FILE_2}, new String[] {OUTPUT_DIR, OUTPUT_DIR});
    verify(officeManager, times(0)).execute(isA(DefaultConversionTask.class));
  }

  @Test
  public void convert_FilenamesToFilenames_TasksExecuted() throws Exception {

    converter.convert(
        new String[] {SOURCE_FILE_1, SOURCE_FILE_2},
        new String[] {OUTPUT_DIR_TARGET_FILE_1, OUTPUT_DIR_TARGET_FILE_2});

    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(2)).execute(taskArgument.capture());
    final List<DefaultConversionTask> tasks = taskArgument.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_1), new File(OUTPUT_DIR_TARGET_FILE_1));
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_2), new File(OUTPUT_DIR_TARGET_FILE_2));
  }

  @Test
  public void convert_FilenamesToFilenamesAllowingOverwrite_TasksExecuted() throws Exception {

    converter.convert(
        new String[] {SOURCE_FILE_1, SOURCE_FILE_2},
        new String[] {TARGET_FILENAME_1, TARGET_FILENAME_2},
        OUTPUT_DIR,
        true);

    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(2)).execute(taskArgument.capture());
    final List<DefaultConversionTask> tasks = taskArgument.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_1), new File(OUTPUT_DIR_TARGET_FILE_1));
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_2), new File(OUTPUT_DIR_TARGET_FILE_2));
  }

  @Test
  public void convert_FilenamesToFilenamesWithoutOverwrite_NoTaskExecuted() throws Exception {

    FileUtils.touch(new File(OUTPUT_DIR_TARGET_FILE_1));
    FileUtils.touch(new File(OUTPUT_DIR_TARGET_FILE_2));

    converter.convert(
        new String[] {SOURCE_FILE_1, SOURCE_FILE_2},
        new String[] {TARGET_FILENAME_1, TARGET_FILENAME_2},
        OUTPUT_DIR,
        false);

    verify(officeManager, times(0)).execute(isA(DefaultConversionTask.class));

    FileUtils.deleteQuietly(new File(OUTPUT_DIR_TARGET_FILE_1));
    FileUtils.deleteQuietly(new File(OUTPUT_DIR_TARGET_FILE_2));
  }

  @Test
  public void convert_FilenamesToFilenamesWithOutputDir_TasksExecuted() throws Exception {

    converter.convert(
        new String[] {SOURCE_FILE_1, SOURCE_FILE_2},
        new String[] {TARGET_FILENAME_1, TARGET_FILENAME_2},
        OUTPUT_DIR);

    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(2)).execute(taskArgument.capture());
    final List<DefaultConversionTask> tasks = taskArgument.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_1), new File(OUTPUT_DIR_TARGET_FILE_1));
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_2), new File(OUTPUT_DIR_TARGET_FILE_2));
  }

  @Test
  public void convert_FilenamesToFormat_TasksExecuted() throws Exception {

    converter.convert(new String[] {SOURCE_FILE_1, SOURCE_FILE_2}, TARGET_FORMAT);

    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(2)).execute(taskArgument.capture());
    final List<DefaultConversionTask> tasks = taskArgument.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_1), new File(SOURCE_DIR_TARGET_FILE_1));
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_2), new File(SOURCE_DIR_TARGET_FILE_2));
  }

  @Test
  public void convert_FilenamesToFormatWithOutputDir_TasksExecuted() throws Exception {

    converter.convert(new String[] {SOURCE_FILE_1, SOURCE_FILE_2}, TARGET_FORMAT, OUTPUT_DIR);

    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(2)).execute(taskArgument.capture());
    final List<DefaultConversionTask> tasks = taskArgument.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_1), new File(OUTPUT_DIR_TARGET_FILE_1));
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_2), new File(OUTPUT_DIR_TARGET_FILE_2));
  }

  @Test
  public void convert_FilenamesToTargetAllowingOverwrite_TasksExecuted() throws Exception {

    FileUtils.touch(new File(OUTPUT_DIR_TARGET_FILE_1));
    FileUtils.touch(new File(OUTPUT_DIR_TARGET_FILE_2));

    converter.convert(new String[] {SOURCE_FILE_1, SOURCE_FILE_2}, TARGET_FORMAT, OUTPUT_DIR, true);

    final ArgumentCaptor<DefaultConversionTask> taskArgument =
        ArgumentCaptor.forClass(DefaultConversionTask.class);
    verify(officeManager, times(2)).execute(taskArgument.capture());
    final List<DefaultConversionTask> tasks = taskArgument.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_1), new File(OUTPUT_DIR_TARGET_FILE_1));
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(new File(SOURCE_FILE_2), new File(OUTPUT_DIR_TARGET_FILE_2));
  }

  @Test
  public void convert_FilenamesToTargetWithoutOverwrite_NoTaskExecuted() throws Exception {

    FileUtils.touch(new File(OUTPUT_DIR_TARGET_FILE_1));
    FileUtils.touch(new File(OUTPUT_DIR_TARGET_FILE_2));

    converter.convert(
        new String[] {SOURCE_FILE_1, SOURCE_FILE_2}, TARGET_FORMAT, OUTPUT_DIR, false);

    verify(officeManager, times(0)).execute(isA(DefaultConversionTask.class));

    FileUtils.deleteQuietly(new File(OUTPUT_DIR_TARGET_FILE_1));
    FileUtils.deleteQuietly(new File(OUTPUT_DIR_TARGET_FILE_2));
  }

  @Before
  public void setUp() {

    officeManager = mock(OfficeManager.class);
    InstalledOfficeManagerHolder.setInstance(officeManager);
    registry = DefaultDocumentFormatRegistry.getInstance();

    converter = new CliConverter(registry);

    ExitException.INSTANCE.reset();
  }

  /** Setup the office manager before each test. */
  @Before
  public void tearDown() {

    officeManager = mock(OfficeManager.class);
    InstalledOfficeManagerHolder.setInstance(officeManager);
    registry = DefaultDocumentFormatRegistry.getInstance();

    converter = new CliConverter(registry);
  }
}
