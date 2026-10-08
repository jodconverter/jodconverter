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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.filter.Filter;

/**
 * Contains tests for the task retries of a {@link LocalOfficeManager}, with a real office process
 * that is killed in the middle of a conversion.
 */
class TaskRetriesITest {

  private static final File SOURCE = documentFile("test.odt");
  private static final int PORT = 2013;

  private static Set<Long> officePids() {
    return ProcessHandle.current()
        .descendants()
        .map(ProcessHandle::pid)
        .collect(Collectors.toSet());
  }

  // A filter that kills the office processes started since the given snapshot, the first time it
  // is applied, then lets the conversion go on (and fail, since its office process is gone).
  private static Filter killerOf(final Set<Long> before, final AtomicInteger applied) {
    return (context, document, chain) -> {
      if (applied.getAndIncrement() == 0) {
        ProcessHandle.current()
            .descendants()
            .filter(process -> !before.contains(process.pid()))
            .forEach(ProcessHandle::destroyForcibly);
      }
      chain.doFilter(context, document);
    };
  }

  @Test
  void withRetries_ShouldConvertAgainAfterTheOfficeProcessWasKilled(final @TempDir File testFolder)
      throws OfficeException {

    final var before = officePids();
    final var manager =
        LocalOfficeManager.builder()
            .portNumbers(PORT)
            .workingDir(testFolder)
            .taskRetries(1)
            .build();
    final var applied = new AtomicInteger();
    final var target = new File(testFolder, "out.pdf");
    try {
      manager.start();

      LocalConverter.builder()
          .officeManager(manager)
          .filterChain(killerOf(before, applied))
          .build()
          .convert(SOURCE)
          .to(target)
          .execute();

      // The conversion was executed twice, on two office processes.
      assertThat(applied).hasValue(2);
      assertThat(target).isFile().isNotEmpty();
      assertThat(manager.getStatus().workers().get(0).restarts()).isEqualTo(1);
    } finally {
      OfficeUtils.stopQuietly(manager);
    }
  }

  @Test
  void withoutRetries_ShouldFailTheConversion(final @TempDir File testFolder)
      throws OfficeException {

    final var before = officePids();
    final var manager =
        LocalOfficeManager.builder().portNumbers(PORT).workingDir(testFolder).build();
    final var applied = new AtomicInteger();
    final var target = new File(testFolder, "out.pdf");
    try {
      manager.start();
      final var converter =
          LocalConverter.builder()
              .officeManager(manager)
              .filterChain(killerOf(before, applied))
              .build();

      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(() -> converter.convert(SOURCE).to(target).execute());
      assertThat(applied).hasValue(1);

      // The office process is restarted for the next conversion.
      converter.convert(SOURCE).to(target).execute();
      assertThat(target).isFile().isNotEmpty();
    } finally {
      OfficeUtils.stopQuietly(manager);
    }
  }

  @Test
  void withRetriesAndATargetStream_ShouldFailTheConversion(final @TempDir File testFolder)
      throws OfficeException {

    final var before = officePids();
    final var manager =
        LocalOfficeManager.builder()
            .portNumbers(PORT)
            .workingDir(testFolder)
            .taskRetries(1)
            .build();
    final var applied = new AtomicInteger();
    final var output = new ByteArrayOutputStream();
    try {
      manager.start();
      final var converter =
          LocalConverter.builder()
              .officeManager(manager)
              .filterChain(killerOf(before, applied))
              .build();

      // A stream must not be written twice: the conversion is not executed again.
      assertThatExceptionOfType(OfficeException.class)
          .isThrownBy(
              () ->
                  converter
                      .convert(SOURCE)
                      .to(output)
                      .as(DefaultDocumentFormatRegistry.PDF)
                      .execute());
      assertThat(applied).hasValue(1);
      assertThat(output.size()).isZero();
    } finally {
      OfficeUtils.stopQuietly(manager);
    }
  }
}
