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
import java.util.HashMap;
import java.util.Map;

import org.jodconverter.document.DocumentFormat;

/**
 * Base class for all target document specifications implementations.
 *
 * @see TargetDocumentSpecs
 */
public abstract class AbstractTargetDocumentSpecs extends AbstractDocumentSpecs
    implements TargetDocumentSpecs {

  private Map<String, Object> customStoreProperties;

  protected AbstractTargetDocumentSpecs(final File file) {
    super(file);
  }

  @Override
  public Map<String, Object> getCustomStoreProperties() {
    return customStoreProperties;
  }

  /**
   * Sets the custom properties that will be applied when a document is stored during a conversion
   * task.
   *
   * <p>Custom properties are applied after the stored properties of the {@link DocumentFormat} of
   * this TargetDocumentSpecs.
   *
   * @param customStoreProperties A map containing the custom properties to apply when storing a
   *     document.
   */
  void setCustomStoreProperties(final Map<String, Object> customStoreProperties) {

    this.customStoreProperties = new HashMap<>(customStoreProperties);
  }
}
