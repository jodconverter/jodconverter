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

package org.jodconverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.jodconverter.test.util.AssertUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocalConverter.class)
public class JodConverterTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private LocalConverter localConverter;

  /** Setup the office manager before each test. */
  @Before
  public void setUp() {

    mockStatic(LocalConverter.class);
    localConverter = mock(LocalConverter.class);
    given(LocalConverter.make()).willReturn(localConverter);
  }

  @Test
  public void ctor_ClassWellDefined() throws Exception {

    AssertUtil.assertUtilityClassWellDefined(JodConverter.class);
  }

  @Test
  public void convert_FromFile_CallForwardToLocalConverter() throws Exception {

    JodConverter.convert(SOURCE_FILE);

    final ArgumentCaptor<File> argument = ArgumentCaptor.forClass(File.class);
    verify(localConverter, times(1)).convert(argument.capture());
    final File file = argument.getValue();
    assertThat(file).isEqualTo(SOURCE_FILE);
  }

  @Test
  public void convert_FromStream_CallForwardToLocalConverter() throws Exception {

    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {

      JodConverter.convert(stream);

      final ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
      verify(localConverter, times(1)).convert(argument.capture());
      assertThat(argument.getValue()).isEqualTo(stream);
    }
  }

  @Test
  public void convert_FromStreamWithCloseArgument_CallForwardToLocalConverter() throws Exception {

    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {

      JodConverter.convert(stream, false);

      final ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
      final ArgumentCaptor<Boolean> booleanArgument = ArgumentCaptor.forClass(Boolean.class);
      verify(localConverter, times(1)).convert(argument.capture(), booleanArgument.capture());
      assertThat(argument.getValue()).isEqualTo(stream);
    }
  }
}
