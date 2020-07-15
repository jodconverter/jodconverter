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

package org.jodconverter.core.job;

import java.io.File;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.util.AssertUtils;

/**
 * Base class for all document specifications implementations.
 *
 * @see DocumentSpecs
 */
public abstract class AbstractDocumentSpecs implements DocumentSpecs {

  private File file;
  private DocumentFormat documentFormat;

  protected AbstractDocumentSpecs() {
    super();
  }

  protected AbstractDocumentSpecs(final @NonNull File file) {
    super();

    AssertUtils.notNull(file, "file must not be null");

    this.file = file;
  }

  @Override
  public @NonNull File getFile() {
    return file;
  }

  @Override
  public @Nullable DocumentFormat getFormat() {
    return documentFormat;
  }

  /**
   * Sets the {@link DocumentFormat} specification for the document.
   *
   * @param documentFormat The document format to set.
   */
  /* default */ void setDocumentFormat(final DocumentFormat documentFormat) {

    AssertUtils.notNull(documentFormat, "documentFormat must not be null");
    this.documentFormat = documentFormat;
  }

  @Override
  public @NonNull String toString() {
    return getClass().getSimpleName()
        + "{"
        + "file="
        + Optional.ofNullable(file).map(File::getName).orElse("null")
        + ", format="
        + Optional.ofNullable(documentFormat).map(DocumentFormat::getExtension).orElse("null")
        + '}';
  }
}
