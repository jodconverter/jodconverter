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

import java.util.Optional;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;

/**
 * A collection of utility functions to make Office properties easier to use.
 *
 * <p>Inspired by the work of Dr. Andrew Davison from the website <a
 * href="http://fivedots.coe.psu.ac.th/~ad/jlop">Java LibreOffice Programming</a>.
 */
public final class Props {

  // private static final Logger LOGGER = LoggerFactory.getLogger(Props.class);

  /**
   * Gets a property value from the properties of the specified object.
   *
   * @param obj The object from which a property is get.
   * @param propName The property name to get.
   * @return An optional containing the property value. the optional will be empty if the property
   *     could not be retrieved.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static Optional<Object> getProperty(final Object obj, final String propName) {

    return getProperty(Lo.qi(XPropertySet.class, obj), propName);
  }

  /**
   * Gets a property value from the specified properties.
   *
   * @param props The {@link XPropertySet} from which a property is get.
   * @param propName The property name to get.
   * @return An optional containing the property value. the optional will be empty if the property
   *     could not be retrieved.
   * @throws WrappedUnoException If an UNO exception occurs. The UNO exception will be the cause of
   *     the {@link WrappedUnoException}.
   */
  public static Optional<Object> getProperty(final XPropertySet props, final String propName) {

    try {
      return Optional.ofNullable(props.getPropertyValue(propName));
    } catch (UnknownPropertyException | WrappedTargetException ex) {
      throw new WrappedUnoException(ex.getMessage(), ex);
    }
  }

  /**
   * Creates an array of {@link PropertyValue} with a single property using the specified property
   * name and value.
   *
   * @param name The property name.
   * @param value The property value.
   * @return An array of size 1.
   */
  public static PropertyValue[] makeProperties(final String name, final Object value) {

    final PropertyValue[] props = {new PropertyValue()};
    props[0].Name = name;
    props[0].Value = value;
    return props;
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
  public static PropertyValue[] makeProperties(
      final String name1, final Object value1, final String name2, final Object value2) {

    final PropertyValue[] props = {new PropertyValue(), new PropertyValue()};
    props[0].Name = name1;
    props[0].Value = value1;
    props[1].Name = name2;
    props[1].Value = value2;
    return props;
  }

  /**
   * Creates an array of {@link PropertyValue} with properties using the specified property names
   * and values.
   *
   * @param names The property names.
   * @param values The property values.
   * @return An array of properties.
   */
  public static PropertyValue[] makeProperties(final String[] names, final Object[] values) {

    if (names.length != values.length) {
      throw new IllegalArgumentException("Mismatch in lengths of names and values");
    }

    final PropertyValue[] props = new PropertyValue[names.length];
    for (int i = 0; i < names.length; i++) {
      props[i] = new PropertyValue();
      props[i].Name = names[i];
      props[i].Value = values[i];
    }
    return props;
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private Props() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
