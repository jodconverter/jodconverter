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

/**
 * Base class for all document specifications implementations providing a file on disk (no temporary
 * file is created).
 */
abstract class AbstractFileDocumentSpecs extends AbstractDocumentSpecs {

  private File file;

  protected AbstractFileDocumentSpecs(File file, DocumentFormat documentFormat) {
    super(documentFormat);

    this.file = file;
  }

  @Override
  public File getFile() {
    return file;
  }

  abstract static class AbstractFileDocumentSpecsBuilder<
          T extends AbstractFileDocumentSpecsBuilder<T>>
      extends AbstractDocumentSpecsBuilder<T> {

    protected File file;

    /**
     * Specifies a path to the file of this builder.
     *
     * @param filePath path to the file.
     * @return This builder instance.
     */
    public T file(String filePath) {

      Validate.notBlank(filePath, "The filePath is blank");
      return file(new File(filePath));
    }

    /**
     * Specifies a the file of this builder.
     *
     * @param file the file.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T file(File file) {

      Validate.notNull(file, "The file is null");
      Validate.isTrue(file.exists(), "File not found: %s", file);
      this.file = file;
      return (T) this;
    }
  }
}
