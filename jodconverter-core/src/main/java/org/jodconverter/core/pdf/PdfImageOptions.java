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

package org.jodconverter.core.pdf;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.jodconverter.core.util.AssertUtils;

/**
 * Options for the images of a PDF document. Supported by all the versions of LibreOffice and by
 * Apache OpenOffice.
 *
 * @see PdfOptions.Builder#images(java.util.function.Consumer)
 */
public final class PdfImageOptions extends AbstractPdfOptionGroup {

  /* default */ PdfImageOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies whether the images are compressed without loss (as PNG) instead of as JPEG.
   *
   * <p>FilterData: {@code UseLosslessCompression}.
   *
   * @param lossless {@code true} for a lossless compression.
   * @return This group.
   */
  public @NonNull PdfImageOptions lossless(final boolean lossless) {
    set(PdfOption.USE_LOSSLESS_COMPRESSION, lossless);
    return this;
  }

  /**
   * Specifies the quality of the JPEG compression, used when the compression is not lossless.
   *
   * <p>FilterData: {@code Quality}.
   *
   * @param percent The quality, from 1 (smallest file) to 100 (best quality).
   * @return This group.
   */
  public @NonNull PdfImageOptions jpegQuality(final int percent) {
    AssertUtils.isTrue(percent >= 1 && percent <= 100, "jpegQuality must be between 1 and 100");
    set(PdfOption.QUALITY, percent);
    return this;
  }

  /**
   * Specifies whether the resolution of the images is reduced to the {@link #maxResolution(int)
   * maximum resolution}.
   *
   * <p>FilterData: {@code ReduceImageResolution}.
   *
   * @param reduce {@code true} to reduce the resolution of the images.
   * @return This group.
   */
  public @NonNull PdfImageOptions reduceResolution(final boolean reduce) {
    set(PdfOption.REDUCE_IMAGE_RESOLUTION, reduce);
    return this;
  }

  /**
   * Reduces the resolution of the images to the given maximum. The office user interface offers 75,
   * 150, 300, 600 and 1200 DPI.
   *
   * <p>FilterData: {@code ReduceImageResolution} set to {@code true}, and {@code
   * MaxImageResolution}.
   *
   * @param dpi The maximum resolution, in dots per inch.
   * @return This group.
   */
  public @NonNull PdfImageOptions maxResolution(final int dpi) {
    setAtLeast(PdfOption.MAX_IMAGE_RESOLUTION, "maxResolution", dpi, 1);
    set(PdfOption.REDUCE_IMAGE_RESOLUTION, true);
    return this;
  }
}
