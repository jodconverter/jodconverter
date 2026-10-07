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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.filter.PagesSelectorFilter;

/**
 * Contains tests for the export of the pages of a presentation or a drawing as images, with a real
 * office installation.
 */
@ExtendWith(LocalOfficeManagerExtension.class)
class PageImagesITest {

  // Three slides, the second one hidden.
  private static final File IMPRESS_FILE = documentFile("/pdf-options/impress.fodp");
  private static final File DRAW_FILE = documentFile("test.fodg");
  private static final File WRITER_FILE = documentFile("/pdf-options/writer.fodt");

  private static BufferedImage read(final File image) throws IOException {
    final var read = ImageIO.read(image);
    assertThat(read).as("%s", image).isNotNull();
    return read;
  }

  @Test
  void withDefaults_ShouldExportTheVisibleSlidesAsPng(
      final @TempDir File testFolder, final OfficeManager manager) throws OfficeException {

    final var images =
        LocalConverter.make(manager).exportPages(IMPRESS_FILE).to(testFolder).execute();

    // The hidden slide is skipped, and the numbers are those of the pages.
    assertThat(images).extracting(File::getName).containsExactly("impress-1.png", "impress-3.png");
    assertThat(images).allSatisfy(image -> assertThat(image).isFile());
  }

  @Test
  void withHiddenSlides_ShouldExportEverySlide(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var images =
        LocalConverter.make(manager)
            .exportPages(IMPRESS_FILE)
            .to(testFolder)
            .hiddenSlides(true)
            .execute();

    assertThat(images)
        .extracting(File::getName)
        .containsExactly("impress-1.png", "impress-2.png", "impress-3.png");
    // Without a size, the image has the size of the page at 96 dpi (a 28 x 15.75 cm slide).
    final var image = read(images.get(0));
    assertThat(image.getWidth()).isBetween(1000, 1100);
    assertThat(image.getHeight()).isBetween(560, 620);
  }

  @Test
  void withSizeAndFormats_ShouldExportThatSize(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var converter = LocalConverter.make(manager);

    final var png =
        converter.exportPages(IMPRESS_FILE).to(testFolder).size(640, 480).pages("1").execute();
    assertThat(png).extracting(File::getName).containsExactly("impress-1.png");
    assertThat(read(png.get(0)).getWidth()).isEqualTo(640);
    assertThat(read(png.get(0)).getHeight()).isEqualTo(480);

    // The height follows the proportions of the page (16:9).
    final var wide =
        converter
            .exportPages(IMPRESS_FILE)
            .to(new File(testFolder, "wide"))
            .width(1600)
            .pages("1")
            .execute();
    assertThat(read(wide.get(0)).getWidth()).isEqualTo(1600);
    assertThat(read(wide.get(0)).getHeight()).isEqualTo(900);

    final var tall =
        converter
            .exportPages(IMPRESS_FILE)
            .to(new File(testFolder, "tall"))
            .height(450)
            .pages("1")
            .execute();
    assertThat(read(tall.get(0)).getWidth()).isEqualTo(800);
    assertThat(read(tall.get(0)).getHeight()).isEqualTo(450);

    final var jpeg =
        converter
            .exportPages(IMPRESS_FILE)
            .to(testFolder)
            .as(ImageFormat.JPEG)
            .size(640, 480)
            .quality(50)
            .pages("1")
            .execute();
    assertThat(jpeg).extracting(File::getName).containsExactly("impress-1.jpg");
    assertThat(read(jpeg.get(0)).getWidth()).isEqualTo(640);
    final var jpegHighQuality =
        converter
            .exportPages(IMPRESS_FILE)
            .to(new File(testFolder, "hq"))
            .as(ImageFormat.JPEG)
            .size(640, 480)
            .quality(100)
            .pages("1")
            .execute();
    assertThat(jpegHighQuality.get(0).length()).isGreaterThan(jpeg.get(0).length());

    final var svg =
        converter.exportPages(IMPRESS_FILE).to(testFolder).as(ImageFormat.SVG).execute();
    assertThat(svg).extracting(File::getName).containsExactly("impress-1.svg", "impress-3.svg");
    assertThat(Files.readString(svg.get(0).toPath(), StandardCharsets.UTF_8))
        .contains("<svg")
        .contains("SLIDEONE");
  }

  @Test
  void withPagesAndFilters_ShouldExportTheSelectedPages(
      final @TempDir File testFolder, final OfficeManager manager) throws OfficeException {

    // The range is applied to the pages of the document...
    final var range =
        LocalConverter.make(manager)
            .exportPages(IMPRESS_FILE)
            .to(testFolder)
            .pages("2-3")
            .hiddenSlides(true)
            .execute();
    assertThat(range).extracting(File::getName).containsExactly("impress-2.png", "impress-3.png");

    // ... and the filters of the converter before the export: the selector keeps the third slide,
    // which is then the only one left.
    final var filtered =
        LocalConverter.builder()
            .officeManager(manager)
            .filterChain(new PagesSelectorFilter(3))
            .build()
            .exportPages(IMPRESS_FILE)
            .to(new File(testFolder, "filtered"))
            .execute();
    assertThat(filtered).extracting(File::getName).containsExactly("impress-1.png");
  }

  @Test
  void withPageBeyondTheLast_ShouldThrowOfficeException(
      final @TempDir File testFolder, final OfficeManager manager) {

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () ->
                LocalConverter.make(manager)
                    .exportPages(IMPRESS_FILE)
                    .to(testFolder)
                    .pages("4")
                    .execute())
        .withMessage("Page 4 does not exist: the document has 3 pages");
  }

  @Test
  void withDrawing_ShouldExportThePages(final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    final var images =
        LocalConverter.make(manager).exportPages(DRAW_FILE).to(testFolder).width(420).execute();

    // An A4 page: the height follows.
    assertThat(images).extracting(File::getName).containsExactly("test-1.png");
    assertThat(read(images.get(0)).getWidth()).isEqualTo(420);
    assertThat(read(images.get(0)).getHeight()).isEqualTo(594);
  }

  @Test
  void withStream_ShouldNameTheImagesPage(
      final @TempDir File testFolder, final OfficeManager manager)
      throws OfficeException, IOException {

    try (var stream = new FileInputStream(IMPRESS_FILE)) {
      final var images =
          LocalConverter.make(manager)
              .exportPages(stream)
              .to(testFolder)
              .pages("1")
              .executeAsync()
              .get(2, TimeUnit.MINUTES);
      assertThat(images).extracting(File::getName).containsExactly("page-1.png");
    } catch (Exception ex) {
      throw new OfficeException("The export failed", ex);
    }

    try (var stream = new FileInputStream(IMPRESS_FILE)) {
      final var images =
          LocalConverter.make(manager)
              .exportPages(stream)
              .to(testFolder)
              .baseName("slide")
              .pages("3")
              .execute();
      assertThat(images).extracting(File::getName).containsExactly("slide-3.png");
    }
  }

  @Test
  void withTextDocument_ShouldThrowOfficeException(
      final @TempDir File testFolder, final OfficeManager manager) {

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () -> LocalConverter.make(manager).exportPages(WRITER_FILE).to(testFolder).execute())
        .withMessage(
            "Only presentations and drawings can be exported as images, not a text document");
  }

  @Test
  void withoutDirectory_ShouldThrowIllegalStateException(final OfficeManager manager) {

    assertThatIllegalStateException()
        .isThrownBy(() -> LocalConverter.make(manager).exportPages(IMPRESS_FILE).execute())
        .withMessageContaining("to(directory)");
  }
}
