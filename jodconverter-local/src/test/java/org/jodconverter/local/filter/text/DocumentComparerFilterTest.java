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

package org.jodconverter.local.filter.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.DispatchResultEvent;
import com.sun.star.frame.DispatchResultState;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.XComponentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.filter.DefaultFilterChain;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.UnoRuntime;

/** Contains tests for the {@link DocumentComparerFilter} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class DocumentComparerFilterTest {

  private static final String COMMAND = ".uno:CompareDocuments";

  private final LocalOfficeContext context = mock(LocalOfficeContext.class);
  private final XComponent document = mock(XComponent.class);
  private final XServiceInfo serviceInfo = mock(XServiceInfo.class);
  private final XStorable storable = mock(XStorable.class);
  private final XModel model = mock(XModel.class);
  private final XController controller = mock(XController.class);
  private final XDispatchHelper helper = mock(XDispatchHelper.class);
  private final XDispatchProvider provider = mock(XDispatchProvider.class);
  private final Filter next = mock(Filter.class);

  private File original;

  @BeforeEach
  void createOriginal(final @TempDir File dir) throws IOException {
    original = new File(dir, "original.odt");
    Files.writeString(original.toPath(), "original");
  }

  // An editable text document with a view, in an office that has the dispatch helper.
  private void setUp(final UnoRuntime unoRuntime) throws Exception {

    given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(serviceInfo.supportsService(Lo.WRITER_SERVICE)).willReturn(true);
    given(unoRuntime.queryInterface(XStorable.class, document)).willReturn(storable);
    given(unoRuntime.queryInterface(XModel.class, document)).willReturn(model);
    given(model.getCurrentController()).willReturn(controller);
    final var frame = mock(XFrame.class);
    given(controller.getFrame()).willReturn(frame);
    given(unoRuntime.queryInterface(XDispatchProvider.class, frame)).willReturn(provider);

    final var componentContext = mock(XComponentContext.class);
    final var serviceManager = mock(XMultiComponentFactory.class);
    final var helperService = new Object();
    given(context.getComponentContext()).willReturn(componentContext);
    given(componentContext.getServiceManager()).willReturn(serviceManager);
    given(
            serviceManager.createInstanceWithContext(
                "com.sun.star.frame.DispatchHelper", componentContext))
        .willReturn(helperService);
    given(unoRuntime.queryInterface(XDispatchHelper.class, helperService)).willReturn(helper);
  }

  private void dispatchWillReturn(final Object result) {
    given(
            helper.executeDispatch(
                eq(provider), eq(COMMAND), anyString(), anyInt(), any(PropertyValue[].class)))
        .willReturn(result);
  }

  private static DispatchResultEvent result(final short state) {
    final var event = new DispatchResultEvent();
    event.State = state;
    return event;
  }

  private void doFilter() throws OfficeException {
    new DefaultFilterChain(false, new DocumentComparerFilter(original), next)
        .doFilter(context, document);
  }

  @Nested
  class New {

    @Test
    void withNullOriginal_ShouldThrowNullPointerException() {

      assertThatNullPointerException().isThrownBy(() -> new DocumentComparerFilter(null));
    }
  }

  @Nested
  class DoFilter {

    @Test
    void withUnsupportedDocument_ShouldOnlyCallNextFilter(final UnoRuntime unoRuntime)
        throws Exception {

      given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
      given(serviceInfo.supportsService(anyString())).willReturn(false);

      final var chain =
          new DefaultFilterChain(false, new DocumentComparerFilter(new File("missing.odt")), next);
      chain.doFilter(context, document);

      verify(next).doFilter(context, document, chain);
    }

    @Test
    void withEditableTextDocument_ShouldDispatchTheComparison(final UnoRuntime unoRuntime)
        throws Exception {

      setUp(unoRuntime);
      dispatchWillReturn(result(DispatchResultState.SUCCESS));

      doFilter();

      final var arguments = ArgumentCaptor.forClass(PropertyValue[].class);
      verify(helper).executeDispatch(eq(provider), eq(COMMAND), eq(""), eq(0), arguments.capture());
      assertThat(arguments.getValue())
          .extracting(property -> property.Name, property -> property.Value)
          .containsExactly(
              org.assertj.core.groups.Tuple.tuple("URL", LocalOfficeUtils.toUrl(original)),
              org.assertj.core.groups.Tuple.tuple("NoAcceptDialog", true));
      verify(next).doFilter(any(), eq(document), any());
    }

    @Test
    void whenTheOriginalDoesNotExist_ShouldThrowOfficeException(final UnoRuntime unoRuntime)
        throws Exception {

      setUp(unoRuntime);
      Files.delete(original.toPath());

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(DocumentComparerFilterTest.this::doFilter)
          .withMessage("The original document does not exist: " + original);
      verify(next, never()).doFilter(any(), any(), any());
    }

    @Test
    void withReadOnlyDocument_ShouldThrowOfficeException(final UnoRuntime unoRuntime)
        throws Exception {

      setUp(unoRuntime);
      given(storable.isReadonly()).willReturn(true);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(DocumentComparerFilterTest.this::doFilter)
          .withMessageContaining("loadProperty(\"ReadOnly\", false)");
      verify(helper, never()).executeDispatch(any(), any(), any(), anyInt(), any());
    }

    @Test
    void withoutView_ShouldThrowOfficeException(final UnoRuntime unoRuntime) throws Exception {

      setUp(unoRuntime);
      given(model.getCurrentController()).willReturn(null);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(DocumentComparerFilterTest.this::doFilter)
          .withMessageContaining("it has no view");
    }

    @Test
    void whenTheOfficeRefuses_ShouldThrowOfficeException(final UnoRuntime unoRuntime)
        throws Exception {

      setUp(unoRuntime);
      dispatchWillReturn(result(DispatchResultState.FAILURE));

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(DocumentComparerFilterTest.this::doFilter)
          .withMessage("The office could not compare the document with " + original);
    }

    @Test
    void whenTheOfficeGivesNoResult_ShouldThrowOfficeException(final UnoRuntime unoRuntime)
        throws Exception {

      setUp(unoRuntime);
      dispatchWillReturn(null);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(DocumentComparerFilterTest.this::doFilter)
          .withMessageContaining("could not compare");
    }
  }
}
