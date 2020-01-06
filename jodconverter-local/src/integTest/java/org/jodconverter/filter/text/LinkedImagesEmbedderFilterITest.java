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

package org.jodconverter.filter.text;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.OfficeException;

public class LinkedImagesEmbedderFilterITest extends AbstractOfficeITest {

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void doFilter_DoesNotThrowAnyException() throws OfficeException {

    assertThatCode(
            () -> {
              LocalConverter.builder()
                  .filterChain(new LinkedImagesEmbedderFilter())
                  .build()
                  .convert(new File(DOCUMENTS_DIR, "test_with_linked_images.odt"))
                  .to(new File(testFolder.getRoot(), "test_with_linked_images.odt"))
                  .execute();
            })
        .doesNotThrowAnyException();

    // TODO: Check if all images are now embedded.
  }
}
