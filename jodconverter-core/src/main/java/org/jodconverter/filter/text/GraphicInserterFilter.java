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

package org.jodconverter.filter.text;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;

import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeUtils;

/** This filter is used to insert a graphics into a document. */
public class GraphicInserterFilter extends TextContentInserterFilter {

  // This class has been inspired by these examples:
  // http://api.libreoffice.org/examples/java/Text/GraphicsInserter.java
  // https://forum.openoffice.org/en/forum/viewtopic.php?t=50114#p252402

  private static final Logger logger = LoggerFactory.getLogger(GraphicInserterFilter.class);

  // Detect the size of an image without loading it into memory
  // See http://stackoverflow.com/a/1560052
  private static Dimension getImageSize(final File image) throws OfficeException {

    try {
      try (final ImageInputStream in = ImageIO.createImageInputStream(image)) {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
        if (readers.hasNext()) {
          final ImageReader reader = readers.next();
          try { // NOSONAR
            reader.setInput(in);

            // Get dimensions of first image in the stream, without decoding pixel values
            return new Dimension(
                pixelsToMillimeters(reader.getWidth(0)), pixelsToMillimeters(reader.getHeight(0)));
          } finally {
            reader.dispose();
          }
        } else {
          throw new OfficeException("Unable to detect the image size: No reader found");
        }
      }
    } catch (IOException ioEx) {
      throw new OfficeException("Unable to detect the image size", ioEx);
    }
  }

  // Convert pixels to millimeters
  private static int pixelsToMillimeters(final int pixels) {

    // 1 mm = 3.7795275590551 pixel (X)
    // 1 pixel (X) = 0.26458333333333 mm
    return Math.round(pixels * 0.26458333333333f);
  }

  private final String imagePath;

  /**
   * Creates a new filter that will insert the specified image at the specified location while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param horizontalPosition The horizontal position where to insert the image on the document
   *     (millimeters).
   * @param verticalPosition The vertical position where to insert the image on the document
   *     (millimeters).
   * @throws OfficeException if the size of the image cannot be detected.
   */
  public GraphicInserterFilter(
      final String imagePath, final int horizontalPosition, final int verticalPosition)
      throws OfficeException {
    super(getImageSize(new File(imagePath)), horizontalPosition, verticalPosition);

    this.imagePath = imagePath;
  }

  /**
   * Creates a new filter that will insert the specified image at the specified location while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param width the width of the image to insert. The original image will be resize if required
   *     (millimeters).
   * @param height the height of the image to insert. The original image will be resize if required
   *     (millimeters).
   * @param horizontalPosition The horizontal position where to insert the image on the document
   *     (millimeters).
   * @param verticalPosition The vertical position where to insert the image on the document
   *     (millimeters).
   */
  public GraphicInserterFilter(
      final String imagePath,
      final int width,
      final int height,
      final int horizontalPosition,
      final int verticalPosition) {
    super(new Dimension(width, height), horizontalPosition, verticalPosition);

    this.imagePath = imagePath;
  }

  /**
   * Creates a new filter that will insert the specified image using the specified properties while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param width the width of the image to insert. The original image will be resize if required
   *     (millimeters).
   * @param height the height of the image to insert. The original image will be resize if required
   *     (millimeters).
   * @param shapeProperties the properties to apply to the created graphic shape.
   * @see <a
   *     href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes">Drawing_Shapes</a>
   */
  public GraphicInserterFilter(
      final String imagePath,
      final int width,
      final int height,
      final Map<String, Object> shapeProperties) {
    super(new Dimension(width, height), shapeProperties);

    this.imagePath = imagePath;
  }

  /**
   * Creates a new filter that will insert the specified image using the specified properties while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param shapeProperties the properties to apply to the created graphic shape.
   * @throws OfficeException if the size of the image cannot be detected.
   * @see <a
   *     href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes">Drawing_Shapes</a>
   */
  public GraphicInserterFilter(final String imagePath, final Map<String, Object> shapeProperties)
      throws OfficeException {
    super(getImageSize(new File(imagePath)), shapeProperties);

    this.imagePath = imagePath;
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
      // Creating graphic shape service
      final Object graphicShape =
          docServiceFactory.createInstance("com.sun.star.drawing.GraphicObjectShape");

      // Access the XShape interface of the GraphicObjectShape
      final XShape shape = UnoRuntime.queryInterface(XShape.class, graphicShape);

      // Set the size of the new Text Frame using the XShape's 'setSize'
      shape.setSize(toOfficeSize(getRectSize()));

      // Generate a new unique name
      final String uuid = UUID.randomUUID().toString();

      // Creating bitmap container service
      final XNameContainer bitmapContainer =
          UnoRuntime.queryInterface(
              XNameContainer.class,
              docServiceFactory.createInstance("com.sun.star.drawing.BitmapTable"));

      // Inserting test image to the container
      final File sourceFile = new File(imagePath);
      final String strUrl = OfficeUtils.toUrl(sourceFile);
      logger.debug("Embedding image to the bitmap container '{}'", strUrl);
      bitmapContainer.insertByName(uuid, strUrl);

      // Querying property interface for the graphic shape service
      final XPropertySet propSet = UnoRuntime.queryInterface(XPropertySet.class, graphicShape);

      // Assign image internal URL to the graphic shape property
      propSet.setPropertyValue("GraphicURL", bitmapContainer.getByName(uuid));

      // Assign all the other properties
      for (final Map.Entry<String, Object> entry : getShapeProperties().entrySet()) {
        propSet.setPropertyValue(entry.getKey(), entry.getValue());
      }

      // Getting text field interface
      final XText text = docText.getText();

      // Getting text cursor
      final XTextCursor textCursor = text.createTextCursor();

      // Apply the AnchorPageNo fix
      applyAnchorPageNoFix(docText, textCursor);

      // Convert graphic shape to the text content item
      final XTextContent textContent = UnoRuntime.queryInterface(XTextContent.class, graphicShape);

      // Embed image into the document text with replacement
      logger.debug("Inserting image into the document");
      text.insertTextContent(textCursor, textContent, false);
    } catch (Exception ex) {
      throw new OfficeException("Could not insert grapic into document.", ex);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }
}
