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

package org.jodconverter.core.office;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Contains tests for the {@link OfficeWorkerPoolStatus} class. */
class OfficeWorkerPoolStatusTest {

  private static OfficeWorkerStatus worker(final OfficeWorkerState state) {
    return new OfficeWorkerStatus(state, 0, 0, 0);
  }

  @Test
  void isRunning_WhenNoWorkerIsReadyOrBusy_ShouldReturnFalse() {

    final var status =
        new OfficeWorkerPoolStatus(
            List.of(worker(OfficeWorkerState.STARTING), worker(OfficeWorkerState.RESTARTING)), 2);

    assertThat(status.isRunning()).isFalse();
    assertThat(status.count(OfficeWorkerState.STARTING)).isEqualTo(1);
    assertThat(status.count(OfficeWorkerState.READY)).isZero();
  }

  @Test
  void isRunning_WhenAWorkerIsBusy_ShouldReturnTrue() {

    final var status =
        new OfficeWorkerPoolStatus(
            List.of(worker(OfficeWorkerState.RESTARTING), worker(OfficeWorkerState.BUSY)), 0);

    assertThat(status.isRunning()).isTrue();
  }

  @Test
  void new_ShouldCopyTheWorkers() {

    final var workers = new ArrayList<>(List.of(worker(OfficeWorkerState.READY)));
    final var status = new OfficeWorkerPoolStatus(workers, 0);

    workers.clear();

    assertThat(status.workers()).hasSize(1);
  }
}
