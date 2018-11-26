/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

package org.jodconverter.task;

import com.sun.star.lang.XComponent;

import org.jodconverter.document.DocumentFamily;
import org.jodconverter.office.LocalOfficeUtils;
import org.jodconverter.office.OfficeException;

/**
 * Provides helper functions for local office tasks.
 *
 * @deprecated
 * @see LocalOfficeUtils#getDocumentFamily(XComponent)
 */
@Deprecated
final class LocalOfficeTaskUtils {

  /**
   * Gets the {@link DocumentFamily} if the specified document.
   *
   * @param document The document whose family will be returned.
   * @return The {@link DocumentFamily} for the specified document.
   * @throws OfficeException If the document family cannot be retrieved.
   * @deprecated
   * @see LocalOfficeUtils#getDocumentFamily(XComponent)
   */
  @Deprecated
  public static DocumentFamily getDocumentFamily(final XComponent document) throws OfficeException {
    return LocalOfficeUtils.getDocumentFamily(document);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private LocalOfficeTaskUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
