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
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.XComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.LocalOfficeUtils;
import org.jodconverter.local.office.utils.Info;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;
import org.jodconverter.local.office.utils.Write;

/** This filter is used to insert a graphics into a document. */
public class GraphicInserterFilter extends AbstractTextContentInserterFilter {

  // This class has been inspired by these examples:
  // http://api.libreoffice.org/examples/java/Text/GraphicsInserter.java
  // https://forum.openoffice.org/en/forum/viewtopic.php?t=50114#p252402

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphicInserterFilter.class);

  private final String imagePath;

  // Detect the size of an image without loading it into memory
  // See http://stackoverflow.com/a/1560052
  private static Dimension getImageSize(final File image) throws OfficeException {

    try {
      try (ImageInputStream inputStream = ImageIO.createImageInputStream(image)) {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
        if (readers.hasNext()) {
          final ImageReader reader = readers.next();
          try {
            reader.setInput(inputStream);

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

  /**
   * Creates a new filter that will insert the specified image at the specified location while
   * converting a document.
   *
   * @param imagePath The path to the image (file) on disk.
   * @param horizontalPosition The horizontal position where to insert the image on the document
   *     (millimeters).
   * @param verticalPosition The vertical position where to insert the image on the document
   *     (millimeters).
   * @throws OfficeException If the size of the image cannot be detected.
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
   * @param imagePath The path to the image (file) on disk.
   * @param width the width of the image to insert. The original image will be resize if required
   *     (millimeters).
   * @param height The height of the image to insert. The original image will be resize if required
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
   * @param imagePath The path to the image (file) on disk.
   * @param width the width of the image to insert. The original image will be resize if required
   *     (millimeters).
   * @param height The height of the image to insert. The original image will be resize if required
   *     (millimeters).
   * @param shapeProperties The properties to apply to the created graphic shape.
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
   * @param imagePath The path to the image (file) on disk.
   * @param shapeProperties The properties to apply to the created graphic shape.
   * @throws OfficeException If the size of the image cannot be detected.
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
      throws Exception {

    LOGGER.debug("Applying the GraphicInserterFilter");

    // This filter can only be used with text document
    if (Write.isText(document)) {
      insertGraphic(((LocalOfficeContext) context).getComponentContext(), document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void insertGraphic(final XComponentContext context, final XComponent document)
      throws Exception {

    // Querying for the interface XMultiServiceFactory (text service factory) on the XTextDocument
    final XMultiServiceFactory serviceFactory = Lo.getServiceFactory(document);

    // Creating graphic shape service
    final Object graphicShape =
        serviceFactory.createInstance("com.sun.star.drawing.GraphicObjectShape");

    // Access the XShape interface of the GraphicObjectShape
    final XShape shape = Lo.qi(XShape.class, graphicShape);

    // Set the size of the new Text Frame using the XShape's 'setSize'
    shape.setSize(toOfficeSize(getRectSize()));

    // Inserting image to the document
    final File sourceFile = new File(imagePath);
    final String strUrl = LocalOfficeUtils.toUrl(sourceFile);

    // Querying property interface for the graphic shape service
    final XPropertySet propSet = Lo.qi(XPropertySet.class, graphicShape);

    if (Info.isLibreOffice(context)
        && Info.compareVersions("6.1", Info.getOfficeVersionShort(context), 2) >= 0) {

      // Create a GraphicProvider at the global service manager.
      final XGraphicProvider graphicProvider =
          Lo.createInstanceMCF(
              context, XGraphicProvider.class, "com.sun.star.graphic.GraphicProvider");

      // Since 6.1, we must use "Graphic" instead of "GraphicURL"
      propSet.setPropertyValue(
          "Graphic",
          graphicProvider.queryGraphic(Props.makeProperties("URL", strUrl, "LoadAsLink", false)));

    } else {
      // Creating bitmap container service
      final XNameContainer bitmapContainer =
          Lo.createInstanceMSF(
              serviceFactory, XNameContainer.class, "com.sun.star.drawing.BitmapTable");

      LOGGER.debug("Embedding image to the bitmap container '{}'", strUrl);
      final String uuid = UUID.randomUUID().toString();
      bitmapContainer.insertByName(uuid, strUrl);

      // Assign image internal URL to the graphic shape property
      propSet.setPropertyValue("GraphicURL", bitmapContainer.getByName(uuid));
    }

    // Assign all the other properties
    for (final Map.Entry<String, Object> entry : getShapeProperties().entrySet()) {
      propSet.setPropertyValue(entry.getKey(), entry.getValue());
    }

    // Querying for the interface XTextDocument (text interface) on the XComponent
    final XTextDocument docText = Write.getTextDoc(document);

    // Getting text field interface
    final XText text = docText.getText();

    // Getting text cursor
    final XTextCursor textCursor = text.createTextCursor();

    // Apply the AnchorPageNo fix
    applyAnchorPageNoFix(docText, textCursor);

    // Convert graphic shape to the text content item
    final XTextContent textContent = Lo.qi(XTextContent.class, graphicShape);

    // Embed image into the document text with replacement
    LOGGER.debug("Inserting image into the document");
    text.insertTextContent(textCursor, textContent, false);
  }
}
