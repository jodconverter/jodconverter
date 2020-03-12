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

package org.jodconverter.local;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.office.LocalOfficeManager;

/** Provides a {@link LocalOfficeManager} that can be use as argument to a test method. */
public class LocalOfficeManagerExtension implements ParameterResolver {

  private static final Namespace NAMESPACE = create(LocalOfficeManagerExtension.class);

  @Override
  public boolean supportsParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {

    final Class<?> type = parameterContext.getParameter().getType();
    return OfficeManager.class.isAssignableFrom(type)
        || DocumentConverter.class.isAssignableFrom(type);
  }

  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {

    final Class<?> type = parameterContext.getParameter().getType();
    if (OfficeManager.class.isAssignableFrom(type)) {
      return getOfficeManager(extensionContext);
    }
    if (DocumentConverter.class.isAssignableFrom(type)) {
      return LocalConverter.make(getOfficeManager(extensionContext));
    }
    return null;
  }

  private static ExtensionContext.Store getStore(final ExtensionContext context) {
    return context.getRoot().getStore(NAMESPACE);
  }

  private static OfficeManager getOfficeManager(final ExtensionContext context) {
    return getStore(context).getOrComputeIfAbsent(LocalOfficeManagerResource.class).get();
  }

  /** Local office manager resource. */
  private static class LocalOfficeManagerResource implements CloseableResource {

    private final LocalOfficeManager manager;

    /* default */ LocalOfficeManagerResource() {
      // Create the office manager. Don't use the default port number here
      // in order to be able to use it in other tests.
      final String property = System.getProperty("org.jodconverter.test.local.portNumbers");
      final int[] portNumbers =
          StringUtils.isBlank(property)
              ? new int[] {2099}
              : Stream.of(property.split("\\s*,\\s*"))
                  .mapToInt(
                      str -> {
                        try {
                          return Integer.parseInt(str);
                        } catch (final Exception e) {
                          return 2099;
                        }
                      })
                  .toArray();
      // TODO: Add support for custom configuration.
      final LocalOfficeManager mng =
          LocalOfficeManager.builder().portNumbers(portNumbers).maxTasksPerProcess(1000).build();
      try {
        mng.start();
      } catch (OfficeException ex) {
        throw new JUnitException("Could not start an office process for the test suite", ex);
      }
      this.manager = mng;
    }

    /* default */ LocalOfficeManager get() {
      return manager;
    }

    @Override
    public void close() {

      try {
        manager.stop();
      } catch (OfficeException ex) {
        throw new JUnitException(
            "Local office manager executor could not be stopped in an orderly manner", ex);
      }
    }
  }
}
