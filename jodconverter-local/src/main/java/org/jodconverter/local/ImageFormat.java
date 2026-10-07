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

import java.util.Locale;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.util.AssertUtils;

/**
 * An image format of the graphic export filter of the office, used to export the pages of a
 * document as images with {@link LocalConverter#exportPages(java.io.File)}.
 *
 * @param mediaType The media type the office filter exports, such as {@code image/png}.
 * @param extension The extension of the image files, without the dot.
 * @param lossy Whether the format has a quality setting (JPEG, WebP).
 */
public record ImageFormat(@NonNull String mediaType, @NonNull String extension, boolean lossy) {

  /** Portable Network Graphics. */
  public static final ImageFormat PNG = new ImageFormat("image/png", "png", false);

  /** JPEG, with a quality. */
  public static final ImageFormat JPEG = new ImageFormat("image/jpeg", "jpg", true);

  /** Scalable Vector Graphics: the vector drawing of the page, with the text as text. */
  public static final ImageFormat SVG = new ImageFormat("image/svg+xml", "svg", false);

  /** Graphics Interchange Format. */
  public static final ImageFormat GIF = new ImageFormat("image/gif", "gif", false);

  /** Windows bitmap. */
  public static final ImageFormat BMP = new ImageFormat("image/bmp", "bmp", false);

  /** Tagged Image File Format. */
  public static final ImageFormat TIFF = new ImageFormat("image/tiff", "tif", false);

  /** WebP, with a quality. Requires LibreOffice 7.4 or later. */
  public static final ImageFormat WEBP = new ImageFormat("image/webp", "webp", true);

  /**
   * Creates an image format.
   *
   * @param mediaType The media type the office filter exports, such as {@code image/png}.
   * @param extension The extension of the image files, without the dot.
   * @param lossy Whether the format has a quality setting.
   */
  public ImageFormat {
    AssertUtils.notBlank(mediaType, "mediaType must not be null nor blank");
    AssertUtils.notBlank(extension, "extension must not be null nor blank");
    mediaType = mediaType.trim().toLowerCase(Locale.ROOT);
    extension = extension.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * Gets the format of the given extension or media type: {@code png}, {@code jpg}, {@code jpeg},
   * {@code svg}, {@code gif}, {@code bmp}, {@code tif}, {@code tiff}, {@code webp}, or one of the
   * media types above.
   *
   * @param name The extension or the media type, in any case.
   * @return The format.
   * @throws IllegalArgumentException If the name is not one of the formats above.
   */
  public static @NonNull ImageFormat of(final @NonNull String name) {
    AssertUtils.notBlank(name, "name must not be null nor blank");
    final var normalized = name.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "png", "image/png" -> PNG;
      case "jpg", "jpeg", "image/jpeg" -> JPEG;
      case "svg", "image/svg+xml" -> SVG;
      case "gif", "image/gif" -> GIF;
      case "bmp", "image/bmp" -> BMP;
      case "tif", "tiff", "image/tiff" -> TIFF;
      case "webp", "image/webp" -> WEBP;
      default ->
          throw new IllegalArgumentException(
              "Unknown image format '"
                  + name
                  + "'; expected png, jpeg, svg, gif, bmp, tiff or webp, or their media type");
    };
  }
}
