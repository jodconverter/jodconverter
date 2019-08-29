/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import org.jodconverter.filter.Filter;
import org.jodconverter.office.utils.Lo;

/** Base class for all filter used to insert a TextContent into a document. */
public abstract class AbstractTextContentInserterFilter implements Filter {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractTextContentInserterFilter.class);

  private final Dimension rectSize;
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
  public static Map<String, Object> createDefaultShapeProperties(
      final int horizontalPosition, final int verticalPosition) {

    final Map<String, Object> props = new LinkedHashMap<>();

    // For all the available properties, see
    // https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes

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

    // Setting the the wrap behavior
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
  public static Size toOfficeSize(final Dimension size) {

    return new Size(size.width * 100, size.height * 100);
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
      final Dimension size, final int horizontalPosition, final int verticalPosition) {
    super();

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
   *     href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes">Drawing_Shapes</a>
   */
  public AbstractTextContentInserterFilter(
      final Dimension size, final Map<String, Object> shapeProperties) {
    super();

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
  protected void applyAnchorPageNoFix(final XTextDocument docText, final XTextCursor textCursor) {

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
  public Dimension getRectSize() {
    return rectSize;
  }

  /**
   * Gets the shape's properties to apply when inserting the shape to the document.
   *
   * @return A map that contains the properties.
   */
  public Map<String, Object> getShapeProperties() {
    return shapeProperties;
  }
}
