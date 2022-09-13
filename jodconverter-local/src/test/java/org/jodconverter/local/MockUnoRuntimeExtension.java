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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.mockito.Mockito;

import org.jodconverter.local.office.utils.UnoRuntime;

/** Provides a mocked {@link UnoRuntime} that can be use as argument to a test method. */
public class MockUnoRuntimeExtension implements AfterEachCallback, ParameterResolver {

  private static final Namespace NAMESPACE = create(MockUnoRuntimeExtension.class);

  @Override
  public void afterEach(final ExtensionContext context) {
    // Restore the UnoRuntime instance after each test.
    if (UnoRuntime.getInstance().getClass() != UnoRuntime.class) {
      UnoRuntime.setInstance(getUnoRuntime(context));
    }
  }

  @Override
  public boolean supportsParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == UnoRuntime.class;
  }

  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext) {

    // Call the getUnoRuntime to ensure the default UnoRuntime is saved.
    getUnoRuntime(extensionContext);

    UnoRuntime.setInstance(Mockito.mock(UnoRuntime.class));
    return UnoRuntime.getInstance();
  }

  private static ExtensionContext.Store getStore(final ExtensionContext context) {
    return context.getRoot().getStore(NAMESPACE);
  }

  private static UnoRuntime getUnoRuntime(final ExtensionContext context) {
    return getStore(context).getOrComputeIfAbsent(UnoRuntimeResource.class).get();
  }

  /** UnoRuntime resource. */
  private static class UnoRuntimeResource {

    private final UnoRuntime unoRuntime;

    /* default */ UnoRuntimeResource() {
      final UnoRuntime runtime = UnoRuntime.getInstance();
      this.unoRuntime = runtime.getClass() == UnoRuntime.class ? runtime : new UnoRuntime();
    }

    /* default */ UnoRuntime get() {
      return unoRuntime;
    }
  }
}
