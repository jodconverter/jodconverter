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

package org.jodconverter.core.document;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link DefaultDocumentFormatRegistryInstanceHolder} class. */
public class DefaultDocumentFormatRegistryInstanceHolderTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(DefaultDocumentFormatRegistryInstanceHolder.class);
  }

  @Test
  public void setInstance_WithCustomRegistry_getInstanceShouldReturnCutomRedistry() {

    final DocumentFormatRegistry registry =
        new DocumentFormatRegistry() {
          @Override
          public DocumentFormat getFormatByExtension(String extension) {
            return null;
          }

          @Override
          public DocumentFormat getFormatByMediaType(String mediaType) {
            return null;
          }

          @Override
          public Set<DocumentFormat> getOutputFormats(DocumentFamily family) {
            return null;
          }

          @Override
          public String toString() {
            return "setInstance_WithCustomRegistry_getInstanceShouldReturnCutomRedistry";
          }
        };

    final DocumentFormatRegistry saved = DefaultDocumentFormatRegistryInstanceHolder.getInstance();
    try {
      DefaultDocumentFormatRegistryInstanceHolder.setInstance(registry);
      Assertions.assertThat(DefaultDocumentFormatRegistryInstanceHolder.getInstance().toString())
          .isEqualTo("setInstance_WithCustomRegistry_getInstanceShouldReturnCutomRedistry");
    } finally {
      DefaultDocumentFormatRegistryInstanceHolder.setInstance(saved);
    }
  }
}
