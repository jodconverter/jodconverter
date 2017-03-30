/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.filter;

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
import com.sun.star.uno.UnoRuntime;

/** Base class for all filter used to insert a TextContent into a document. */
public abstract class TextContentInserterFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger(TextContentInserterFilter.class);

  /**
   * Creates the default shape properties that would insert text content at the specified position
   * on the first page of a document.
   *
   * @param horizontalPosition The horizontal position where to insert the text content on the
   *     document (millimeters).
   * @param verticalPosition The vertical position where to insert the text content on the document
   *     (millimeters).
   * @return A map containing the default shape properties.
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
   * @param size the size to convert, in millimeters.
   * @return the converted size instance, in 1/100 millimeters.
   */
  public static Size toOfficeSize(final Dimension size) {

    return new Size(size.width * 100, size.height * 100);
  }

  private final Dimension rectSize;
  private final Map<String, Object> shapeProperties;

  /**
   * Creates a new filter that will insert a text content (shape) of the specified size at the
   * specified location while converting a document.
   *
   * @param size the dimension of the shape that will be inserted (millimeters).
   * @param horizontalPosition The horizontal position where to insert the text content on the
   *     document (millimeters).
   * @param verticalPosition The vertical position where to insert the text content on the document
   *     (millimeters).
   */
  public TextContentInserterFilter(
      final Dimension size, final int horizontalPosition, final int verticalPosition) {
    super();

    this.rectSize = new Dimension(size.width, size.height);
    this.shapeProperties = createDefaultShapeProperties(horizontalPosition, verticalPosition);
  }

  /**
   * Creates a new filter that will insert a text content (shape) of the specified size using the
   * specified shape properties while converting a document.
   *
   * @param size the dimension of the shape that will be inserted (millimeters).
   * @param shapeProperties the properties to apply to the created shape.
   * @see <a
   *     href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes">Drawing_Shapes</a>
   */
  public TextContentInserterFilter(
      final Dimension size, final Map<String, Object> shapeProperties) {
    super();

    this.rectSize = new Dimension(size.width, size.height);
    this.shapeProperties = new LinkedHashMap<>(shapeProperties);
  }

  /**
   * Jumps to the page specified by the "AnchorPageNo" if present in the shape properties if this
   * filter. This seems to be required for some output format.
   *
   * @param docText the text interface for the document.
   * @param textCursor the text cursor of the document.
   */
  protected void applyAnchorPageNoFix(final XTextDocument docText, final XTextCursor textCursor) {

    // The following bloc seems to be required for some output format
    // (doc, docx, rtf) instead of the "AnchorPageNo" property.
    final Object anchorPageNo = shapeProperties.get("AnchorPageNo");
    if (anchorPageNo != null) {
      logger.debug("Applying AnchorPageNo fix");
      final XController controller = docText.getCurrentController();
      final XTextViewCursorSupplier cursorSupplier =
          UnoRuntime.queryInterface(XTextViewCursorSupplier.class, controller);
      final XTextViewCursor textViewCursor = cursorSupplier.getViewCursor();
      final XPageCursor pageCursor = UnoRuntime.queryInterface(XPageCursor.class, textViewCursor);
      pageCursor.jumpToPage(Short.parseShort(anchorPageNo.toString()));
      textCursor.gotoRange(textViewCursor, false);
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
