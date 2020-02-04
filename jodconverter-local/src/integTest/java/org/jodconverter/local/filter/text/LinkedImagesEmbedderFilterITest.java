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

package org.jodconverter.local.filter.text;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

/** Contains tests for the {@link LinkedImagesEmbedderFilter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
public class LinkedImagesEmbedderFilterITest {

  private static final File SOURCE_FILE = documentFile("test_with_linked_images.odt");

  @Test
  public void doFilter_DoesNotThrowAnyException(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, "test_with_linked_images.odt");
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new LinkedImagesEmbedderFilter())
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();

    // TODO: Check if all images are now embedded.
  }

  /**
   * Test the conversion of a document which is not a TEXT document. We can't really test the
   * result, but at least we will test the the conversion doesn't fail (filter does nothing).
   */
  @Test
  public void doFilter_WithBadDocumentType_DoNothing(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, "test_with_linked_images.badtype.pdf");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new LinkedImagesEmbedderFilter())
                    .build()
                    .convert(documentFile("test.xls"))
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();
  }
}
