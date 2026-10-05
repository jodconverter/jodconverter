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

package org.jodconverter.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.LocalOfficeManager;

/** Contains tests for the external office manager auto-configuration. */
@SpringBootTest
@TestPropertySource(locations = "classpath:config/application-external.properties")
class ExternalConverterITest {

  private static final String SOURCE_FILE_PATH = "src/integTest/resources/documents/test1.doc";

  // The office process the external manager connects to, started outside of the Spring context.
  private static OfficeManager runningOffice;

  @Autowired
  @Qualifier("externalDocumentConverter")
  private DocumentConverter converter;

  @BeforeAll
  static void startOfficeProcess() throws OfficeException {
    runningOffice = LocalOfficeManager.builder().portNumbers(2011).build();
    runningOffice.start();
  }

  @AfterAll
  static void stopOfficeProcess() throws OfficeException {
    runningOffice.stop();
  }

  @Test
  void convert_ShouldUseTheRunningOfficeProcess(final @TempDir File testFolder)
      throws OfficeException {

    final var outputFile = new File(testFolder, "out.pdf");

    converter.convert(new File(SOURCE_FILE_PATH)).to(outputFile).execute();

    assertThat(outputFile).isFile();
    assertThat(outputFile.length()).isGreaterThan(0L);
  }
}
