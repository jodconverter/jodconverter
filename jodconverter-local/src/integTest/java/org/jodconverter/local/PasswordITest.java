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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.PasswordProtectedException;

/**
 * Contains tests for the conversion of password-protected documents, with a real office.
 *
 * <p>The protected documents are all made before the tests, and the tests with a wrong password run
 * last: LibreOffice 26.2 on Windows was seen crashing when it encrypted a document after it had
 * refused a wrong password.
 */
@ExtendWith(LocalOfficeManagerExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class PasswordITest {

  private static final String PASSWORD = "s3cret";

  @TempDir static File folder;

  private static File protectedOdt;
  private static File protectedDocx;
  private static File protectedOdp;

  // The protected documents are made by the office itself: the Password store property encrypts
  // the output.
  @BeforeAll
  static void makeProtectedDocuments(final OfficeManager manager) throws OfficeException {

    final var converter =
        LocalConverter.builder().officeManager(manager).storeProperty("Password", PASSWORD).build();
    protectedOdt = new File(folder, "protected.odt");
    converter.convert(documentFile("test.txt")).to(protectedOdt).execute();
    protectedDocx = new File(folder, "protected.docx");
    converter.convert(documentFile("test.txt")).to(protectedDocx).execute();
    protectedOdp = new File(folder, "protected.odp");
    converter.convert(documentFile("test.odp")).to(protectedOdp).execute();
  }

  private static String text(final File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.UTF_8);
  }

  @Test
  @Order(1)
  void withTheRightPassword_ShouldConvert(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var odt = new File(testFolder, "odt.txt");
    final var docx = new File(testFolder, "docx.txt");

    LocalConverter.make(manager).convert(protectedOdt).to(odt).password(PASSWORD).execute();
    LocalConverter.make(manager).convert(protectedDocx).to(docx).password(PASSWORD).execute();

    assertThat(text(odt)).contains("Test document");
    assertThat(text(docx)).contains("Test document");
  }

  @Test
  @Order(3)
  void withoutPassword_ShouldThrowPasswordProtectedException(
      final @TempDir File testFolder, final OfficeManager manager) {

    final var target = new File(testFolder, "out.txt");

    assertThatExceptionOfType(PasswordProtectedException.class)
        .isThrownBy(() -> LocalConverter.make(manager).convert(protectedOdt).to(target).execute())
        .withMessageStartingWith("Document password requested for");
  }

  @Test
  @Order(4)
  void withAWrongPassword_ShouldThrowPasswordProtectedException(
      final @TempDir File testFolder, final OfficeManager manager) {

    for (final var source : List.of(protectedOdt, protectedDocx)) {
      final var target = new File(testFolder, "out.txt");

      assertThatExceptionOfType(PasswordProtectedException.class)
          .isThrownBy(
              () ->
                  LocalConverter.make(manager)
                      .convert(source)
                      .to(target)
                      .password("wrong")
                      .execute())
          .withMessage("Wrong password for " + source.getName());
    }
  }

  @Test
  @Order(2)
  void exportPages_WithThePassword_ShouldExport(
      final @TempDir File testFolder, final OfficeManager manager) throws OfficeException {

    final var images =
        LocalConverter.make(manager)
            .exportPages(protectedOdp)
            .to(testFolder)
            .password(PASSWORD)
            .execute();

    assertThat(images).isNotEmpty().allSatisfy(image -> assertThat(image).isFile());
  }
}
