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

package org.jodconverter.core.job;

import java.io.File;

/**
 * An interface that provides the behavior to apply when a target file is no longer required by a
 * conversion process.
 */
public interface TargetDocumentSpecs extends DocumentSpecs {

  /**
   * Called if the conversion was completed successfully.
   *
   * @param file The file to which the conversion result was written.
   */
  void onComplete(File file);

  /**
   * Called if the conversion finished with an exception.
   *
   * @param file The file to which the conversion result was supposed to be written.
   * @param exception An exception representing the reason for the failed conversion.
   */
  void onFailure(File file, Exception exception);
}
