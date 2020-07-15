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

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A collection of utility functions to make Office properties easier to use.
 *
 * <p>Inspired by the work of Dr. Andrew Davison from the website <a
 * href="http://fivedots.coe.psu.ac.th/~ad/jlop">Java LibreOffice Programming</a>.
 */
public final class Props { // NOPMD - Disable utility class name rule violation

  // private static final Logger LOGGER = LoggerFactory.getLogger(Props.class);

  /**
   * Gets a property value from the properties of the specified object.
   *
   * @param obj The object from which a property is get.
   * @param propName The property name to get.
   * @return The property value, or null if the property could not be retrieved.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static @NonNull Object getProperty(
      final @NonNull Object obj, final @NonNull String propName) {
    return getProperty(Lo.qi(XPropertySet.class, obj), propName);
  }

  /**
   * Gets a property value from the specified properties.
   *
   * @param props The {@link XPropertySet} from which a property is get.
   * @param propName The property name to get.
   * @return The property value, or null if the property could not be retrieved.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static @NonNull Object getProperty(
      final @NonNull XPropertySet props, final @NonNull String propName) {

    try {
      return props.getPropertyValue(propName);
    } catch (UnknownPropertyException | WrappedTargetException ex) {
      throw new WrappedUnoException(ex.getMessage(), ex);
    }
  }

  /**
   * Creates a {@code PropertyValue} with the specified name and value.
   *
   * @param name The property name.
   * @param value The property value.
   * @return The created {@code PropertyValue}.
   */
  public static @NonNull PropertyValue makeProperty(
      final @NonNull String name, final @NonNull Object value) {

    final PropertyValue prop = new PropertyValue();
    prop.Name = name;
    prop.Value = value;
    return prop;
  }

  /**
   * Creates an array of {@link PropertyValue} with a single property using the specified property
   * name and value.
   *
   * @param name The property name.
   * @param value The property value.
   * @return An array of size 1.
   */
  public static @NonNull PropertyValue[] makeProperties(
      final @NonNull String name, final @NonNull Object value) {

    return new PropertyValue[] {makeProperty(name, value)};
  }

  /**
   * Creates an array of {@link PropertyValue} with 2 properties using the specified property names
   * and values.
   *
   * @param name1 The first property name.
   * @param value1 The first property value.
   * @param name2 The second property name.
   * @param value2 The second property value.
   * @return An array of size 2.
   */
  public static @NonNull PropertyValue[] makeProperties(
      final @NonNull String name1,
      final @NonNull Object value1,
      final @NonNull String name2,
      final @NonNull Object value2) {

    return new PropertyValue[] {makeProperty(name1, value1), makeProperty(name2, value2)};
  }

  /**
   * Creates an array of {@link PropertyValue} with properties using the specified property names
   * and values.
   *
   * @param names The property names.
   * @param values The property values.
   * @return An array of properties.
   */
  public static @NonNull PropertyValue[] makeProperties(
      final @NonNull String[] names, final @NonNull Object[] values) {

    if (names.length != values.length) {
      throw new IllegalArgumentException("Mismatch in lengths of names and values");
    }

    final PropertyValue[] props = new PropertyValue[names.length];
    for (int i = 0; i < names.length; i++) {
      props[i] = makeProperty(names[i], values[i]);
    }
    return props;
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private Props() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
