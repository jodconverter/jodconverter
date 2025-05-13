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

package org.jodconverter.core.office;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Holds a unique instance of an {@link org.jodconverter.core.office.OfficeManager} that will be
 * used by created {@link org.jodconverter.core.DocumentConverter} when no office manager is given
 * to the converter builder.
 */
public final
class InstalledOfficeManagerHolder { // NOPMD - Disable utility class name rule violation

  private static OfficeManager instance;

  /**
   * Gets the static instance of the static holder class.
   *
   * @return The main default office manager.
   */
  public static @Nullable OfficeManager getInstance() {

    synchronized (InstalledOfficeManagerHolder.class) {
      return instance;
    }
  }

  /**
   * Sets the static instance of the static holder class.
   *
   * @param manager The main default office manager.
   * @return the previous installed office manager, or {@code null} if there was no installed office
   *     manager.
   */
  public static @Nullable OfficeManager setInstance(final @Nullable OfficeManager manager) {

    synchronized (InstalledOfficeManagerHolder.class) {
      final OfficeManager oldManager = instance;
      instance = manager;
      return oldManager;
    }
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private InstalledOfficeManagerHolder() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
