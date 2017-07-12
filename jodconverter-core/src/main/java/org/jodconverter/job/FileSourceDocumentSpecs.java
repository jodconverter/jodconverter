/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.job;

import java.io.File;

import org.apache.commons.lang3.Validate;

import org.jodconverter.document.DocumentFormat;

public class FileSourceDocumentSpecs extends AbstractFileDocumentSpecs
    implements SourceDocumentSpecs {

  /**
   * Creates a new {@link FileSourceDocumentSpecs} using the specified {@link File}.
   *
   * @param file The {@link file} of this specification.
   * @return A created {@link FileSourceDocumentSpecs}.
   */
  public static FileSourceDocumentSpecs make(final File file) {
    return builder().file(file).build();
  }

  /**
   * Creates a new {@link FileSourceDocumentSpecs} using the specified {@link File} and {@link
   * DocumentFormat}.
   *
   * @param file The {@link file} of this specification.
   * @param format The {@link DocumentFormat} of the specified file.
   * @return A created {@link FileSourceDocumentSpecs}.
   */
  public static FileSourceDocumentSpecs make(final File file, final DocumentFormat format) {
    return builder().file(file).format(format).build();
  }

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  private FileSourceDocumentSpecs(final File file, final DocumentFormat documentFormat) {
    super(file, documentFormat);
  }

  @Override
  public void onConsumed(File file) {
    // Do nothing
  }

  /** A builder for constructing a {@link FileTargetDocumentSpecs}. */
  public static final class Builder
      extends AbstractFileDocumentSpecs.AbstractFileDocumentSpecsBuilder<Builder> {

    // Private ctor so only FileTargetDocumentSpecs can create an instance of this builder.
    private Builder() {}

    @Override
    public Builder file(File file) {

      Validate.notNull(file, "The file is null");
      Validate.isTrue(file.exists(), "File not found: %s", file);
      return super.file(file);
    }

    /**
     * Creates the specs that is specified by this builder.
     *
     * @return The specs that is specified by this builder.
     */
    public FileSourceDocumentSpecs build() {

      // Create the converter
      return new FileSourceDocumentSpecs(file, documentFormat);
    }
  }
}
