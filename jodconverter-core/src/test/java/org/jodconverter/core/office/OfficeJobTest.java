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

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.task.OfficeTask;

/** Contains tests for the {@link OfficeJob} class. */
class OfficeJobTest {

  private static final OfficeTask TASK = context -> {};

  @Test
  void new_ShouldBeWaiting() {

    final OfficeJob job = new OfficeJob(TASK);

    assertThat(job.getTask()).isSameAs(TASK);
    assertThat(job.getFuture()).isNotDone();
    assertThat(job.getRunner()).isNull();
    // A job that is waiting is not running.
    assertThat(job.tryEndRunning()).isFalse();
  }

  @Test
  void tryStart_ShouldSucceedOnlyOnceAndOnlyWhileWaiting() {

    final OfficeWorkerRunner runner = new OfficeWorkerRunner(null, new FakeOfficeWorker(), true);
    final OfficeJob job = new OfficeJob(TASK);

    assertThat(job.tryStart(runner)).isTrue();
    assertThat(job.getRunner()).isSameAs(runner);
    assertThat(job.tryStart(runner)).isFalse();
    // A job that is running is no longer waiting.
    assertThat(job.tryEndWaiting()).isFalse();

    assertThat(job.tryEndRunning()).isTrue();
    assertThat(job.tryEndRunning()).isFalse();
    assertThat(job.tryStart(runner)).isFalse();
  }

  @Test
  void tryEndWaiting_ShouldSucceedOnlyOnceAndPreventTheStart() {

    final OfficeJob job = new OfficeJob(TASK);

    assertThat(job.tryEndWaiting()).isTrue();
    assertThat(job.tryEndWaiting()).isFalse();
    assertThat(job.tryStart(new OfficeWorkerRunner(null, new FakeOfficeWorker(), true))).isFalse();
    assertThat(job.getRunner()).isNull();
  }

  @Test
  void setTimeout_ShouldCancelThePreviousTimeout() {

    final OfficeJob job = new OfficeJob(TASK);
    final CompletableFuture<Void> queueTimeout = new CompletableFuture<>();
    final CompletableFuture<Void> executionTimeout = new CompletableFuture<>();

    job.setTimeout(queueTimeout);
    assertThat(queueTimeout).isNotCancelled();

    job.setTimeout(executionTimeout);
    assertThat(queueTimeout).isCancelled();
    assertThat(executionTimeout).isNotCancelled();
  }

  @Test
  void end_ShouldCancelTheTimeout() {

    final OfficeJob waiting = new OfficeJob(TASK);
    final CompletableFuture<Void> queueTimeout = new CompletableFuture<>();
    waiting.setTimeout(queueTimeout);
    waiting.tryEndWaiting();
    assertThat(queueTimeout).isCancelled();

    final OfficeJob running = new OfficeJob(TASK);
    final CompletableFuture<Void> executionTimeout = new CompletableFuture<>();
    running.tryStart(new OfficeWorkerRunner(null, new FakeOfficeWorker(), true));
    running.setTimeout(executionTimeout);
    running.tryEndRunning();
    assertThat(executionTimeout).isCancelled();
  }
}
