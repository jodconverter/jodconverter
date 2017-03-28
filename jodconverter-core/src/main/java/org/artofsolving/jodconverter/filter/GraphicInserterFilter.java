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
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.RelOrientation;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.VertOrientation;
import com.sun.star.text.WrapTextMode;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeUtils;

/** This filter is used to insert a graphics into a document. */
public class GraphicInserterFilter implements Filter {

  // This class has been inspired by these examples:
  // http://api.libreoffice.org/examples/java/Text/GraphicsInserter.java
  // https://forum.openoffice.org/en/forum/viewtopic.php?t=50114#p252402

  private static final Logger logger = LoggerFactory.getLogger(GraphicInserterFilter.class);

  private final String imagePath;
  private final Dimension imageSize;
  private Map<String, Object> filterProperties;

  /**
   * Creates a new filter that will insert the specified image at the specified location while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param imageHorizontalPosition The horizontal position where to insert the image on the
   *     document (1/100 mm).
   * @param imageVerticalPosition The vertical position where to insert the image on the document
   *     (1/100 mm).
   * @throws OfficeException if the size of the image cannot be detected.
   */
  public GraphicInserterFilter(
      String imagePath, int imageHorizontalPosition, int imageVerticalPosition)
      throws OfficeException {
    super();

    this.imagePath = imagePath;
    this.imageSize = getImageSize(new File(imagePath));
    this.filterProperties = createDefaultProperties(imageHorizontalPosition, imageVerticalPosition);
  }

  /**
   * Creates a new filter that will insert the specified image at the specified location while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param imageWidth the width of the graphic to insert. The original image will be resize if
   *     required (1/100 mm).
   * @param imageHeight the height of the image. The original image will be resize if required
   *     (1/100 mm).
   * @param imageHorizontalPosition The horizontal position where to insert the image on the
   *     document (1/100 mm).
   * @param imageVerticalPosition The vertical position where to insert the image on the document
   *     (1/100 mm).
   */
  public GraphicInserterFilter(
      String imagePath,
      int imageWidth,
      int imageHeight,
      int imageHorizontalPosition,
      int imageVerticalPosition) {
    super();

    this.imagePath = imagePath;
    this.imageSize = new Dimension(imageWidth, imageHeight);
    this.filterProperties = createDefaultProperties(imageHorizontalPosition, imageVerticalPosition);
  }

  /**
   * Creates a new filter that will insert the specified image at the specified location while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param imageWidth the width of the graphic to insert. The original image will be resize if
   *     required (1/100 mm).
   * @param imageHeight the height of the image. The original image will be resize if required
   *     (1/100 mm).
   * @param graphicShapeProperties the properties to apply to the created graphic shape.
   * @see <a
   *     href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes">Drawing_Shapes</a>
   */
  public GraphicInserterFilter(
      String imagePath,
      int imageWidth,
      int imageHeight,
      Map<String, Object> graphicShapeProperties) {
    super();

    this.imagePath = imagePath;
    this.imageSize = new Dimension(imageWidth, imageHeight);
    this.filterProperties = new LinkedHashMap<>(graphicShapeProperties);
  }

  /**
   * Creates a new filter that will insert the specified image at the specified location while
   * converting a document.
   *
   * @param imagePath the path to the image (file) on disk.
   * @param graphicShapeProperties the properties to apply to the created graphic shape. (1/100 mm).
   * @throws OfficeException if the size of the image cannot be detected.
   * @see <a
   *     href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes">Drawing_Shapes</a>
   */
  public GraphicInserterFilter(String imagePath, Map<String, Object> graphicShapeProperties)
      throws OfficeException {
    super();

    this.imagePath = imagePath;
    this.imageSize = getImageSize(new File(imagePath));
    this.filterProperties = new LinkedHashMap<>(graphicShapeProperties);
  }

  /**
   * Creates the default properties that will insert the image at the specified position into the
   * document.
   *
   * @param imageHorizontalPosition The horizontal position where to insert the image on the
   *     document (1/100 mm).
   * @param imageVerticalPosition The vertical position where to insert the image on the document
   *     (1/100 mm).
   * @return A map containing the default properties.
   */
  protected Map<String, Object> createDefaultProperties(
      int imageHorizontalPosition, int imageVerticalPosition) {

    Map<String, Object> props = new LinkedHashMap<>();

    // For all the available properties, see
    // https://wiki.openoffice.org/wiki/Documentation/DevGuide/Text/Drawing_Shapes

    // Setting the anchor type
    props.put("AnchorType", TextContentAnchorType.AT_PAGE);

    // Setting the horizontal position (1 = 0.01 mm)
    props.put("HoriOrient", VertOrientation.NONE);
    props.put("HoriOrientPosition", imageHorizontalPosition);
    props.put("HoriOrientRelation", RelOrientation.PAGE_FRAME);

    // Setting the vertical position (1 = 0.01 mm)
    props.put("VertOrient", VertOrientation.NONE);
    props.put("VertOrientPosition", imageVerticalPosition);
    props.put("VertOrientRelation", RelOrientation.PAGE_FRAME);

    // Setting the the wrap behavior
    props.put("Surround", WrapTextMode.THROUGHT);

    return props;
  }

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws OfficeException {

    // Querying for the interface XTextDocument (text interface) on the XComponent
    XTextDocument docText = UnoRuntime.queryInterface(XTextDocument.class, document);

    // Querying for the interface XMultiServiceFactory (text service factory) on the XTextDocument
    XMultiServiceFactory docServiceFactory =
        UnoRuntime.queryInterface(XMultiServiceFactory.class, docText);

    // Creating graphic shape service
    Object graphicShape = null;
    try {
      graphicShape = docServiceFactory.createInstance("com.sun.star.drawing.GraphicObjectShape");

      // Customizing graphic shape size
      XShape shapeSettings = UnoRuntime.queryInterface(XShape.class, graphicShape);
      shapeSettings.setSize(new Size(imageSize.width, imageSize.height));

    } catch (Exception ex) {
      throw new OfficeException("Could not create graphic shape service.", ex);
    }

    // Generate a new unique name
    String uuid = UUID.randomUUID().toString();

    // Creating bitmap container service
    XNameContainer bitmapContainer = null;
    try {
      bitmapContainer =
          UnoRuntime.queryInterface(
              XNameContainer.class,
              docServiceFactory.createInstance("com.sun.star.drawing.BitmapTable"));

      // Inserting test image to the container
      File sourceFile = new File(imagePath);
      String strUrl = OfficeUtils.toUrl(sourceFile);
      logger.debug("Inserting image '{}'", strUrl);
      bitmapContainer.insertByName(uuid, strUrl);

    } catch (Exception ex) {
      throw new OfficeException(
          "Could not create bitmap container service with the provided image.", ex);
    }

    // Querying property interface for the graphic shape service
    XPropertySet propSet = UnoRuntime.queryInterface(XPropertySet.class, graphicShape);
    try {
      // Assign image internal URL to the graphic shape property
      propSet.setPropertyValue("GraphicURL", bitmapContainer.getByName(uuid));

      // Assign all the other properties
      for (Map.Entry<String, Object> entry : filterProperties.entrySet()) {
        propSet.setPropertyValue(entry.getKey(), entry.getValue());
      }

    } catch (Exception ex) {
      throw new OfficeException("Could not set property 'GraphicURL'", ex);
    }

    // Getting text field interface
    XText text = docText.getText();

    // Getting text cursor
    XTextCursor textCursor = text.createTextCursor();

    // Convert graphic shape to the text content item
    XTextContent textContent = UnoRuntime.queryInterface(XTextContent.class, graphicShape);

    // Embed image into the document text with replacement
    logger.debug("Inserting image...");
    try {
      // Inserting the content
      text.insertTextContent(textCursor, textContent, true);
    } catch (Exception ex) {
      throw new OfficeException("Could not insert text content", ex);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  // Detect the size of an image without loading it into memory
  // See http://stackoverflow.com/a/1560052
  private Dimension getImageSize(File image) throws OfficeException {

    try {
      try (ImageInputStream in = ImageIO.createImageInputStream(image)) {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
        if (readers.hasNext()) {
          ImageReader reader = readers.next();
          try { // NOSONAR
            reader.setInput(in);

            // Get dimensions of first image in the stream, without decoding pixel values
            return new Dimension(
                pixelsToMillimeters(reader.getWidth(0) * 100),
                pixelsToMillimeters(reader.getHeight(0) * 100));
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
  private int pixelsToMillimeters(int pixels) {

    // 1 mm = 3.7795275590551 pixel (X)
    // 1 pixel (X) = 0.26458333333333 mm
    return Math.round(pixels * 0.26458333333333f);
  }
}
