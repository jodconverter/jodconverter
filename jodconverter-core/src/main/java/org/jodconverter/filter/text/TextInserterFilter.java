/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextFrame;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/** This filter is used to insert text into a document. */
public class TextInserterFilter extends TextContentInserterFilter {

  // This class has been inspired by these examples:
  // http://api.libreoffice.org/examples/java/Text/SWriter.java
  // http://api.libreoffice.org/examples/DevelopersGuide/Text/TextDocuments.java

  private static final Logger logger = LoggerFactory.getLogger(TextInserterFilter.class);

  private final String insertedText;

  /**
   * Creates a new filter that will insert the specified text at the specified location while
   * converting a document.
   *
   * @param text The text to insert.
   * @param width The width of the rectangle to insert (millimeters).
   * @param height The height of the rectangle to insert (millimeters).
   * @param horizontalPosition The horizontal position where to insert the text on the document
   *     (millimeters).
   * @param verticalPosition The vertical position where to insert the text on the document
   *     (millimeters).
   */
  public TextInserterFilter(
      final String text,
      final int width,
      final int height,
      final int horizontalPosition,
      final int verticalPosition) {
    super(new Dimension(width, height), horizontalPosition, verticalPosition);

    Validate.notBlank(text);

    this.insertedText = text;
  }

  /**
   * Creates a new filter that will insert the specified text at the specified location while
   * converting a document.
   *
   * @param text The text to insert.
   * @param width The width of the graphic to insert. The original image will be resize if required
   *     (millimeters).
   * @param height The height of the image. The original image will be resize if required
   *     (millimeters).
   * @param shapeProperties The properties to apply to the created rectangle shape.
   * @see <a
   *     href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes">Drawing_Shapes</a>
   */
  public TextInserterFilter(
      final String text,
      final int width,
      final int height,
      final Map<String, Object> shapeProperties) {
    super(new Dimension(width, height), shapeProperties);

    Validate.notBlank(text);

    this.insertedText = text;
  }

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws OfficeException {

    // Querying for the interface XTextDocument (text interface) on the XComponent
    final XTextDocument docText = UnoRuntime.queryInterface(XTextDocument.class, document);

    // Querying for the interface XMultiServiceFactory (text service factory) on the XTextDocument
    final XMultiServiceFactory docServiceFactory =
        UnoRuntime.queryInterface(XMultiServiceFactory.class, docText);

    try {
      // Use the document's factory to create a new text frame and
      // immediately access its XTextFrame interface
      final XTextFrame textFrame =
          UnoRuntime.queryInterface(
              XTextFrame.class, docServiceFactory.createInstance("com.sun.star.text.TextFrame"));

      // Access the XShape interface of the TextFrame
      final XShape shape = UnoRuntime.queryInterface(XShape.class, textFrame);

      // Set the size of the new Text Frame using the XShape's 'setSize'
      shape.setSize(toOfficeSize(getRectSize()));

      // Access the XPropertySet interface of the TextFrame
      final XPropertySet propSet = UnoRuntime.queryInterface(XPropertySet.class, textFrame);

      // Assign all the other properties
      for (final Map.Entry<String, Object> entry : getShapeProperties().entrySet()) {
        propSet.setPropertyValue(entry.getKey(), entry.getValue());
      }

      // Access the XText interface of the text contained within the frame
      XText text = docText.getText();
      XTextCursor textCursor = text.createTextCursor();

      // Apply the AnchorPageNo fix
      applyAnchorPageNoFix(docText, textCursor);

      // Insert the new frame into the document
      logger.debug("Inserting frame into the document");
      text.insertTextContent(textCursor, textFrame, false);

      // Access the XText interface of the text contained within the frame
      text = textFrame.getText();

      // Create a TextCursor over the frame's contents
      textCursor = text.createTextCursor();

      // Insert text into the frame
      logger.debug("Writing text to the inserted frame");
      text.insertString(textCursor, insertedText, false);

    } catch (Exception ex) {
      throw new OfficeException("Could not insert text into document.", ex);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }
}
