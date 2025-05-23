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

package org.jodconverter.core.job;

import java.io.File;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An interface that provides the behavior to apply when a source file is no longer required by a
 * conversion process.
 */
public interface SourceDocumentSpecs extends DocumentSpecs {

  /**
   * Called when the file was consumed and is no longer required by the converter. The file must not
   * be removed from the file system before this method is called.
   *
   * @param file The file that was consumed.
   */
  default void onConsumed(final @NonNull File file) {
    // The default behavior is to do nothing
  }
}
