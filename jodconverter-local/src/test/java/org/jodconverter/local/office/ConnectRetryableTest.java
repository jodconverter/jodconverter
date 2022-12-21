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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.office.RetryTimeoutException;

/** Contains tests for the {@link ConnectRetryable} class. */
class ConnectRetryableTest {

  @Test
  void whenAbleToConnect_ShouldNotThrowAnyException() throws OfficeConnectionException {

    final OfficeConnection connection = mock(OfficeConnection.class);

    assertThatCode(
            () -> {
              final ConnectRetryable retryable = new ConnectRetryable(connection);
              retryable.execute(0L, 0L);
            })
        .doesNotThrowAnyException();

    verify(connection, times(1)).connect();
  }

  @Test
  void whenUnableToConnect_ShouldThrowTemporaryException() throws OfficeConnectionException {

    final OfficeConnection connection = mock(OfficeConnection.class);
    willThrow(OfficeConnectionException.class).given(connection).connect();

    assertThatExceptionOfType(RetryTimeoutException.class)
        .isThrownBy(
            () -> {
              final ConnectRetryable retryable = new ConnectRetryable(connection);
              retryable.execute(150L, 100L);
            });

    verify(connection, times(2)).connect();
  }
}
