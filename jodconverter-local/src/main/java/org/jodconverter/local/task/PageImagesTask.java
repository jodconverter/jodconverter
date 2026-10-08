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

package org.jodconverter.local.task;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.sun.star.beans.XPropertySet;
import com.sun.star.document.XExporter;
import com.sun.star.document.XFilter;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.lang.XComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.local.ImageFormat;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.filter.RefreshFilter;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.office.utils.Lo;

/**
 * Exports each page of a presentation or a drawing as an image, through the graphic export filter
 * of the office with a single page as the source. The images are written in a directory, named
 * after a base name and the number of the page in the document, and listed by {@link #getImages()}
 * once the task is done.
 */
public class PageImagesTask extends AbstractLocalOfficeTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(PageImagesTask.class);

  private static final String GRAPHIC_EXPORT_FILTER = "com.sun.star.drawing.GraphicExportFilter";
  private static final Pattern RANGE_PATTERN =
      Pattern.compile("\\s*(\\d+)\\s*(?:-\\s*(\\d+)\\s*)?");

  private final File directory;
  private final String baseName;
  private final ImageFormat format;
  private final int width;
  private final int height;
  private final int quality;
  private final @Nullable SortedSet<Integer> pages;
  private final boolean hiddenSlides;
  private final FilterChain filterChain;
  private final List<File> images = new ArrayList<>();

  /**
   * Creates a new task.
   *
   * @param source The source document, a presentation or a drawing.
   * @param useStreamAdapters Whether the document is loaded through a stream rather than a file.
   * @param loadProperties The properties to load the document with.
   * @param filterChain The filters applied before the export, or null.
   * @param directory The directory where the images are written; created if missing.
   * @param baseName The start of the names of the images, followed by the page number.
   * @param format The image format.
   * @param width The width of the images in pixels, or 0 to take it from the height and the size of
   *     the page, or from the page size at 96 dpi when both are 0.
   * @param height The height of the images in pixels, or 0.
   * @param quality The quality of a lossy format, 1 to 100.
   * @param pages The pages to export, as a range such as {@code 1-3,7}, or null for all of them.
   * @param hiddenSlides Whether the hidden slides of a presentation are exported too.
   */
  public PageImagesTask(
      final @NonNull SourceDocumentSpecs source,
      final boolean useStreamAdapters,
      final @Nullable Map<@NonNull String, @NonNull Object> loadProperties,
      final @Nullable FilterChain filterChain,
      final @NonNull File directory,
      final @NonNull String baseName,
      final @NonNull ImageFormat format,
      final int width,
      final int height,
      final int quality,
      final @Nullable String pages,
      final boolean hiddenSlides) {
    super(source, useStreamAdapters, loadProperties);

    Objects.requireNonNull(directory, "directory must not be null");
    AssertUtils.notBlank(baseName, "baseName must not be null nor blank");
    Objects.requireNonNull(format, "format must not be null");
    AssertUtils.isTrue(width >= 0, "width must be positive");
    AssertUtils.isTrue(height >= 0, "height must be positive");
    AssertUtils.isTrue(quality >= 1 && quality <= 100, "quality must be between 1 and 100");

    this.directory = directory;
    this.baseName = baseName;
    this.format = format;
    this.width = width;
    this.height = height;
    this.quality = quality;
    this.pages = pages == null ? null : parseRange(pages);
    this.hiddenSlides = hiddenSlides;
    this.filterChain = filterChain == null ? RefreshFilter.CHAIN : filterChain;
  }

  /**
   * Parses a range of pages, such as {@code 1-3,7}, into the numbers of the pages.
   *
   * @param range The range.
   * @return The page numbers, in order.
   * @throws IllegalArgumentException If the range is not valid.
   */
  public static @NonNull SortedSet<@NonNull Integer> parseRange(final @NonNull String range) {
    AssertUtils.notBlank(range, "range must not be null nor blank");
    final var numbers = new TreeSet<Integer>();
    for (final var part : range.split(",")) {
      final var matcher = RANGE_PATTERN.matcher(part);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(
            "Invalid page range '" + range + "'; expected pages such as 1-3,7");
      }
      final var from = Integer.parseInt(matcher.group(1));
      final var to = matcher.group(2) == null ? from : Integer.parseInt(matcher.group(2));
      if (from < 1 || to < from) {
        throw new IllegalArgumentException(
            "Invalid page range '" + range + "'; expected pages such as 1-3,7");
      }
      for (var page = from; page <= to; page++) {
        numbers.add(page);
      }
    }
    return numbers;
  }

  /**
   * Gets the images written by this task, in the order of the pages.
   *
   * @return The image files; empty until the task is done.
   */
  public @NonNull List<@NonNull File> getImages() {
    return List.copyOf(images);
  }

  @Override
  public void execute(final @NonNull OfficeContext context) throws OfficeException {

    final var localContext = (LocalOfficeContext) context;
    LOGGER.info("Executing page images task [{} -> {}]...", source, format.extension());

    if (!directory.isDirectory() && !directory.mkdirs()) {
      throw new OfficeException("Could not create the directory " + directory);
    }

    final var sourceFile = source.getFile();
    try {
      XComponent document = null;
      try {
        document = loadDocument(localContext, sourceFile);
        // A chain keeps its position, and this one is the chain of the converter: each export goes
        // through a copy, so that the filters are applied at every export, not only the first.
        filterChain.copy().doFilter(context, document);
        exportPages(localContext, document);
      } catch (OfficeException officeEx) {
        throw officeEx;
      } catch (Exception ex) {
        throw new OfficeException("Page images export failed", ex);
      } finally {
        closeDocument(document);
      }
    } finally {
      source.onConsumed(sourceFile);
    }
  }

  private void exportPages(final LocalOfficeContext context, final XComponent document)
      throws Exception {

    // A text document also has draw pages (its drawing layer): only the presentations and the
    // drawings are exported.
    final var family = LocalOfficeUtils.getDocumentFamilySilently(document);
    if (family != DocumentFamily.PRESENTATION && family != DocumentFamily.DRAWING) {
      throw new OfficeException(
          "Only presentations and drawings can be exported as images, not a "
              + (family == null
                  ? "document of unknown type"
                  : family.name().toLowerCase(Locale.ROOT) + " document"));
    }
    final var drawPages = Lo.qi(XDrawPagesSupplier.class, document).getDrawPages();
    final var pageCount = drawPages.getCount();
    if (pages != null && pages.last() > pageCount) {
      throw new OfficeException(
          "Page " + pages.last() + " does not exist: the document has " + pageCount + " pages");
    }

    // The page numbers are padded to the width of the page count, so that the files sort.
    final var nameFormat = "%s-%0" + String.valueOf(pageCount).length() + "d.%s";
    final var filter =
        Objects.requireNonNull(
                context.getServiceManager(), "Context service manager must not be null")
            .createInstanceWithContext(GRAPHIC_EXPORT_FILTER, context.getComponentContext());
    final var exporter = Lo.qi(XExporter.class, filter);
    final var xfilter = Lo.qi(XFilter.class, filter);

    for (var i = 0; i < pageCount; i++) {
      final var number = i + 1;
      if (pages != null && !pages.contains(number)) {
        continue;
      }
      final var page = Lo.qi(XDrawPage.class, drawPages.getByIndex(i));
      if (!hiddenSlides && isHidden(page)) {
        LOGGER.debug("Skipping the hidden slide {}", number);
        continue;
      }
      final var image =
          new File(directory, String.format(nameFormat, baseName, number, format.extension()));
      exporter.setSourceDocument(Lo.qi(XComponent.class, page));
      if (!xfilter.filter(LocalOfficeUtils.toUnoProperties(exportProperties(page, image)))) {
        throw new OfficeException("Could not export the page " + number + " as " + image);
      }
      images.add(image);
    }
    LOGGER.debug("{} page(s) exported as images in {}", images.size(), directory);
  }

  // The export properties: the target, the media type and the filter data (size, quality).
  private Map<String, Object> exportProperties(final XDrawPage page, final File image)
      throws Exception {

    final var filterData = new LinkedHashMap<String, Object>();
    if (width > 0 || height > 0) {
      var pixelWidth = width;
      var pixelHeight = height;
      if (pixelWidth == 0 || pixelHeight == 0) {
        // The missing dimension keeps the proportions of the page (sizes in 1/100 mm).
        final var properties = Lo.qi(XPropertySet.class, page);
        final var pageWidth = (Integer) properties.getPropertyValue("Width");
        final var pageHeight = (Integer) properties.getPropertyValue("Height");
        if (pixelWidth == 0) {
          pixelWidth = Math.max(1, Math.round((float) pixelHeight * pageWidth / pageHeight));
        } else {
          pixelHeight = Math.max(1, Math.round((float) pixelWidth * pageHeight / pageWidth));
        }
      }
      filterData.put("PixelWidth", pixelWidth);
      filterData.put("PixelHeight", pixelHeight);
    }
    if (format.lossy()) {
      filterData.put("Quality", quality);
    }

    final var properties = new LinkedHashMap<String, Object>();
    properties.put("URL", LocalOfficeUtils.toUrl(image));
    properties.put("MediaType", format.mediaType());
    properties.put("Overwrite", true);
    if (!filterData.isEmpty()) {
      properties.put("FilterData", filterData);
    }
    return properties;
  }

  // A hidden slide of a presentation; a draw page has no such property.
  private static boolean isHidden(final XDrawPage page) throws Exception {
    final var properties = Lo.qi(XPropertySet.class, page);
    return properties.getPropertySetInfo().hasPropertyByName("Visible")
        && Boolean.FALSE.equals(properties.getPropertyValue("Visible"));
  }

  @Override
  public @NonNull String toString() {
    return getClass().getSimpleName()
        + "{"
        + "source="
        + source
        + ", directory="
        + directory
        + ", baseName="
        + baseName
        + ", format="
        + format.extension()
        + ", width="
        + width
        + ", height="
        + height
        + ", quality="
        + quality
        + ", pages="
        + pages
        + ", hiddenSlides="
        + hiddenSlides
        + '}';
  }
}
