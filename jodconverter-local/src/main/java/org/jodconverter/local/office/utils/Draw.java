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

import com.sun.star.lang.XComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A collection of utility functions to make Office Draw documents (Drawing) easier to use.
 *
 * <p>Inspired by the work of Dr. Andrew Davison from the website <a
 * href="http://fivedots.coe.psu.ac.th/~ad/jlop">Java LibreOffice Programming</a>.
 */
public final class Draw { // NOPMD - Disable utility class name rule violation

  /**
   * Gets whether the given document is a Draw document.
   *
   * @param document The document to test.
   * @return {@code true} if the document is a Draw document, {@code false} otherwise.
   */
  public static boolean isDraw(@NonNull final XComponent document) {
    return Info.isDocumentType(document, Lo.DRAW_SERVICE);
  }

  /**
   * Gets whether the given document is a Presentation document.
   *
   * @param document The document to test.
   * @return {@code true} if the document is a Presentation document, {@code false} otherwise.
   */
  public static boolean isImpress(@NonNull final XComponent document) {
    return Info.isDocumentType(document, Lo.IMPRESS_SERVICE);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private Draw() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
