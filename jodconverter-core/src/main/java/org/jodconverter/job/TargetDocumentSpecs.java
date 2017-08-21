/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.job;

import java.io.File;
import java.util.Map;

import org.jodconverter.document.DocumentFormat;

/**
 * An interface that provides the behavior to apply when a target file is no longer required by a
 * conversion process.
 */
public interface TargetDocumentSpecs extends DocumentSpecs {

  /**
   * Gets the custom properties that will be applied when a document is stored during a conversion
   * task.
   *
   * <p>Custom properties are applied after the stored properties of the {@link DocumentFormat} of
   * this TargetDocumentSpecs.
   *
   * @return A map containing the custom properties to apply when storing a document.
   */
  Map<String, Object> getCustomStoreProperties();

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
