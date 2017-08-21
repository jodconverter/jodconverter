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

package org.jodconverter.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.job.AbstractSourceDocumentSpecs;
import org.jodconverter.job.AbstractTargetDocumentSpecs;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/**
 * Contains tests for the {@link DefaultConversionTask} class.
 *
 * @see DefaultConversionTask
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(UnoRuntime.class)
public class DefaultConversionTaskTest {

  private static final String TEST_OUTPUT_DIR = "build/test-results/";
  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");
  private static final String TARGET_FILENAME = "test.pdf";

  private static File outputDir;

  private static class FooSourceSpecs extends AbstractSourceDocumentSpecs {

    public FooSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.TXT;
    }

    @Override
    public Map<String, Object> getCustomLoadProperties() {
      return new HashMap<>();
    }

    @Override
    public void onConsumed(final File file) {
      // Do nothing here
    }
  }

  private static class FooTargetSpecs extends AbstractTargetDocumentSpecs {

    public FooTargetSpecs(final File target) {
      super(target);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.PDF;
    }

    @Override
    public Map<String, Object> getCustomStoreProperties() {
      return new HashMap<>();
    }

    @Override
    public void onComplete(final File file) {
      // Do nothing here
    }

    @Override
    public void onFailure(final File file, final Exception exception) {
      // Do nothing here
    }
  }

  /** Creates an output test directory just once. */
  @BeforeClass
  public static void setUpClass() {

    outputDir = new File(TEST_OUTPUT_DIR, DefaultConversionTaskTest.class.getSimpleName());
    outputDir.mkdirs();
  }

  /** Deletes the output test directory once the tests are all done. */
  @AfterClass
  public static void tearDownClass() {

    FileUtils.deleteQuietly(outputDir);
  }

  @Test
  public void storeDocument_CatchErrorCodeIoException_ThrowOfficeException() throws Exception {

    final XServiceInfo serviceInfo = mock(XServiceInfo.class);
    given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

    final XStorable storable = mock(XStorable.class);
    doThrow(ErrorCodeIOException.class)
        .when(storable)
        .storeToURL(isA(String.class), isA(PropertyValue[].class));

    final XComponent document = mock(XComponent.class);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(UnoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    final DefaultConversionTask task =
        new DefaultConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null);
    try {
      task.storeDocument(document, targetFile);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(ErrorCodeIOException.class);
    }
  }

  @Test
  public void storeDocument_CatchIoException_ThrowOfficeException() throws Exception {

    final XServiceInfo serviceInfo = mock(XServiceInfo.class);
    given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

    final XStorable storable = mock(XStorable.class);
    doThrow(IOException.class)
        .when(storable)
        .storeToURL(isA(String.class), isA(PropertyValue[].class));

    final XComponent document = mock(XComponent.class);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(UnoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    final DefaultConversionTask task =
        new DefaultConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null);
    try {
      task.storeDocument(document, targetFile);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Test
  public void execute_CatchIoException_ThrowOfficeException() throws Exception {

    final XServiceInfo serviceInfo = mock(XServiceInfo.class);
    given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

    final XStorable storable = mock(XStorable.class);
    doThrow(IOException.class)
        .when(storable)
        .storeToURL(isA(String.class), isA(PropertyValue[].class));

    final XComponent document = mock(XComponent.class);
    final XComponentLoader loader = mock(XComponentLoader.class);
    final OfficeContext context = mock(OfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willReturn(document);
    given(context.getComponentLoader()).willReturn(loader);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(UnoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
    given(UnoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    final DefaultConversionTask task =
        new DefaultConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null);

    try {
      task.execute(context);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Test
  public void execute_CatchRuntimeException_ThrowOfficeException() throws Exception {

    final XServiceInfo serviceInfo = mock(XServiceInfo.class);
    given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

    final XStorable storable = mock(XStorable.class);
    doThrow(RuntimeException.class)
        .when(storable)
        .storeToURL(isA(String.class), isA(PropertyValue[].class));

    final XComponent document = mock(XComponent.class);
    final XComponentLoader loader = mock(XComponentLoader.class);
    final OfficeContext context = mock(OfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willReturn(document);
    given(context.getComponentLoader()).willReturn(loader);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(UnoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
    given(UnoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

    final File targetFile = new File(outputDir, TARGET_FILENAME);
    final DefaultConversionTask task =
        new DefaultConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null);

    try {
      task.execute(context);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(RuntimeException.class);
    }
  }
}
