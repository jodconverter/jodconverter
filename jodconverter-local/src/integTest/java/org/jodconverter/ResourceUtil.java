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

package org.jodconverter;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

  public static File documentFile(String filename) {
    return new File("src/integTest/resources/documents", filename);
  }

  public static File imageFile(String filename) {
    return new File("src/integTest/resources/images", filename);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private ResourceUtil() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
