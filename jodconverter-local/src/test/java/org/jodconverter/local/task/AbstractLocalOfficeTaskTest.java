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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.entry;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.UpdateDocMode;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.io.IOException;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.DocumentPasswordRequest;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.task.XInteractionHandler;
import com.sun.star.task.XInteractionRequest;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.PasswordProtectedException;
import org.jodconverter.local.office.utils.UnoRuntime;

/** Contains tests for the {@link AbstractLocalOfficeTask} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class AbstractLocalOfficeTaskTest {

  private static final File SOURCE_FILE = documentFile("test.txt");

  @Nested
  class GetLoadProperties {

    @Test
    void withDefaultProperties_ShouldUseDefaultLoadProperties() {

      final var task = new FooOfficeTask(new DocSourceSpecs(SOURCE_FILE));
      assertThat(task.getLoadProperties())
          .hasSize(4)
          .containsKey("InteractionHandler")
          .contains(
              entry("Hidden", true),
              entry("ReadOnly", true),
              entry("UpdateDocMode", UpdateDocMode.NO_UPDATE));
    }

    @Test
    void withCustomProperties_ShouldUseCustomLoadProperties() {

      final var customProps = new HashMap<String, Object>();
      customProps.put("Key", "Val");
      final var task = new FooOfficeTask(new DocSourceSpecs(SOURCE_FILE), false, customProps);

      assertThat(task.getLoadProperties()).contains(entry("Key", "Val"));
    }

    @Test
    void withDefaultPropertiesAndNullSourceFormat_ShouldUseDefaultLoadProperties() {

      final var task = new FooOfficeTask(new NullSourceSpecs(SOURCE_FILE));
      assertThat(task.getLoadProperties())
          .hasSize(4)
          .containsKey("InteractionHandler")
          .contains(
              entry("Hidden", true),
              entry("ReadOnly", true),
              entry("UpdateDocMode", UpdateDocMode.NO_UPDATE));
    }

    @Test
    void withCustomPropertiesAndNullSourceFormat_ShouldUseCustomLoadProperties() {

      final var customProps = new HashMap<String, Object>();
      customProps.put("Key", "Val");
      final var task = new FooOfficeTask(new NullSourceSpecs(SOURCE_FILE), false, customProps);

      assertThat(task.getLoadProperties()).contains(entry("Key", "Val"));
    }

    @Test
    void withDefaultAndSourceProperties_ShouldUseDefaultAndSourceLoadProperties() {

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThat(task.getLoadProperties())
          .hasSize(6)
          .containsKey("InteractionHandler")
          .contains(
              entry("Hidden", true),
              entry("ReadOnly", true),
              entry("UpdateDocMode", UpdateDocMode.NO_UPDATE),
              entry("FilterName", "Text (encoded)"),
              entry("FilterOptions", "utf8"));
    }

    @Test
    void withCustomAndSourceProperties_ShouldUseCustomLoadProperties() {

      final var customProps = new HashMap<String, Object>();
      customProps.put("Key", "Val");
      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE), false, customProps);

      assertThat(task.getLoadProperties())
          .contains(
              entry("Key", "Val"),
              entry("FilterName", "Text (encoded)"),
              entry("FilterOptions", "utf8"));
    }
  }

  @Nested
  class LoadDocument {

    @Test
    void whenUnoExceptionExceptionCatched_ShouldThrowOfficeException()
        throws com.sun.star.uno.Exception {

      final var loader = mock(XComponentLoader.class);
      final var context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willThrow(com.sun.star.lang.IllegalArgumentException.class);
      given(context.getComponentLoader()).willReturn(loader);

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));

      // This exception is a checked one in the office libraries of OpenOffice, and a runtime
      // exception in those of LibreOffice, which is then thrown as it is.
      if (com.sun.star.uno.RuntimeException.class.isAssignableFrom(
          com.sun.star.lang.IllegalArgumentException.class)) {
        assertThatExceptionOfType(com.sun.star.lang.IllegalArgumentException.class)
            .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE));
      } else {
        assertThatExceptionOfType(OfficeException.class)
            .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
            .withCauseExactlyInstanceOf(com.sun.star.lang.IllegalArgumentException.class);
      }
    }

    @Test
    void whenErrorCodeIOExceptionCatched_ShouldThrowOfficeException()
        throws com.sun.star.uno.Exception {

      final var loader = mock(XComponentLoader.class);
      final var context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willThrow(ErrorCodeIOException.class);
      given(context.getComponentLoader()).willReturn(loader);

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withCauseExactlyInstanceOf(ErrorCodeIOException.class);
    }

    @Test
    void whenIOExceptionCatched_ShouldThrowOfficeException() throws com.sun.star.uno.Exception {

      final var loader = mock(XComponentLoader.class);
      final var context = mock(LocalOfficeContext.class);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willThrow(IOException.class);
      given(context.getComponentLoader()).willReturn(loader);

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Nested
  class CloseDocument {

    @Test
    void withNull_ShouldNotThrowAnyException() {

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatCode(() -> task.closeDocument(null)).doesNotThrowAnyException();
    }

    @Test
    void whenCloseableIsNull_ShouldCallComponentDispose(final UnoRuntime unoRuntime) {

      final var document = mock(XComponent.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(null);

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      task.closeDocument(document);

      // The document is the component: it is disposed once.
      verify(document, times(1)).dispose();
    }

    @Test
    void whenCloseableIsNotNull_ShouldCallCloseableClose(final UnoRuntime unoRuntime)
        throws CloseVetoException {

      final var document = mock(XComponent.class);
      final var closeable = mock(XCloseable.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(closeable);

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      task.closeDocument(document);
      verify(closeable, times(1)).close(isA(Boolean.class));
    }

    @Test
    void whenCloseVetoExceptionCatched_ShouldNotThrowAnyException(final UnoRuntime unoRuntime)
        throws CloseVetoException {

      final var document = mock(XComponent.class);
      final var closeable = mock(XCloseable.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(closeable);
      willThrow(CloseVetoException.class).given(closeable).close(isA(Boolean.class));

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatCode(() -> task.closeDocument(document)).doesNotThrowAnyException();
      verify(closeable, times(1)).close(isA(Boolean.class));
    }
  }

  @Nested
  class CloseLostDocument {

    @Test
    void whenTheOfficeIsLostWhileClosing_ShouldNotThrowAnyException(final UnoRuntime unoRuntime)
        throws CloseVetoException {

      final var document = mock(XComponent.class);
      final var closeable = mock(XCloseable.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(closeable);
      willThrow(DisposedException.class).given(closeable).close(isA(Boolean.class));

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatCode(() -> task.closeDocument(document)).doesNotThrowAnyException();
    }

    @Test
    void whenTheCallIsInterruptedWhileClosing_ShouldNotThrowAnyException(
        final UnoRuntime unoRuntime) throws CloseVetoException {

      // What the UNO bridge throws when the worker interrupts the thread waiting for the answer.
      final var document = mock(XComponent.class);
      final var closeable = mock(XCloseable.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(closeable);
      willThrow(new com.sun.star.uno.RuntimeException("removeJob - unexpected"))
          .given(closeable)
          .close(isA(Boolean.class));

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatCode(() -> task.closeDocument(document)).doesNotThrowAnyException();
    }

    @Test
    void whenAnotherRuntimeExceptionIsThrownWhileClosing_ShouldThrowIt(final UnoRuntime unoRuntime)
        throws CloseVetoException {

      final var document = mock(XComponent.class);
      final var closeable = mock(XCloseable.class);
      given(unoRuntime.queryInterface(XCloseable.class, document)).willReturn(closeable);
      willThrow(new IllegalStateException("Not from the office"))
          .given(closeable)
          .close(isA(Boolean.class));

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatIllegalStateException().isThrownBy(() -> task.closeDocument(document));
    }

    @Test
    void whenTheOfficeIsLostWhileDisposing_ShouldNotThrowAnyException() {

      final var document = mock(XComponent.class);
      willThrow(DisposedException.class).given(document).dispose();

      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE));
      assertThatCode(() -> task.closeDocument(document)).doesNotThrowAnyException();
    }
  }

  @Nested
  class ToString {

    @Test
    void withPassword_ShouldHideIt() {

      final var customProps = new HashMap<String, Object>();
      customProps.put("Key", "Val");
      customProps.put("Password", "secret");
      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE), true, customProps);

      assertThat(task.toString())
          .doesNotContain("secret")
          .contains("Password=***")
          .contains("Key=Val");
    }

    @Test
    void shouldReturnExpectedValue() {

      final var sourceSpecs = new TxtSourceSpecs(SOURCE_FILE);
      final var customProps = new HashMap<String, Object>();
      customProps.put("Key", "Val");
      final var task = new FooOfficeTask(new TxtSourceSpecs(SOURCE_FILE), true, customProps);
      assertThat(task.toString())
          .isEqualTo(
              "FooOfficeTask{"
                  + "source="
                  + sourceSpecs
                  + ", loadProperties="
                  + customProps
                  + ", useStreamAdapters="
                  + true
                  + '}');
    }
  }

  @Nested
  class LoadDocumentWithPassword {

    private final XComponentLoader loader = mock(XComponentLoader.class);
    private final LocalOfficeContext context = mock(LocalOfficeContext.class);

    private static Object property(final Object[] arguments, final String name) {
      return Arrays.stream((PropertyValue[]) arguments[3])
          .filter(property -> name.equals(property.Name))
          .map(property -> property.Value)
          .findFirst()
          .orElse(null);
    }

    // An office that loads nothing with the password given, and that does the given thing when
    // the file is loaded again without the password.
    private void officeWillRefuseThePassword(final Answer<XComponent> withoutPassword)
        throws com.sun.star.uno.Exception {

      given(context.getComponentLoader()).willReturn(loader);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willAnswer(
              invocation ->
                  property(invocation.getArguments(), "Password") == null
                      ? withoutPassword.answer(invocation)
                      : null);
    }

    // What the office does with a protected document and no password: it asks for one.
    private static XComponent askForThePassword(final InvocationOnMock invocation) {

      final var request = mock(XInteractionRequest.class);
      given(request.getRequest()).willReturn(new DocumentPasswordRequest());
      ((XInteractionHandler) property(invocation.getArguments(), "InteractionHandler"))
          .handle(request);
      return null;
    }

    private FooOfficeTask taskWithPassword(final boolean useStreamAdapters) {
      final var properties = new HashMap<String, Object>();
      properties.put("Password", "wrong");
      return new FooOfficeTask(new DocSourceSpecs(SOURCE_FILE), useStreamAdapters, properties);
    }

    @Test
    void whenNothingIsLoadedAndTheDocumentIsProtected_ShouldThrowWrongPassword()
        throws com.sun.star.uno.Exception {

      officeWillRefuseThePassword(LoadDocumentWithPassword::askForThePassword);
      final var task = taskWithPassword(false);

      assertThatExceptionOfType(PasswordProtectedException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withMessage("Wrong password for " + SOURCE_FILE.getName())
          .withCauseInstanceOf(DocumentPasswordRequest.class);
      assertThat(task.hasPasswordInteractionRequest()).isTrue();

      // Once with the password, once without.
      verify(loader, times(2))
          .loadComponentFromURL(
              isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class));
    }

    @Test
    void whenLoadedThroughAStream_ShouldThrowWrongPasswordToo() throws com.sun.star.uno.Exception {

      officeWillRefuseThePassword(LoadDocumentWithPassword::askForThePassword);
      final var task = taskWithPassword(true);

      assertThatExceptionOfType(PasswordProtectedException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withMessage("Wrong password for " + SOURCE_FILE.getName());
    }

    @Test
    void whenNothingIsLoadedAndTheDocumentIsNotProtected_ShouldNotBlameThePassword()
        throws com.sun.star.uno.Exception {

      // A file the office cannot read, given with a password it does not need.
      officeWillRefuseThePassword(invocation -> null);
      final var task = taskWithPassword(false);

      assertThatNullPointerException()
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withMessage("Could not open document: " + SOURCE_FILE.getName());
      assertThat(task.hasPasswordInteractionRequest()).isFalse();
    }

    @Test
    void whenTheSecondLoadFails_ShouldNotBlameThePassword() throws com.sun.star.uno.Exception {

      officeWillRefuseThePassword(
          invocation -> {
            throw new com.sun.star.lang.IllegalArgumentException("no");
          });
      final var task = taskWithPassword(false);

      assertThatNullPointerException()
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withMessage("Could not open document: " + SOURCE_FILE.getName());
    }

    @Test
    void whenTheSecondLoadGivesADocument_ShouldCloseItAndNotBlameThePassword()
        throws com.sun.star.uno.Exception {

      final var document = mock(XComponent.class);
      officeWillRefuseThePassword(invocation -> document);
      final var task = taskWithPassword(false);

      assertThatNullPointerException().isThrownBy(() -> task.loadDocument(context, SOURCE_FILE));
      verify(document).dispose();
    }

    @Test
    void whenTheOfficeAsksAgainThenAbortsWithARuntimeException_ShouldThrowWrongPassword()
        throws com.sun.star.uno.Exception {

      // What LibreOffice does with a wrong password on a Microsoft document: it asks for the
      // password, then aborts the type detection, with an exception that is a runtime exception
      // in the office libraries of LibreOffice.
      given(context.getComponentLoader()).willReturn(loader);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willAnswer(
              invocation -> {
                askForThePassword(invocation);
                throw new com.sun.star.uno.RuntimeException("type detection aborted");
              });
      final var task = taskWithPassword(false);

      assertThatExceptionOfType(PasswordProtectedException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withMessage("Wrong password for " + SOURCE_FILE.getName());
    }

    @Test
    void whenARuntimeExceptionIsThrownWithoutPasswordRequest_ShouldThrowIt()
        throws com.sun.star.uno.Exception {

      given(context.getComponentLoader()).willReturn(loader);
      given(
              loader.loadComponentFromURL(
                  isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
          .willThrow(new com.sun.star.uno.RuntimeException("other"));
      final var task = new FooOfficeTask(new DocSourceSpecs(SOURCE_FILE));

      assertThatExceptionOfType(com.sun.star.uno.RuntimeException.class)
          .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
          .withMessage("other");
    }

    @Test
    void withoutPassword_ShouldLoadOnce() throws com.sun.star.uno.Exception {

      given(context.getComponentLoader()).willReturn(loader);
      final var task = new FooOfficeTask(new DocSourceSpecs(SOURCE_FILE));

      assertThatNullPointerException().isThrownBy(() -> task.loadDocument(context, SOURCE_FILE));
      verify(loader, times(1))
          .loadComponentFromURL(
              isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class));
    }
  }

  private static class FooOfficeTask extends AbstractLocalOfficeTask {

    FooOfficeTask(final SourceDocumentSpecs source) {
      super(source);
    }

    FooOfficeTask(
        final SourceDocumentSpecs source,
        final boolean useStreamAdapters,
        final Map<String, Object> loadProperties) {
      super(source, useStreamAdapters, loadProperties);
    }

    @Override
    public void execute(@SuppressWarnings("NullableProblems") final OfficeContext context) {
      // Do nothing here
    }
  }

  private static class DocSourceSpecs extends AbstractSourceDocumentSpecs {

    DocSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.DOC;
    }
  }

  private static class TxtSourceSpecs extends AbstractSourceDocumentSpecs {

    TxtSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.TXT;
    }
  }

  private static class NullSourceSpecs extends AbstractSourceDocumentSpecs {

    NullSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return null;
    }
  }
}
