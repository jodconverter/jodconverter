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

package org.jodconverter.core.office;

import java.io.File;

/**
 * This class provides the configuration of an {@link AbstractOfficeManager}.
 *
 * @see AbstractOfficeManager
 */
public interface OfficeManagerConfig {

  /**
   * Gets the directory where temporary files will be created when working with streams.
   *
   * <p>&nbsp; <b><i>Default</i></b>: The system temporary directory as specified by the <code>
   * java.io.tmpdir</code> system property.
   *
   * @return The working directory.
   */
  File getWorkingDir();

  /**
   * Sets the directory where temporary files will be created when working with streams.
   *
   * @param workingDir The new working directory.
   */
  void setWorkingDir(final File workingDir);
}
