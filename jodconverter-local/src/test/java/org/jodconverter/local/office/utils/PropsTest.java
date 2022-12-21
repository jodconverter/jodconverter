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

package org.jodconverter.local.office.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.test.util.AssertUtil;
import org.jodconverter.local.MockUnoRuntimeExtension;

/** Contains tests for the {@link Props} class. */
@ExtendWith(MockUnoRuntimeExtension.class)
class PropsTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(Props.class);
  }

  @Nested
  class GetProperty {

    @Test
    void withObjectAndPropertyFound_ShouldReturnExpectedProperty(final UnoRuntime unoRuntime) {

      assertThatCode(
              () -> {
                final Object object = mock(Object.class);
                final XPropertySet props = mock(XPropertySet.class);
                given(props.getPropertyValue("propTestName")).willReturn("propTestValue");
                given(unoRuntime.queryInterface(XPropertySet.class, object)).willReturn(props);

                assertThat(Props.getProperty(object, "propTestName")).isEqualTo("propTestValue");
              })
          .doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void withObjectAndPropertyNotFound_ShouldReturnExpectedProperty(final UnoRuntime unoRuntime) {

      assertThatCode(
              () -> {
                final Object object = mock(Object.class);
                final XPropertySet props = mock(XPropertySet.class);
                given(props.getPropertyValue("propTestName")).willReturn(null);
                given(unoRuntime.queryInterface(XPropertySet.class, object)).willReturn(props);

                assertThat(Props.getProperty(object, "propTestName")).isNull();
              })
          .doesNotThrowAnyException();
    }

    @Test
    void withUnknownPropertyException_ShouldThrowWrappedUnoException()
        throws WrappedTargetException, UnknownPropertyException {

      final XPropertySet props = mock(XPropertySet.class);
      given(props.getPropertyValue(isA(String.class))).willThrow(UnknownPropertyException.class);

      assertThatExceptionOfType(WrappedUnoException.class)
          .isThrownBy(() -> Props.getProperty(props, "test"))
          .withCauseExactlyInstanceOf(UnknownPropertyException.class);
    }

    @Test
    void wrappedTargetException_ShouldThrowWrappedUnoException()
        throws WrappedTargetException, UnknownPropertyException {

      final XPropertySet props = mock(XPropertySet.class);
      given(props.getPropertyValue(isA(String.class))).willThrow(WrappedTargetException.class);

      assertThatExceptionOfType(WrappedUnoException.class)
          .isThrownBy(() -> Props.getProperty(props, "test"))
          .withCauseExactlyInstanceOf(WrappedTargetException.class);
    }
  }

  @Nested
  class MakeProperty {

    @Test
    void shouldReturnExpectedValues() {

      final String name = "name";
      final Object value = 100;

      assertThat(Props.makeProperty(name, value))
          .extracting("Name", "Value")
          .containsExactly(name, value);
    }
  }

  @Nested
  class MakeProperties {

    @Test
    void withOneValue_ShouldReturnArrayWithExpectedValues() {

      final String name = "name";
      final Object value = 100;

      final PropertyValue[] props = Props.makeProperties(name, value);
      assertThat(props).hasSize(1);
      assertThat(props[0]).extracting("Name", "Value").containsExactly(name, value);
    }

    @Test
    void withTwoValue_ShouldReturnArrayWithExpectedValues() {

      final String name1 = "name1";
      final Object value1 = 100;
      final String name2 = "name2";
      final Object value2 = 200;

      final PropertyValue[] props = Props.makeProperties(name1, value1, name2, value2);
      assertThat(props).hasSize(2);
      assertThat(props[0]).extracting("Name", "Value").containsExactly(name1, value1);
      assertThat(props[1]).extracting("Name", "Value").containsExactly(name2, value2);
    }

    @Test
    void withArrayNotSameLength_ShouldThrowIllegalArgumentException() {

      final String[] names = {"name1", "name2", "name3"};
      final Object[] values = {100, 200, 300, 400};
      assertThatIllegalArgumentException().isThrownBy(() -> Props.makeProperties(names, values));
    }

    @Test
    void withArraySameLength_ShouldReturnArrayWithExpectedValues() {

      final String[] names = {"name1", "name2", "name3", "name4"};
      final Object[] values = {100, 200, 300, 400};

      final PropertyValue[] props = Props.makeProperties(names, values);
      assertThat(props).hasSize(4);
      assertThat(props[0]).extracting("Name", "Value").containsExactly("name1", 100);
      assertThat(props[1]).extracting("Name", "Value").containsExactly("name2", 200);
      assertThat(props[2]).extracting("Name", "Value").containsExactly("name3", 300);
      assertThat(props[3]).extracting("Name", "Value").containsExactly("name4", 400);
    }
  }
}
