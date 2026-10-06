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

package org.jodconverter.core.pdf;

import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Options for the encryption and the permissions of a PDF document. Supported by all the versions
 * of LibreOffice and by Apache OpenOffice.
 *
 * <p>The permissions ({@link #printing(Printing)}, {@link #changes(Changes)}, {@link
 * #copying(boolean)} and {@link #accessibilityAccess(boolean)}) require a {@link
 * #permissionPassword(String) permission password}. PDF/A does not allow encryption, so none of
 * these options can be used with a PDF/A {@link PdfVersion version}.
 *
 * @see PdfOptions.Builder#security(java.util.function.Consumer)
 */
public final class PdfSecurityOptions extends AbstractPdfOptionGroup {

  /** The printing allowed for a PDF document. */
  public enum Printing {
    /** The document cannot be printed. */
    NONE(0),

    /** The document can only be printed in low resolution. */
    LOW_RESOLUTION(1),

    /** The document can be printed in high resolution. */
    HIGH_RESOLUTION(2);

    private final int value;

    Printing(final int value) {
      this.value = value;
    }
  }

  /** The changes allowed to a PDF document. */
  public enum Changes {
    /** The document cannot be changed. */
    NONE(0),

    /** Pages can be inserted, deleted and rotated. */
    PAGES(1),

    /** Form fields can be filled in. */
    FORMS(2),

    /** Form fields can be filled in, and comments can be added. */
    FORMS_AND_COMMENTS(3),

    /** Everything can be changed, except extracting pages. */
    ALL_EXCEPT_EXTRACTION(4);

    private final int value;

    Changes(final int value) {
      this.value = value;
    }
  }

  /* default */ PdfSecurityOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Encrypts the document with a password required to open it.
   *
   * <p>FilterData: {@code EncryptFile} set to {@code true}, and {@code DocumentOpenPassword}.
   *
   * @param password The password required to open the document. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSecurityOptions openPassword(final @Nullable String password) {
    setText(PdfOption.DOCUMENT_OPEN_PASSWORD, password);
    set(PdfOption.ENCRYPT_FILE, password == null ? null : Boolean.TRUE);
    return this;
  }

  /**
   * Restricts what can be done with the document, with a password required to change these
   * permissions.
   *
   * <p>FilterData: {@code RestrictPermissions} set to {@code true}, and {@code PermissionPassword}.
   *
   * @param password The password required to change the permissions. {@code null} removes the
   *     option.
   * @return This group.
   */
  public @NonNull PdfSecurityOptions permissionPassword(final @Nullable String password) {
    setText(PdfOption.PERMISSION_PASSWORD, password);
    set(PdfOption.RESTRICT_PERMISSIONS, password == null ? null : Boolean.TRUE);
    return this;
  }

  /**
   * Specifies the printing that is allowed.
   *
   * <p>FilterData: {@code Printing}.
   *
   * @param printing The printing allowed.
   * @return This group.
   */
  public @NonNull PdfSecurityOptions printing(final @NonNull Printing printing) {
    Objects.requireNonNull(printing, "printing must not be null");
    set(PdfOption.PRINTING, printing.value);
    return this;
  }

  /**
   * Specifies the changes that are allowed.
   *
   * <p>FilterData: {@code Changes}.
   *
   * @param changes The changes allowed.
   * @return This group.
   */
  public @NonNull PdfSecurityOptions changes(final @NonNull Changes changes) {
    Objects.requireNonNull(changes, "changes must not be null");
    set(PdfOption.CHANGES, changes.value);
    return this;
  }

  /**
   * Specifies whether the content of the document can be copied.
   *
   * <p>FilterData: {@code EnableCopyingOfContent}.
   *
   * @param allow {@code true} to allow copying the content.
   * @return This group.
   */
  public @NonNull PdfSecurityOptions copying(final boolean allow) {
    set(PdfOption.ENABLE_COPYING_OF_CONTENT, allow);
    return this;
  }

  /**
   * Specifies whether the accessibility tools, such as screen readers, can read the text of the
   * document.
   *
   * <p>FilterData: {@code EnableTextAccessForAccessibilityTools}.
   *
   * @param allow {@code true} to allow the accessibility tools to read the text.
   * @return This group.
   */
  public @NonNull PdfSecurityOptions accessibilityAccess(final boolean allow) {
    set(PdfOption.ENABLE_TEXT_ACCESS_FOR_ACCESSIBILITY_TOOLS, allow);
    return this;
  }
}
