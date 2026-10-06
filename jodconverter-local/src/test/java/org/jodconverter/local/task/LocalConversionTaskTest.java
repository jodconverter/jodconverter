/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

import static org.assertj.core.api.Assertions.*;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.HashMap;

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

      final var serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final var document = mock(XComponent.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);

      final var targetFile = new File(testFolder, ZIP_TARGET_FILENAME);
      final var task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE),
              new FooTargetSpecsWithoutFilterFormat(targetFile),
              false,
              null,
              null,
              null);

      assertThatIllegalArgumentException()
          .isThrownBy(() -> task.storeDocument(document, targetFile));
    }

    @Test
    void whenErrorCodeIOExceptionCatched_ShouldThrowOfficeException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) throws Exception {

      final var serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final var storable = mock(XStorable.class);
      doThrow(ErrorCodeIOException.class)
          .when(storable)
          .storeToURL(isA(String.class), isA(PropertyValue[].class));

      final var document = mock(XComponent.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);

      final var targetFile = new File(testFolder, TARGET_FILENAME);
      final var task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE),
              new FooTargetSpecs(targetFile),
              false,
              null,
              null,
              null);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.storeDocument(document, targetFile))
          .withCauseExactlyInstanceOf(ErrorCodeIOException.class);
    }

    @Test
    void whenIOExceptionCatched_ShouldThrowOfficeException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) throws Exception {

      final var serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final var storable = mock(XStorable.class);
      doThrow(IOException.class)
          .when(storable)
          .storeToURL(isA(String.class), isA(PropertyValue[].class));

      final var document = mock(XComponent.class);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);

      final var targetFile = new File(testFolder, TARGET_FILENAME);
      final var task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE),
              new FooTargetSpecs(targetFile),
              false,
              null,
              null,
              null);
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

      final var serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final var storable = mock(XStorable.class);
      doThrow(IOException.class)
          .when(storable)
          .storeToURL(isA(String.class), isA(PropertyValue[].class));

      final var document = mock(XComponent.class);
      final var loader = mock(XComponentLoader.class);
      final var context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willReturn(document);
      given(context.getComponentLoader()).willReturn(loader);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
      given(unoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

      final var targetFile = new File(testFolder, TARGET_FILENAME);
      final var task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE),
              new FooTargetSpecs(targetFile),
              false,
              null,
              null,
              null);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.execute(context))
          .withCauseExactlyInstanceOf(IOException.class);
    }

    @Test
    void whenRuntimeExceptionCatched_ShouldThrowOfficeException(
        final UnoRuntime unoRuntime, final @TempDir File testFolder) throws Exception {

      final var serviceInfo = mock(XServiceInfo.class);
      given(serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")).willReturn(true);

      final var storable = mock(XStorable.class);
      doThrow(RuntimeException.class)
          .when(storable)
          .storeToURL(isA(String.class), isA(PropertyValue[].class));

      final var document = mock(XComponent.class);
      final var loader = mock(XComponentLoader.class);
      final var context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willReturn(document);
      given(context.getComponentLoader()).willReturn(loader);
      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
      given(unoRuntime.queryInterface(XComponent.class, document)).willReturn(document);

      final var targetFile = new File(testFolder, TARGET_FILENAME);
      final var task =
          new LocalConversionTask(
              new FooSourceSpecs(SOURCE_FILE),
              new FooTargetSpecs(targetFile),
              false,
              null,
              null,
              null);
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.execute(context))
          .withCauseExactlyInstanceOf(RuntimeException.class);
    }
  }

  @Nested
  class ToString {

    @Test
    void shouldReturnExpectedValue(final @TempDir File testFolder) {

      final var sourceSpecs = new FooSourceSpecs(SOURCE_FILE);
      final var loadProps = new HashMap<String, Object>();
      loadProps.put("Key1", "Val1");
      final var targetFile = new File(testFolder, TARGET_FILENAME);
      final var targetSpecs = new FooTargetSpecs(targetFile);
      final var storeProps = new HashMap<String, Object>();
      storeProps.put("Key2", "Val2");

      final var task =
          new LocalConversionTask(sourceSpecs, targetSpecs, true, loadProps, storeProps, null);
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
                  + ", useStreamAdapters="
                  + true
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
      // The PDF format without its store properties.
      final var pdf = DefaultDocumentFormatRegistry.PDF;
      return DocumentFormat.builder()
          .name(pdf.getName())
          .extension(pdf.getExtension())
          .mediaType(pdf.getMediaType())
          .inputFamily(pdf.getInputFamily())
          .build();
    }
  }
}
