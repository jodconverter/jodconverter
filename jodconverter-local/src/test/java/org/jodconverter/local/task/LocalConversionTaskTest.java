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

package org.jodconverter.local.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.task.ErrorCodeIOException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.job.AbstractTargetDocumentSpecs;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.utils.UnoRuntime;

/** Contains tests for the {@link LocalConversionTask} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class LocalConversionTaskTest {

  private static final File SOURCE_FILE = documentFile("test.txt");
  private static final String TARGET_FILENAME = "test.pdf";
  private static final String ZIP_TARGET_FILENAME = "test.zip";

  @Nested
  class StoreDocument {

    @Test
    void withUnsupportedFormat_ShouldThrowIllegalArgumentException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) {

      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final XComponent document = mock(XComponent.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);

      final File targetFile = new File(testFolder, ZIP_TARGET_FILENAME);
      final LocalConversionTask task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE),
              new FooTargetSpecsWithoutFilterFormat(targetFile),
              null,
              null,
              null);

      assertThatIllegalArgumentException()
          .isThrownBy(() -> task.storeDocument(document, targetFile));
    }

    @Test
    void whenErrorCodeIOExceptionCatched_ShouldThrowOfficeException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) throws Exception {

      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final XStorable storable = mock(XStorable.class);
      doThrow(ErrorCodeIOException.class)
          .when(storable)
          .storeToURL(isA(String.class), isA(PropertyValue[].class));

      final XComponent document = mock(XComponent.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);

      final File targetFile = new File(testFolder, TARGET_FILENAME);
      final LocalConversionTask task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.storeDocument(document, targetFile))
          .withCauseExactlyInstanceOf(ErrorCodeIOException.class);
    }

    @Test
    void whenIOExceptionCatched_ShouldThrowOfficeException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) throws Exception {

      final XServiceInfo serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final XStorable storable = mock(XStorable.class);
      doThrow(IOException.class)
          .when(storable)
          .storeToURL(isA(String.class), isA(PropertyValue[].class));

      final XComponent document = mock(XComponent.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);

      final File targetFile = new File(testFolder, TARGET_FILENAME);
      final LocalConversionTask task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.storeDocument(document, targetFile))
          .withCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Nested
  class Execute {

    @Test
    void whenIOExceptionCatched_ShouldThrowOfficeException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) throws Exception {

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
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
      given(unoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

      final File targetFile = new File(testFolder, TARGET_FILENAME);
      final LocalConversionTask task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.execute(context))
          .withCauseExactlyInstanceOf(IOException.class);
    }

    @Test
    void whenRuntimeExceptionCatched_ShouldThrowOfficeException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) throws Exception {

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
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
      given(unoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

      final File targetFile = new File(testFolder, TARGET_FILENAME);
      final LocalConversionTask task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE), new FooTargetSpecs(targetFile), null, null, null);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.execute(context))
          .withCauseExactlyInstanceOf(RuntimeException.class);
    }
  }

  @Nested
  class ToString {

    @Test
    void shouldReturnExpectedValue(final @TempDir File testFolder) {

      final FooSourceSpecs sourceSpecs = new FooSourceSpecs(SOURCE_FILE);
      final Map<String, Object> loadProps = new HashMap<>();
      loadProps.put("Key1", "Val1");
      final File targetFile = new File(testFolder, TARGET_FILENAME);
      final FooTargetSpecs targetSpecs = new FooTargetSpecs(targetFile);
      final Map<String, Object> storeProps = new HashMap<>();
      storeProps.put("Key2", "Val2");

      final LocalConversionTask task =
          new LocalConversionTask(sourceSpecs, targetSpecs, loadProps, null, storeProps);
      assertThat(task.toString())
          .isEqualTo(
              "LocalConversionTask{"
                  + "source="
                  + sourceSpecs
                  + ", loadProperties="
                  + loadProps
                  + ", target="
                  + targetSpecs
                  + ", storeProperties="
                  + storeProps
                  + '}');
    }
  }

  private static class FooSourceSpecs extends AbstractSourceDocumentSpecs {

    public FooSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.TXT;
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
  }

  private static class FooTargetSpecsWithoutFilterFormat extends FooTargetSpecs {

    public FooTargetSpecsWithoutFilterFormat(final File target) {
      super(target);
    }

    @Override
    public DocumentFormat getFormat() {
      final DocumentFormat fmt = DocumentFormat.copy(DefaultDocumentFormatRegistry.PDF);
      if (fmt.getStoreProperties() != null) {
        fmt.getStoreProperties().clear();
      }
      return fmt;
    }
  }
}
