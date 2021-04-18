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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.UpdateDocMode;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.utils.UnoRuntime;

/** Contains tests for the {@link AbstractLocalOfficeTask} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class AbstractLocalOfficeTaskTest {

  private static final File SOURCE_FILE = documentFile("test.txt");

  @Nested
  class GetLoadProperties {

    @Test
    void withDefaultProperties_ShouldUseDefaultLoadProperties() {

      final FooOfficeTask task = new FooOfficeTask(new DocSourceSpecs(SOURCE_FILE));
      assertThat(task.getLoadProperties())
          .hasSize(4)
          .containsKey("InteractionHandler")
          .contains(
              entry("Hidden", true),
              entry("ReadOnly", true),
              entry("UpdateDocMode", UpdateDocMode.QUIET_UPDATE));
    }

    @Test
    void withCustomProperties_ShouldUseCustomLoadProperties() {

      final Map<String, Object> customProps = new HashMap<>();
      customProps.put("Key", "Val");
      final FooOfficeTask task = new FooOfficeTask(new DocSourceSpecs(SOURCE_FILE), customProps);

      assertThat(task.getLoadProperties()).hasSize(1).contains(entry("Key", "Val"));
    }

    @Test
    void withDefaultPropertiesAndNullSourceFormat_ShouldUseDefaultLoadProperties() {

      final FooOfficeTask task = new FooOfficeTask(new NullSourceSpecs(SOURCE_FILE));
      assertThat(task.getLoadProperties())
          .hasSize(4)
          .containsKey("InteractionHandler")
          .contains(
              entry("Hidden", true),
              entry("ReadOnly", true),
              entry("UpdateDocMode", UpdateDocMode.QUIET_UPDATE));
    }

    @Test
    void withCustomPropertiesAndNullSourceFormat_ShouldUseCustomLoadProperties() {

      final Map<String, Object> customProps = new HashMap<>();
      customProps.put("Key", "Val");
      final FooOfficeTask task = new FooOfficeTask(new NullSourceSpecs(SOURCE_FILE), customProps);

      assertThat(task.getLoadProperties()).hasSize(1).contains(entry("Key", "Val"));
    }

    @Test
    void withDefaultAndSourceProperties_ShouldUseDefaultAndSourceLoadProperties() {

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThat(task.getLoadProperties())
          .hasSize(6)
          .containsKey("InteractionHandler")
          .contains(
              entry("Hidden", true),
              entry("ReadOnly", true),
              entry("UpdateDocMode", UpdateDocMode.QUIET_UPDATE),
              entry("FilterName", "Text (encoded)"),
              entry("FilterOptions", "utf8"));
    }

    @Test
    void withCustomAndSourceProperties_ShouldUseCustomLoadProperties() {

      final Map<String, Object> customProps = new HashMap<>();
      customProps.put("Key", "Val");
      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE), customProps);

      assertThat(task.getLoadProperties())
          .hasSize(3)
          .contains(
              entry("Key", "Val"),
              entry("FilterName", "Text (encoded)"),
              entry("FilterOptions", "utf8"));
    }
  }

  @Nested
  class LoadDocument {

    @Test
    void whenIllegalArgumentExceptionCatched_ShouldThrowOfficeException()
        throws IOException, IllegalArgumentException {

      final XComponentLoader loader = mock(XComponentLoader.class);
      final LocalOfficeContext context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willThrow(IllegalArgumentException.class);
      given(context.getComponentLoader()).willReturn(loader);

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withCauseExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenErrorCodeIOExceptionCatched_ShouldThrowOfficeException()
        throws IOException, IllegalArgumentException {

      final XComponentLoader loader = mock(XComponentLoader.class);
      final LocalOfficeContext context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willThrow(ErrorCodeIOException.class);
      given(context.getComponentLoader()).willReturn(loader);

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withCauseExactlyInstanceOf(ErrorCodeIOException.class);
    }

    @Test
    void whenIOExceptionCatched_ShouldThrowOfficeException()
        throws IOException, IllegalArgumentException {

      final XComponentLoader loader = mock(XComponentLoader.class);
      final LocalOfficeContext context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willThrow(IOException.class);
      given(context.getComponentLoader()).willReturn(loader);

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Nested
  class CloseDocument {

    @Test
    void withNull_ShouldNotThrowAnyException() {

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatCode(() -> task.closeDocument(null)).doesNotThrowAnyException();
    }

    @Test
    void whenCloseableIsNull_ShouldCallComponentDispose(final UnoRuntime unoRuntime) {

      final XComponent document = mock(XComponent.class);
      final XComponent component = mock(XComponent.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(null);
      given(unoRuntime.queryInterface(XComponent.class, document)).willReturn(component);

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      task.closeDocument(document);
      verify(component, times(1)).dispose();
    }

    @Test
    void whenCloseableIsNotNull_ShouldCallCloseableClose(final UnoRuntime unoRuntime)
        throws CloseVetoException {

      final XComponent document = mock(XComponent.class);
      final XCloseable closeable = mock(XCloseable.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(closeable);

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      task.closeDocument(document);
      verify(closeable, times(1)).close(isA(Boolean.class));
    }

    @Test
    void whenCloseVetoExceptionCatched_ShouldNotThrowAnyException(final UnoRuntime unoRuntime)
        throws CloseVetoException {

      final XComponent document = mock(XComponent.class);
      final XCloseable closeable = mock(XCloseable.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(closeable);
      willThrow(CloseVetoException.class).given(closeable).close(isA(Boolean.class));

      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatCode(() -> task.closeDocument(document)).doesNotThrowAnyException();
      verify(closeable, times(1)).close(isA(Boolean.class));
    }
  }

  @Nested
  class ToString {

    @Test
    void shouldReturnExpectedValue() {

      final TxtSourceSpecs sourceSpecs = new TxtSourceSpecs(SOURCE_FILE);
      final Map<String, Object> customProps = new HashMap<>();
      customProps.put("Key", "Val");
      final FooOfficeTask task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE), customProps);
      assertThat(task.toString())
          .isEqualTo(
              "FooOfficeTask{" + "source=" + sourceSpecs + ", loadProperties=" + customProps + '}');
    }
  }

  private static class FooOfficeTask extends AbstractLocalOfficeTask {

    public FooOfficeTask(final SourceDocumentSpecs source) {
      super(source);
    }

    public FooOfficeTask(
        final SourceDocumentSpecs source, final Map<String, Object> loadProperties) {
      super(source, loadProperties);
    }

    @Override
    public void execute(@SuppressWarnings("NullableProblems") final OfficeContext context) {
      // Do nothing here
    }
  }

  private static class DocSourceSpecs extends AbstractSourceDocumentSpecs {

    public DocSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.DOC;
    }
  }

  private static class TxtSourceSpecs extends AbstractSourceDocumentSpecs {

    public TxtSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.TXT;
    }
  }

  private static class NullSourceSpecs extends AbstractSourceDocumentSpecs {

    public NullSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return null;
    }
  }
}
