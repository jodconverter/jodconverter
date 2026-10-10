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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.uno.XComponentContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.MockUnoRuntimeExtension;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.OfficeFilterInfo;
import org.jodconverter.local.office.OfficeInfo;
import org.jodconverter.local.office.utils.Props;
import org.jodconverter.local.office.utils.UnoRuntime;

/**
 * Contains tests for the {@link OfficeInfoTask} class, and for the {@link OfficeInfo} and {@link
 * OfficeFilterInfo} records.
 */
@ExtendWith(MockUnoRuntimeExtension.class)
class OfficeInfoTaskTest {

  private static final String FILTER_FACTORY = "com.sun.star.document.FilterFactory";
  private static final String TYPE_DETECTION = "com.sun.star.document.TypeDetection";

  private final LocalOfficeContext context = mock(LocalOfficeContext.class);
  private final XComponentContext componentContext = mock(XComponentContext.class);
  private final XMultiComponentFactory serviceManager = mock(XMultiComponentFactory.class);
  private final XNameAccess filters = mock(XNameAccess.class);
  private final XNameAccess types = mock(XNameAccess.class);

  // An office without configuration provider (no product name, no version), with the given
  // services.
  private void setUp(final UnoRuntime unoRuntime, final boolean withFilters) throws Exception {

    given(context.getComponentContext()).willReturn(componentContext);
    given(componentContext.getServiceManager()).willReturn(serviceManager);
    final var filtersService = new Object();
    final var typesService = new Object();
    given(serviceManager.createInstanceWithContext(FILTER_FACTORY, componentContext))
        .willReturn(filtersService);
    given(serviceManager.createInstanceWithContext(TYPE_DETECTION, componentContext))
        .willReturn(typesService);
    if (withFilters) {
      given(unoRuntime.queryInterface(XNameAccess.class, filtersService)).willReturn(filters);
    }
    given(unoRuntime.queryInterface(XNameAccess.class, typesService)).willReturn(types);
  }

  @Nested
  class Execute {

    @Test
    void withAContextThatIsNotLocal_ShouldThrowOfficeException() {

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> new OfficeInfoTask().execute(mock(OfficeContext.class)))
          .withMessageContaining("local or an attached office");
    }

    @Test
    void whenNotConnected_ShouldThrowOfficeException() {

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> new OfficeInfoTask().execute(context))
          .withMessage("The office is not connected");
    }

    @Test
    void withoutFilterFactory_ShouldThrowOfficeException(final UnoRuntime unoRuntime)
        throws Exception {

      setUp(unoRuntime, false);

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> new OfficeInfoTask().execute(context))
          .withMessage("The office does not give access to its filters");
    }

    @Test
    void whenAFilterCannotBeRead_ShouldThrowOfficeException(final UnoRuntime unoRuntime)
        throws Exception {

      setUp(unoRuntime, true);
      given(filters.getElementNames()).willReturn(new String[] {"gone"});
      given(filters.getByName("gone")).willThrow(new NoSuchElementException("gone"));
      final var task = new OfficeInfoTask();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> task.execute(context))
          .withMessage("Could not read the office information")
          .withCauseInstanceOf(NoSuchElementException.class);
      assertThatIllegalStateException().isThrownBy(task::getInfo);
    }

    @Test
    void withFilters_ShouldDescribeThemSortedByName(final UnoRuntime unoRuntime) throws Exception {

      setUp(unoRuntime, true);
      given(filters.getElementNames()).willReturn(new String[] {"writer_x", "calc_y", "bare"});
      given(filters.getByName("writer_x"))
          .willReturn(
              new PropertyValue[] {
                Props.makeProperty("Type", "type_x"),
                Props.makeProperty("Flags", 0x1 | 0x2 | 0x100),
                Props.makeProperty("DocumentService", "com.sun.star.text.TextDocument"),
                Props.makeProperty("UIName", "X Document"),
              });
      // The office may wrap the description, and its values, in an Any.
      given(filters.getByName("calc_y"))
          .willReturn(
              new Any(
                  new Type(PropertyValue[].class),
                  new PropertyValue[] {
                    Props.makeProperty("Type", new Any(Type.STRING, "type_unknown")),
                    Props.makeProperty("Flags", new Any(Type.LONG, 0x2)),
                    Props.makeProperty("UIName", ""),
                    Props.makeProperty("UserData", Any.VOID),
                  }));
      given(filters.getByName("bare")).willReturn("not a description");
      given(types.hasByName("type_x")).willReturn(true);
      given(types.getByName("type_x"))
          .willReturn(
              new PropertyValue[] {
                Props.makeProperty("MediaType", "application/x"),
                Props.makeProperty("Extensions", new String[] {"x", "xx"}),
              });

      final var task = new OfficeInfoTask();
      task.execute(context);

      final var info = task.getInfo();
      assertThat(info.productName()).isNull();
      assertThat(info.version()).isNull();
      assertThat(info.filters())
          .containsExactly(
              new OfficeFilterInfo("bare", "bare", null, null, null, List.of(), false, false),
              new OfficeFilterInfo(
                  "calc_y", "calc_y", "type_unknown", null, null, List.of(), false, true),
              new OfficeFilterInfo(
                  "writer_x",
                  "X Document",
                  "type_x",
                  "com.sun.star.text.TextDocument",
                  "application/x",
                  List.of("x", "xx"),
                  true,
                  true));
      assertThat(info.importFilters())
          .extracting(OfficeFilterInfo::name)
          .containsExactly("writer_x");
      assertThat(info.exportFilters())
          .extracting(OfficeFilterInfo::name)
          .containsExactly("calc_y", "writer_x");
    }
  }

  @Nested
  class Query {

    @Test
    void shouldExecuteATaskWithTheManager(final UnoRuntime unoRuntime) throws Exception {

      setUp(unoRuntime, true);
      given(filters.getElementNames()).willReturn(new String[0]);
      final var manager = mock(OfficeManager.class);
      willAnswer(
              invocation -> {
                invocation.<OfficeTask>getArgument(0).execute(context);
                return null;
              })
          .given(manager)
          .execute(any(OfficeTask.class));

      assertThat(OfficeInfoTask.query(manager)).isEqualTo(new OfficeInfo(null, null, List.of()));
    }
  }

  @Nested
  class GetInfo {

    @Test
    void whenNotExecuted_ShouldThrowIllegalStateException() {

      assertThatIllegalStateException()
          .isThrownBy(() -> new OfficeInfoTask().getInfo())
          .withMessage("The task was not executed");
    }
  }
}
