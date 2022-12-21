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

import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.awt.Size;
import com.sun.star.frame.XController;
import com.sun.star.text.RelOrientation;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.VertOrientation;
import com.sun.star.text.WrapTextMode;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.office.utils.Lo;

/** Base class for all filter used to insert a TextContent into a document. */
public abstract class AbstractTextContentInserterFilter implements Filter {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractTextContentInserterFilter.class);

  private Dimension rectSize;
  private final Map<String, Object> shapeProperties;

  /**
   * Creates the default shape properties that would insert text content at the specified position
   * on the first page of a document.
   *
   * @param horizontalPosition The horizontal position where to insert the text content on the
   *     document (millimeters).
   * @param verticalPosition The vertical position where to insert the text content on the document
   *     (millimeters).
   * @return A map containing The default shape properties.
   */
  public static @NonNull Map<@NonNull String, @NonNull Object> createDefaultShapeProperties(
      final int horizontalPosition, final int verticalPosition) {

    final Map<String, Object> props = new LinkedHashMap<>();

    // For all the available properties, see
    // https://api.libreoffice.org/docs/idl/ref/servicecom_1_1sun_1_1star_1_1text_1_1Shape.html

    // Setting the anchor type
    props.put("AnchorType", TextContentAnchorType.AT_PAGE);

    // Setting the horizontal position (1 = 0.01 mm)
    props.put("HoriOrient", VertOrientation.NONE);
    props.put("HoriOrientPosition", horizontalPosition * 100);
    props.put("HoriOrientRelation", RelOrientation.PAGE_FRAME);

    // Setting the vertical position (1 = 0.01 mm)
    props.put("VertOrient", VertOrientation.NONE);
    props.put("VertOrientPosition", verticalPosition * 100);
    props.put("VertOrientRelation", RelOrientation.PAGE_FRAME);

    // Setting the wrap behavior
    props.put("Surround", WrapTextMode.THROUGHT);

    return props;
  }

  /**
   * Converts the specified size where units are millimeters to an office size where unit are 1/100
   * millimeters.
   *
   * @param size The size to convert, in millimeters.
   * @return The converted size instance, in 1/100 millimeters.
   */
  public static Size toOfficeSize(final @NonNull Dimension size) {

    return new Size(size.width * 100, size.height * 100);
  }

  /**
   * Creates a new filter that will insert a text content (shape) at the specified location while
   * converting a document.
   *
   * @param horizontalPosition The horizontal position where to insert the text content on the
   *     document (millimeters).
   * @param verticalPosition The vertical position where to insert the text content on the document
   *     (millimeters).
   */
  public AbstractTextContentInserterFilter(
      final int horizontalPosition, final int verticalPosition) {
    super();

    this.shapeProperties = createDefaultShapeProperties(horizontalPosition, verticalPosition);
  }

  /**
   * Creates a new filter that will insert a text content (shape) using the specified shape
   * properties while converting a document.
   *
   * @param shapeProperties The properties to apply to the created shape.
   * @see <a
   *     href="https://api.libreoffice.org/docs/idl/ref/servicecom_1_1sun_1_1star_1_1text_1_1Shape.html">Drawing
   *     Shapes</a>
   */
  public AbstractTextContentInserterFilter(
      final @NonNull Map<@NonNull String, @NonNull Object> shapeProperties) {
    super();

    AssertUtils.notNull(shapeProperties, "shapeProperties must not be null");

    this.shapeProperties = new LinkedHashMap<>(shapeProperties);
  }

  /**
   * Creates a new filter that will insert a text content (shape) of the specified size at the
   * specified location while converting a document.
   *
   * @param size The dimension of the shape that will be inserted (millimeters).
   * @param horizontalPosition The horizontal position where to insert the text content on the
   *     document (millimeters).
   * @param verticalPosition The vertical position where to insert the text content on the document
   *     (millimeters).
   */
  public AbstractTextContentInserterFilter(
      final @NonNull Dimension size, final int horizontalPosition, final int verticalPosition) {
    super();

    AssertUtils.notNull(size, "size must not be null");

    this.rectSize = new Dimension(size.width, size.height);
    this.shapeProperties = createDefaultShapeProperties(horizontalPosition, verticalPosition);
  }

  /**
   * Creates a new filter that will insert a text content (shape) of the specified size using the
   * specified shape properties while converting a document.
   *
   * @param size The dimension of the shape that will be inserted (millimeters).
   * @param shapeProperties The properties to apply to the created shape.
   * @see <a
   *     href="https://api.libreoffice.org/docs/idl/ref/servicecom_1_1sun_1_1star_1_1text_1_1Shape.html">Drawing
   *     Shapes</a>
   */
  public AbstractTextContentInserterFilter(
      final @NonNull Dimension size,
      final @NonNull Map<@NonNull String, @NonNull Object> shapeProperties) {
    super();

    AssertUtils.notNull(size, "size must not be null");
    AssertUtils.notNull(shapeProperties, "shapeProperties must not be null");

    this.rectSize = new Dimension(size.width, size.height);
    this.shapeProperties = new LinkedHashMap<>(shapeProperties);
  }

  /**
   * Jumps to the page specified by the "AnchorPageNo" if present in the shape properties if this
   * filter. This seems to be required for some output format.
   *
   * @param docText The text interface for the document.
   * @param textCursor The text cursor of the document.
   */
  protected void applyAnchorPageNoFix(
      final @NonNull XTextDocument docText, final @NonNull XTextCursor textCursor) {

    // The following bloc seems to be required for some output format
    // (doc, docx, rtf) instead of the "AnchorPageNo" property.
    final Object anchorPageNo = shapeProperties.get("AnchorPageNo");
    if (anchorPageNo != null) {
      LOGGER.debug("Applying AnchorPageNo fix");
      final XController controller = docText.getCurrentController();
      final XTextViewCursor viewCursor =
          Lo.qi(XTextViewCursorSupplier.class, controller).getViewCursor();
      final XPageCursor pageCursor = Lo.qi(XPageCursor.class, viewCursor);
      pageCursor.jumpToPage(Short.parseShort(anchorPageNo.toString()));
      textCursor.gotoRange(viewCursor, false);
    }
  }

  /**
   * Gets the rectangle's size of the shape that will be inserted.
   *
   * @return A Rectangle that represents the size of the shape. Units are millimeters.
   */
  public @NonNull Dimension getRectSize() {
    return rectSize;
  }

  /**
   * Sets the rectangle's size of the shape that will be inserted.
   *
   * @param size A Rectangle that represents the size of the shape. Units are millimeters.
   */
  protected void setRectSize(final @NonNull Dimension size) {
    this.rectSize = size;
  }

  /**
   * Gets the shape's properties to apply when inserting the shape to the document.
   *
   * @return A map that contains the properties.
   */
  public @NonNull Map<@NonNull String, Object> getShapeProperties() {
    return shapeProperties;
  }
}
