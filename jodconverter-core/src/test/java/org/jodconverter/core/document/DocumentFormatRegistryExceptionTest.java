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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Contains tests for the {@link DocumentFormatRegistryException} class. */
public class DocumentFormatRegistryExceptionTest {

  @Test
  public void create_IoExceptionThrownWhileLoading_ShouldThrowDocumentFormatRegistryException() {

    DefaultDocumentFormatRegistryInstanceHolder.setInstance(null);
    try (MockedStatic<JsonDocumentFormatRegistry> registry =
        mockStatic(JsonDocumentFormatRegistry.class, RETURNS_SMART_NULLS)) {
      registry
          .when(() -> JsonDocumentFormatRegistry.create(isA(InputStream.class)))
          .thenThrow(IOException.class);
      assertThatExceptionOfType(Throwable.class)
          .isThrownBy(DefaultDocumentFormatRegistry::getInstance)
          .satisfies(
              e ->
                  assertThat(e)
                      .isInstanceOfAny(
                          ExceptionInInitializerError.class, DocumentFormatRegistryException.class)
                      .hasRootCauseExactlyInstanceOf(IOException.class));
    }
  }
}
