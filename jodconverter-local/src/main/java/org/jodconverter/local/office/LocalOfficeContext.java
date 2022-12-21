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

package org.jodconverter.local.office;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.office.OfficeContext;

/** Represents an office context for local conversions. */
public interface LocalOfficeContext extends OfficeContext {

  /**
   * Gets the office component loader for this context.
   *
   * @return The {@link com.sun.star.frame.XComponentLoader}.
   */
  @Nullable
  XComponentLoader getComponentLoader();

  /**
   * Gets the ComponentContext interface of the remote component context.
   *
   * @return The {@link com.sun.star.uno.XComponentContext}.
   */
  @Nullable
  XComponentContext getComponentContext();

  /**
   * Gets the MultiComponentFactory interface of the remote service manager.
   *
   * @return The {@link com.sun.star.lang.XMultiComponentFactory}.
   */
  @Nullable
  XMultiComponentFactory getServiceManager();

  /**
   * Gets the Desktop interface of the desktop service.
   *
   * @return The {@link com.sun.star.frame.XDesktop}.
   */
  @Nullable
  XDesktop getDesktop();
}
