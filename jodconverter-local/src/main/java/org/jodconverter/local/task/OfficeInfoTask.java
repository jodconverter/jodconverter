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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.uno.Any;
import com.sun.star.uno.XComponentContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.OfficeFilterInfo;
import org.jodconverter.local.office.OfficeInfo;
import org.jodconverter.local.office.utils.Info;
import org.jodconverter.local.office.utils.Lo;

/**
 * A task that asks a running office what it is: its product name, its version, and the filters it
 * has to load and to store documents. It loads no document.
 *
 * <pre>{@code
 * OfficeInfo info = OfficeInfoTask.query(officeManager);
 * }</pre>
 *
 * <p>The task is meant for diagnostics, at startup or behind a support endpoint: it asks the office
 * for each of its filters, which is a few hundred calls. Do not execute it for each conversion.
 */
public class OfficeInfoTask implements OfficeTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeInfoTask.class);

  private static final String FILTER_FACTORY = "com.sun.star.document.FilterFactory";
  private static final String TYPE_DETECTION = "com.sun.star.document.TypeDetection";

  // com.sun.star.document.FilterFlags
  private static final int FLAG_IMPORT = 0x1;
  private static final int FLAG_EXPORT = 0x2;

  private static final String UI_NAME = "UIName";

  private OfficeInfo info;

  /**
   * Executes a new task with the given manager, and gets its result.
   *
   * @param officeManager The manager of the office to describe, a local or an attached one.
   * @return The description of the office.
   * @throws OfficeException If the task cannot be executed.
   */
  public static @NonNull OfficeInfo query(final @NonNull OfficeManager officeManager)
      throws OfficeException {

    final var task = new OfficeInfoTask();
    officeManager.execute(task);
    return task.getInfo();
  }

  /**
   * Gets the description of the office that executed this task.
   *
   * @return The description of the office.
   * @throws IllegalStateException If the task was not executed successfully.
   */
  public @NonNull OfficeInfo getInfo() {
    if (info == null) {
      throw new IllegalStateException("The task was not executed");
    }
    return info;
  }

  @Override
  public void execute(final @NonNull OfficeContext context) throws OfficeException {

    if (!(context instanceof LocalOfficeContext localContext)) {
      throw new OfficeException(
          "The office information can only be read from a local or an attached office");
    }
    final var componentContext = localContext.getComponentContext();
    if (componentContext == null) {
      throw new OfficeException("The office is not connected");
    }

    LOGGER.debug("Executing office info task...");
    try {
      info =
          new OfficeInfo(
              Info.getOfficeName(componentContext),
              Info.getOfficeVersionLong(componentContext),
              readFilters(componentContext));
    } catch (OfficeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new OfficeException("Could not read the office information", ex);
    }
    LOGGER.debug(
        "Office info: {} {}, {} filters",
        info.productName(),
        info.version(),
        info.filters().size());
  }

  private static List<OfficeFilterInfo> readFilters(final XComponentContext context)
      throws Exception {

    final var filters = Lo.createInstance(context, XNameAccess.class, FILTER_FACTORY);
    final var types = Lo.createInstance(context, XNameAccess.class, TYPE_DETECTION);
    if (filters == null || types == null) {
      throw new OfficeException("The office does not give access to its filters");
    }

    final var names = filters.getElementNames();
    Arrays.sort(names);
    final var result = new ArrayList<OfficeFilterInfo>(names.length);
    for (final var name : names) {
      result.add(readFilter(name, toMap(filters.getByName(name)), types));
    }
    return result;
  }

  private static OfficeFilterInfo readFilter(
      final String name, final Map<String, Object> filter, final XNameAccess types)
      throws Exception {

    final var typeName = string(filter.get("Type"));
    final var type =
        typeName != null && types.hasByName(typeName)
            ? toMap(types.getByName(typeName))
            : Map.<String, Object>of();
    final var flags = filter.get("Flags") instanceof Number number ? number.intValue() : 0;
    final var uiName = string(filter.get(UI_NAME));
    return new OfficeFilterInfo(
        name,
        uiName == null ? name : uiName,
        typeName,
        string(filter.get("DocumentService")),
        string(type.get("MediaType")),
        type.get("Extensions") instanceof String[] extensions ? List.of(extensions) : List.of(),
        (flags & FLAG_IMPORT) != 0,
        (flags & FLAG_EXPORT) != 0);
  }

  // The description of a filter or of a type is a sequence of named values.
  private static Map<String, Object> toMap(final Object description) {

    final var map = new HashMap<String, Object>();
    if (unwrap(description) instanceof PropertyValue[] properties) {
      for (final var property : properties) {
        final var value = unwrap(property.Value);
        if (value != null) {
          map.put(property.Name, value);
        }
      }
    }
    return map;
  }

  private static @Nullable Object unwrap(final @Nullable Object value) {
    return value instanceof Any any ? any.getObject() : value;
  }

  private static @Nullable String string(final @Nullable Object value) {
    return value instanceof String string && !string.isEmpty() ? string : null;
  }
}
