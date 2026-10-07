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

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.local.task.PageImagesTask;

/**
 * The export of the pages of a presentation or a drawing as images, one image per slide or draw
 * page, created by {@link LocalConverter#exportPages(File)}.
 *
 * <pre>
 * List&lt;File&gt; images =
 *     converter
 *         .exportPages(new File("deck.pptx"))
 *         .to(new File("out"))
 *         .as(ImageFormat.PNG)
 *         .size(1920, 1080)
 *         .execute();
 * </pre>
 *
 * <p>The images are named after the source document and the number of the page in the document,
 * {@code deck-01.png}, {@code deck-02.png}... The filters of the converter are applied to the
 * document before the export.
 */
public final class PageImagesJob {

  /** The default image format. */
  public static final ImageFormat DEFAULT_FORMAT = ImageFormat.PNG;

  /** The default quality of a lossy format. */
  public static final int DEFAULT_QUALITY = 90;

  private final SourceDocumentSpecs source;
  private final OfficeManager officeManager;
  private final Function<PageImagesJob, PageImagesTask> taskFactory;

  private File directory;
  private String baseName;
  private ImageFormat format = DEFAULT_FORMAT;
  private int width;
  private int height;
  private int quality = DEFAULT_QUALITY;
  private String pages;
  private boolean hiddenSlides;

  /* default */ PageImagesJob(
      final SourceDocumentSpecs source,
      final String baseName,
      final OfficeManager officeManager,
      final Function<PageImagesJob, PageImagesTask> taskFactory) {
    this.source = source;
    this.baseName = baseName;
    this.officeManager = officeManager;
    this.taskFactory = taskFactory;
  }

  /**
   * Specifies the directory where the images are written; it is created if missing.
   *
   * @param directory The directory.
   * @return This job.
   */
  public @NonNull PageImagesJob to(final @NonNull File directory) {
    Objects.requireNonNull(directory, "directory must not be null");
    this.directory = directory;
    return this;
  }

  /**
   * Specifies the start of the names of the images, followed by the page number and the extension
   * of the format ({@code name-01.png}). The default is the name of the source file without its
   * extension, or {@code page} for a document given as a stream.
   *
   * @param baseName The base name.
   * @return This job.
   */
  public @NonNull PageImagesJob baseName(final @NonNull String baseName) {
    AssertUtils.notBlank(baseName, "baseName must not be null nor blank");
    this.baseName = baseName;
    return this;
  }

  /**
   * Specifies the image format; PNG by default.
   *
   * @param format The format.
   * @return This job.
   */
  public @NonNull PageImagesJob as(final @NonNull ImageFormat format) {
    Objects.requireNonNull(format, "format must not be null");
    this.format = format;
    return this;
  }

  /**
   * Specifies the size of the images, in pixels. Without a size, the images have the size of the
   * page at 96 dpi.
   *
   * @param width The width.
   * @param height The height.
   * @return This job.
   */
  public @NonNull PageImagesJob size(final int width, final int height) {
    AssertUtils.isTrue(width > 0, "width must be greater than 0");
    AssertUtils.isTrue(height > 0, "height must be greater than 0");
    this.width = width;
    this.height = height;
    return this;
  }

  /**
   * Specifies the width of the images, in pixels; the height follows the proportions of the page.
   *
   * @param width The width.
   * @return This job.
   */
  public @NonNull PageImagesJob width(final int width) {
    AssertUtils.isTrue(width > 0, "width must be greater than 0");
    this.width = width;
    this.height = 0;
    return this;
  }

  /**
   * Specifies the height of the images, in pixels; the width follows the proportions of the page.
   *
   * @param height The height.
   * @return This job.
   */
  public @NonNull PageImagesJob height(final int height) {
    AssertUtils.isTrue(height > 0, "height must be greater than 0");
    this.width = 0;
    this.height = height;
    return this;
  }

  /**
   * Specifies the quality of a lossy format (JPEG, WebP), from 1 to 100; 90 by default. Ignored by
   * the other formats.
   *
   * @param quality The quality.
   * @return This job.
   */
  public @NonNull PageImagesJob quality(final int quality) {
    AssertUtils.isTrue(quality >= 1 && quality <= 100, "quality must be between 1 and 100");
    this.quality = quality;
    return this;
  }

  /**
   * Specifies the pages to export, as a range such as {@code 1-3,7}; all of them by default. A page
   * beyond the last one fails the export.
   *
   * @param pages The range, or null for all the pages.
   * @return This job.
   * @throws IllegalArgumentException If the range is not valid.
   */
  public @NonNull PageImagesJob pages(final @Nullable String pages) {
    if (pages != null) {
      PageImagesTask.parseRange(pages);
    }
    this.pages = pages;
    return this;
  }

  /**
   * Specifies whether the hidden slides of a presentation are exported too; they are skipped by
   * default. The images keep the number of the page in the document, so the names of skipped slides
   * are missing from the sequence.
   *
   * @param hiddenSlides Whether the hidden slides are exported.
   * @return This job.
   */
  public @NonNull PageImagesJob hiddenSlides(final boolean hiddenSlides) {
    this.hiddenSlides = hiddenSlides;
    return this;
  }

  /**
   * Exports the pages, and waits for the export to be done.
   *
   * @return The image files, in the order of the pages.
   * @throws OfficeException If the export fails, or if the document is not a presentation nor a
   *     drawing.
   * @throws IllegalStateException If the directory was not given.
   */
  public @NonNull List<@NonNull File> execute() throws OfficeException {
    final var task = task();
    officeManager.execute(task);
    return task.getImages();
  }

  /**
   * Exports the pages without waiting. The future completes with the image files, in the order of
   * the pages, or exceptionally with an {@link OfficeException}.
   *
   * @return The future of the export.
   * @throws IllegalStateException If the directory was not given.
   */
  public @NonNull CompletableFuture<@NonNull List<@NonNull File>> executeAsync() {
    final var task = task();
    return officeManager.submit(task).thenApply(nothing -> task.getImages());
  }

  private PageImagesTask task() {
    if (directory == null) {
      throw new IllegalStateException("The directory of the images is required: to(directory)");
    }
    return taskFactory.apply(this);
  }

  /* default */ SourceDocumentSpecs getSource() {
    return source;
  }

  /* default */ File getDirectory() {
    return directory;
  }

  /* default */ String getBaseName() {
    return baseName;
  }

  /* default */ ImageFormat getFormat() {
    return format;
  }

  /* default */ int getWidth() {
    return width;
  }

  /* default */ int getHeight() {
    return height;
  }

  /* default */ int getQuality() {
    return quality;
  }

  /* default */ @Nullable String getPages() {
    return pages;
  }

  /* default */ boolean isHiddenSlides() {
    return hiddenSlides;
  }
}
