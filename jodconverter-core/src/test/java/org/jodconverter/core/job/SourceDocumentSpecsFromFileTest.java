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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.io.File;

import org.junit.jupiter.api.Test;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;

public class SourceDocumentSpecsFromFileTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";
  private static final String BAD_SOURCE_FILE = "src/test/resources/documents/unexisting_file.txt";

  public void ctor_WithNullFile_ThrowsNullPointerException() {

    assertThatNullPointerException().isThrownBy(() -> new SourceDocumentSpecsFromFile(null));
  }

  public void ctor_WithUnexistingFile_ThrowsIllegalArgumentsException() {

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new SourceDocumentSpecsFromFile(new File(BAD_SOURCE_FILE)));
  }

  @Test
  public void ctor_WithValidValues_SpecsCreatedWithExpectedValues() {

    final File sourceFile = new File(SOURCE_FILE);
    final SourceDocumentSpecsFromFile specs = new SourceDocumentSpecsFromFile(sourceFile);
    specs.setDocumentFormat(DefaultDocumentFormatRegistry.ODS);

    assertThat(specs)
        .extracting("file", "documentFormat")
        .containsExactly(sourceFile, DefaultDocumentFormatRegistry.ODS);
  }
}
