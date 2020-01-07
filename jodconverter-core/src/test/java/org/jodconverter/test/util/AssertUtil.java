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

package org.jodconverter.test.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class AssertUtil {

  /**
   * Verifies that a utility class is well defined.
   *
   * @param clazz Utility class to verify.
   */
  public static void assertUtilityClassWellDefined(final Class<?> clazz)
      throws NoSuchMethodException {

    // Check final identifier
    assertThat(clazz).as("Check class final identifier").isFinal();

    // Check single private constructor throwing AssertionError
    assertThat(clazz.getDeclaredConstructors().length)
        .as("Check class single constructor")
        .isEqualTo(1);
    final Constructor<?> constructor = clazz.getDeclaredConstructor();
    assertThat(constructor.isAccessible() || !Modifier.isPrivate(constructor.getModifiers()))
        .as("Check class constructor modifier")
        .isFalse();
    constructor.setAccessible(true);

    assertThatExceptionOfType(InvocationTargetException.class)
        .isThrownBy(constructor::newInstance)
        .satisfies(
            e -> assertThat(e.getTargetException()).isExactlyInstanceOf(AssertionError.class));

    // Check for static method only
    Arrays.stream(clazz.getMethods())
        .forEach(
            method ->
                assertThat(
                        !Modifier.isStatic(method.getModifiers())
                            && method.getDeclaringClass().equals(clazz))
                    .as("Check class non-static method")
                    .isFalse());
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private AssertUtil() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
