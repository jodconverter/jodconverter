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
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.util.AssertUtils;

/**
 * Options for a text watermark printed on each page of a PDF document.
 *
 * @see PdfOptions.Builder#watermark(java.util.function.Consumer)
 */
public final class PdfWatermarkOptions extends AbstractPdfOptionGroup {

  /* default */ PdfWatermarkOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Specifies the text of a watermark printed once across each page.
   *
   * <p>FilterData: {@code Watermark}.
   *
   * @param text The watermark text. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfWatermarkOptions text(final @Nullable String text) {
    setText(PdfOption.WATERMARK, text);
    return this;
  }

  /**
   * Specifies the text of a watermark repeated all over each page.
   *
   * <p>FilterData: {@code TiledWatermark}. Requires LibreOffice 6.3 or later.
   *
   * @param text The watermark text. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfWatermarkOptions tiledText(final @Nullable String text) {
    setText(PdfOption.TILED_WATERMARK, text);
    return this;
  }

  /**
   * Specifies the font of the watermark.
   *
   * <p>FilterData: {@code WatermarkFontName}. Requires LibreOffice 7.4 or later.
   *
   * @param name The font name. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfWatermarkOptions fontName(final @Nullable String name) {
    setText(PdfOption.WATERMARK_FONT_NAME, name);
    return this;
  }

  /**
   * Specifies the height of the font of the watermark.
   *
   * <p>FilterData: {@code WatermarkFontHeight}. Requires LibreOffice 7.4 or later.
   *
   * @param points The font height, in points.
   * @return This group.
   */
  public @NonNull PdfWatermarkOptions fontHeight(final int points) {
    setAtLeast(PdfOption.WATERMARK_FONT_HEIGHT, "fontHeight", points, 1);
    return this;
  }

  /**
   * Specifies the color of the watermark.
   *
   * <p>FilterData: {@code WatermarkColor}. Requires LibreOffice 7.4 or later.
   *
   * @param rgb The color as an RGB value, such as {@code 0xFF0000} for red.
   * @return This group.
   */
  public @NonNull PdfWatermarkOptions color(final int rgb) {
    AssertUtils.isTrue(rgb >= 0 && rgb <= 0xFFFFFF, "color must be an RGB value");
    set(PdfOption.WATERMARK_COLOR, rgb);
    return this;
  }

  /**
   * Specifies the rotation of the watermark.
   *
   * <p>FilterData: {@code WatermarkRotateAngle}, in tenths of a degree. Requires LibreOffice 7.4 or
   * later.
   *
   * @param degrees The rotation angle, in degrees.
   * @return This group.
   */
  public @NonNull PdfWatermarkOptions rotation(final int degrees) {
    AssertUtils.isTrue(degrees >= 0 && degrees < 360, "rotation must be between 0 and 359");
    set(PdfOption.WATERMARK_ROTATE_ANGLE, degrees * 10);
    return this;
  }
}
