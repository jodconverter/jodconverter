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

package org.jodconverter.remote.task;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.job.SourceDocumentSpecsFromFile;
import org.jodconverter.core.office.OfficeContext;

/** Contains tests for the {@link AbstractRemoteOfficeTask} class. */
public class AbstractRemoteOfficeTaskTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  @Test
  public void toString_AsExpected() {

    final SourceDocumentSpecsFromFile source = new SourceDocumentSpecsFromFile(SOURCE_FILE);
    final AbstractRemoteOfficeTask obj =
        new AbstractRemoteOfficeTask(source) {
          @Override
          @SuppressWarnings("NullableProblems")
          public void execute(final OfficeContext context) {
            // Nothing...
          }
        };
    Assertions.assertThat(obj.toString()).contains("test.txt");
  }
}
