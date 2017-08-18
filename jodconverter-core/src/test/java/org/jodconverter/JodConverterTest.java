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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.jodconverter.test.util.AssertUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DefaultConverter.class)
@SuppressWarnings("PMD.LawOfDemeter")
public class JodConverterTest {

  private static final String TEST_OUTPUT_DIR = "build/test-results/";
  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private static File outputDir;

  private DefaultConverter defaultConverter;

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, JodConverterTest.class.getSimpleName());
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

    mockStatic(DefaultConverter.class);
    defaultConverter = mock(DefaultConverter.class);
    given(DefaultConverter.make()).willReturn(defaultConverter);
  }

  @Test
  public void ctor_ClassWellDefined() throws Exception {

    AssertUtil.assertUtilityClassWellDefined(JodConverter.class);
  }

  @Test
  public void convert_FromFile_CallForwardToDefaultConverter() throws Exception {

    JodConverter.convert(SOURCE_FILE);

    final ArgumentCaptor<File> fileArgument = ArgumentCaptor.forClass(File.class);
    verify(defaultConverter, times(1)).convert(fileArgument.capture());
    final File file = fileArgument.getValue();
    assertThat(file).isEqualTo(SOURCE_FILE);
  }

  @Test
  public void convert_FromStream_CallForwardToDefaultConverter() throws Exception {

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {

      JodConverter.convert(inputStream);

      final ArgumentCaptor<InputStream> inputStreamArgument =
          ArgumentCaptor.forClass(InputStream.class);
      verify(defaultConverter, times(1)).convert(inputStreamArgument.capture());
      final InputStream stream = inputStreamArgument.getValue();
      assertThat(stream).isEqualTo(inputStream);
    }
  }

  @Test
  public void convert_FromStreamWithCloseArgument_CallForwardToDefaultConverter() throws Exception {

    try (final FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {

      JodConverter.convert(inputStream, false);

      final ArgumentCaptor<InputStream> inputStreamArgument =
          ArgumentCaptor.forClass(InputStream.class);
      final ArgumentCaptor<Boolean> booleanArgument = ArgumentCaptor.forClass(Boolean.class);
      verify(defaultConverter, times(1))
          .convert(inputStreamArgument.capture(), booleanArgument.capture());
      final InputStream stream = inputStreamArgument.getValue();
      assertThat(stream).isEqualTo(inputStream);
    }
  }
}
