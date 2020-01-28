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

package org.jodconverter.local.office.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import org.jodconverter.core.test.util.AssertUtil;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest(UnoRuntime.class)
public class PropsTest {

  @Test
  public void ctor_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(Props.class);
  }

  @Test
  public void getProperty_WithObjectAndPropertyFound_ReturnExpectedProperty() {

    assertThatCode(
            () -> {
              final Object object = mock(Object.class);
              final XPropertySet props = mock(XPropertySet.class);
              given(props.getPropertyValue("propTestName")).willReturn("propTestValue");
              mockStatic(UnoRuntime.class);
              given(UnoRuntime.queryInterface(XPropertySet.class, object)).willReturn(props);

              assertThat(Props.getProperty(object, "propTestName")).hasValue("propTestValue");
            })
        .doesNotThrowAnyException();
  }

  @Test
  public void getProperty_WithObjectAndPropertyNotFound_ReturnExpectedProperty() {

    assertThatCode(
            () -> {
              final Object object = mock(Object.class);
              final XPropertySet props = mock(XPropertySet.class);
              given(props.getPropertyValue("propTestName")).willReturn(null);
              mockStatic(UnoRuntime.class);
              given(UnoRuntime.queryInterface(XPropertySet.class, object)).willReturn(props);

              assertThat(Props.getProperty(object, "propTestName")).isEmpty();
            })
        .doesNotThrowAnyException();
  }

  @Test
  public void getProperty_WithUnknownPropertyException_ThrowWrappedUnoException()
      throws WrappedTargetException, UnknownPropertyException {

    final XPropertySet props = mock(XPropertySet.class);
    given(props.getPropertyValue(isA(String.class))).willThrow(UnknownPropertyException.class);

    assertThatExceptionOfType(WrappedUnoException.class)
        .isThrownBy(() -> Props.getProperty(props, "test"))
        .withCauseExactlyInstanceOf(UnknownPropertyException.class);
  }

  @Test
  public void getProperty_WrappedTargetException_ThrowWrappedUnoException()
      throws WrappedTargetException, UnknownPropertyException {

    final XPropertySet props = mock(XPropertySet.class);
    given(props.getPropertyValue(isA(String.class))).willThrow(WrappedTargetException.class);

    assertThatExceptionOfType(WrappedUnoException.class)
        .isThrownBy(() -> Props.getProperty(props, "test"))
        .withCauseExactlyInstanceOf(WrappedTargetException.class);
  }

  @Test
  public void makeProperties_WithOneValue_ReturnArrayWithExpectedValues() {

    final String name = "name";
    final Object value = 100;

    final PropertyValue[] props = Props.makeProperties(name, value);
    assertThat(props).hasSize(1);
    assertThat(props[0]).extracting("Name", "Value").containsExactly(name, value);
  }

  @Test
  public void makeProperties_WithTwoValue_ReturnArrayWithExpectedValues() {

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
  public void makeProperties_ArrayNotSameLength_ThrowIllegalArgumentException() {

    final String[] names = new String[] {"name1", "name2", "name3"};
    final Object[] values = new Object[] {100, 200, 300, 400};
    assertThatIllegalArgumentException().isThrownBy(() -> Props.makeProperties(names, values));
  }

  @Test
  public void makeProperties_ArraySameLength_ReturnArrayWithExpectedValues() {

    final String[] names = new String[] {"name1", "name2", "name3", "name4"};
    final Object[] values = new Object[] {100, 200, 300, 400};

    final PropertyValue[] props = Props.makeProperties(names, values);
    assertThat(props).hasSize(4);
    assertThat(props[0]).extracting("Name", "Value").containsExactly("name1", 100);
    assertThat(props[1]).extracting("Name", "Value").containsExactly("name2", 200);
    assertThat(props[2]).extracting("Name", "Value").containsExactly("name3", 300);
    assertThat(props[3]).extracting("Name", "Value").containsExactly("name4", 400);
  }
}
