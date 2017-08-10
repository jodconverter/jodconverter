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

import java.util.Map;

import org.jodconverter.document.DocumentFormat;

/**
 * A sufficiently specified conversion job with store properties that is not yet applied to the
 * converter.
 */
public interface ConversionJobWithStorePropertiesUnspecified extends ConversionJob {

  /**
   * Specifies the custom properties that will be applied when a document is stored during the
   * conversion task.
   *
   * <p>Custom properties are applied after the store properties of the target {@link
   * DocumentFormat}.
   *
   * @param properties A map containing the custom properties to apply when storing a document.
   * @return This builder instance.
   */
  public ConversionJob storeWithProperties(Map<String, Object> properties);
}
