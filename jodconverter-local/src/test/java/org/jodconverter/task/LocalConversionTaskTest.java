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

package org.jodconverter.task;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
import org.jodconverter.office.LocalOfficeContext;
import org.jodconverter.office.OfficeException;

/**
 * Contains tests for the {@link LocalConversionTask} class.
 *
 * @see LocalConversionTask
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(UnoRuntime.class)
public class LocalConversionTaskTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");
  private static final String TARGET_FILENAME = "test.pdf";
  private static final String ZIP_TARGET_FILENAME = "test.zip";

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  private static class FooSourceSpecs extends AbstractSourceDocumentSpecs {

    public FooSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.TXT;
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
    public void onComplete(final File file) {
      // Do nothing here
    }

    @Override
    public void onFailure(final File file, final Exception exception) {
      // Do nothing here
    }
  }

  private static class FooTargetSpecsWithoutFilterFormat extends FooTargetSpecs {

    public FooTargetSpecsWithoutFilterFormat(final File target) {
      super(target);
    }

    @Override
    public DocumentFormat getFormat() {
      final DocumentFormat fmt = DocumentFormat.copy(DefaultDocumentFormatRegistry.PDF);
      fmt.getStoreProperties().clear();
      return fmt;
    }
  }

  /**
   * Creates the test folder.
   *
   * @throws java.io.IOException If an IO error occurs.
   */
  @BeforeClass
  public static void setUpClass() throws java.io.IOException {

    // PowerMock reloads a test class with custom class loader
    // it is done after jUnit applies @ClassRule, so we have to
    // do this.
    // See https://github.com/powermock/powermock/issues/687
    testFolder.create();
  }

  /** Deletes the test folder. */
  @AfterClass
  public static void tearDownClass() {

    testFolder.delete();
  }

  @Test(expected = IllegalArgumentException.class)
  public void storeDocument_WithUnsupportedFormat_ThrowIllegalArgumentException() throws Exception {

    final XServiceInfo serviceInfo = mock(XServiceInfo.class);
    given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

    final XComponent document = mock(XComponent.class);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);

    final File targetFile = new File(testFolder.getRoot(), ZIP_TARGET_FILENAME);
    final LocalConversionTask task =
        new LocalConversionTask(
            new FooSourceSpecs(SOURCE_FILE),
            new FooTargetSpecsWithoutFilterFormat(targetFile),
            null,
            null,
            null);
    task.storeDocument(document, targetFile);
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

    final File targetFile = new File(testFolder.getRoot(), TARGET_FILENAME);
    final LocalConversionTask task =
        new LocalConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task.storeDocument(document, targetFile))
        .withCauseExactlyInstanceOf(ErrorCodeIOException.class);
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

    final File targetFile = new File(testFolder.getRoot(), TARGET_FILENAME);
    final LocalConversionTask task =
        new LocalConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task.storeDocument(document, targetFile))
        .withCauseExactlyInstanceOf(IOException.class);
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
    final LocalOfficeContext context = mock(LocalOfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willReturn(document);
    given(context.getComponentLoader()).willReturn(loader);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(UnoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
    given(UnoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

    final File targetFile = new File(testFolder.getRoot(), TARGET_FILENAME);
    final LocalConversionTask task =
        new LocalConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task.execute(context))
        .withCauseExactlyInstanceOf(IOException.class);
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
    final LocalOfficeContext context = mock(LocalOfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willReturn(document);
    given(context.getComponentLoader()).willReturn(loader);
    mockStatic(UnoRuntime.class);
    given(UnoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(UnoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
    given(UnoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

    final File targetFile = new File(testFolder.getRoot(), TARGET_FILENAME);
    final LocalConversionTask task =
        new LocalConversionTask(
            new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task.execute(context))
        .withCauseExactlyInstanceOf(RuntimeException.class);
  }
}
