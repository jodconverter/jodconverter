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
import static org.jodconverter.local.ResourceUtil.imageFile;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

@ExtendWith(LocalOfficeManagerExtension.class)
public class GraphicInserterFilterITest {

  private static final String SOURCE_FILENAME = "test.doc";
  private static final File SOURCE_FILE = documentFile(SOURCE_FILENAME);
  private static final String MULTI_PAGE_FILENAME = "test_multi_page.doc";
  private static final File MULTI_PAGE_FILE = documentFile(MULTI_PAGE_FILENAME);
  private static final File IMAGE_FILE = imageFile("sample-1.jpg");

  /** Test the conversion of a document inserting a graphic along the way on the second page. */
  @Test
  public void doFilter_WithCustomizedProperties(@TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, MULTI_PAGE_FILENAME + ".pdf");

    // Create the properties of the filter
    final Map<String, Object> props =
        GraphicInserterFilter.createDefaultShapeProperties(
            50, // Horizontal Position, 5 CM
            100 // Vertical Position, 10 CM
            );

    // Add a special property to add the image on the second page
    props.put("AnchorPageNo", (short) 2);

    assertThatCode(
            () -> {
              // Create the GraphicInserterFilter to test.
              final GraphicInserterFilter filter =
                  new GraphicInserterFilter(IMAGE_FILE.getPath(), props);
              // Convert to PDF
              LocalConverter.builder()
                  .officeManager(manager)
                  .filterChain(filter)
                  .build()
                  .convert(MULTI_PAGE_FILE)
                  .to(targetFile)
                  .execute();
            })
        .doesNotThrowAnyException();
  }

  /** Test the conversion of a document inserting a graphic along the way. */
  @Test
  public void doFilter_WithDefaultProperties(@TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".originalsize.pdf");

    assertThatCode(
            () -> {
              // Create the GraphicInserterFilter to test.
              final GraphicInserterFilter filter =
                  new GraphicInserterFilter(
                      IMAGE_FILE.getPath(),
                      50, // Horizontal Position // 5 CM
                      100); // Vertical Position // 10 CM

              // Convert to PDF
              LocalConverter.builder()
                  .officeManager(manager)
                  .filterChain(filter)
                  .build()
                  .convert(SOURCE_FILE)
                  .to(targetFile)
                  .execute();
            })
        .doesNotThrowAnyException();
  }

  /**
   * Test the conversion of a document inserting a graphic along the way. The image will be resize
   * (smaller).
   */
  @Test
  public void doFilter_WithDefaultPropertiesAndSmallerSize(
      @TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".smallersize.pdf");

    assertThatCode(
            () -> {
              // Create the GraphicInserterFilter to test.
              final GraphicInserterFilter filter =
                  new GraphicInserterFilter(
                      IMAGE_FILE.getPath(),
                      74, // Image Width // 7.4 CM (half the original size)
                      56, // Image Height // 5.6 CM (half the original size)
                      30, // Horizontal Position // 3 CM
                      50); // Vertical Position // 5 CM

              // Convert to PDF
              LocalConverter.builder()
                  .officeManager(manager)
                  .filterChain(filter)
                  .build()
                  .convert(SOURCE_FILE)
                  .to(targetFile)
                  .execute();
            })
        .doesNotThrowAnyException();
  }

  /**
   * Test the conversion of a document inserting a graphic along the way on the second page. The
   * image will be resize (smaller).
   */
  @Test
  public void doFilter_WithCustomizedPropertiesAndSmallerSize(
      @TempDir File testFolder, OfficeManager manager) {

    final File targetFile = new File(testFolder, MULTI_PAGE_FILENAME + ".smallersize.pdf");

    // Create the properties of the filter
    final Map<String, Object> props =
        GraphicInserterFilter.createDefaultShapeProperties(
            50, // Horizontal Position, 5 CM
            100 // Vertical Position, 10 CM
            );

    // Add a special property to add the image on the second page
    props.put("AnchorPageNo", (short) 2);

    assertThatCode(
            () -> {
              // Create the GraphicInserterFilter to test.
              final GraphicInserterFilter filter =
                  new GraphicInserterFilter(
                      IMAGE_FILE.getPath(),
                      74, // Image Width // 7.4 CM (half the original size)
                      56, // Image Height // 5.6 CM (half the original size)
                      props);

              // Convert to PDF
              LocalConverter.builder()
                  .officeManager(manager)
                  .filterChain(filter)
                  .build()
                  .convert(MULTI_PAGE_FILE)
                  .to(targetFile)
                  .execute();
            })
        .doesNotThrowAnyException();
  }
}
