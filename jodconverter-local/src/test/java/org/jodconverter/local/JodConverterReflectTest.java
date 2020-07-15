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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests that use reflection for the {@link JodConverter} class. */
@RunWith(PowerMockRunner.class)
@PrepareForTest(LocalConverter.class)
public class JodConverterReflectTest {

  private static final File SOURCE_FILE = documentFile("test.txt");

  private LocalConverter localConverter;

  /** Setup the office manager before each test. */
  @Before
  public void setUp() {

    mockStatic(LocalConverter.class);
    localConverter = mock(LocalConverter.class);
    given(LocalConverter.make()).willReturn(localConverter);
  }

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(JodConverter.class);
  }

  @Test
  public void convert_FromFile_CallForwardToLocalConverter() {

    JodConverter.convert(SOURCE_FILE);

    final ArgumentCaptor<File> arg = ArgumentCaptor.forClass(File.class);
    verify(localConverter, times(1)).convert(arg.capture());
    final File file = arg.getValue();
    assertThat(file).isEqualTo(SOURCE_FILE);
  }

  @Test
  public void convert_FromStream_CallForwardToLocalConverter() throws IOException {

    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {

      JodConverter.convert(stream);

      final ArgumentCaptor<InputStream> arg = ArgumentCaptor.forClass(InputStream.class);
      verify(localConverter, times(1)).convert(arg.capture());
      assertThat(arg.getValue()).isEqualTo(stream);
    }
  }

  @Test
  public void convert_FromStreamWithCloseArgument_CallForwardToLocalConverter() throws IOException {

    try (InputStream stream = Files.newInputStream(SOURCE_FILE.toPath())) {

      JodConverter.convert(stream, false);

      final ArgumentCaptor<InputStream> arg = ArgumentCaptor.forClass(InputStream.class);
      final ArgumentCaptor<Boolean> boolArg = ArgumentCaptor.forClass(Boolean.class);
      verify(localConverter, times(1)).convert(arg.capture(), boolArg.capture());
      assertThat(arg.getValue()).isEqualTo(stream);
    }
  }
}
