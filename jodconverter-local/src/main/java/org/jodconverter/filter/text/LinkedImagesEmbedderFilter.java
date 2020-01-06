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

package org.jodconverter.filter.text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.graphic.XGraphic;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;

import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.LocalOfficeContext;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.utils.Info;
import org.jodconverter.office.utils.Lo;
import org.jodconverter.office.utils.Props;
import org.jodconverter.office.utils.Write;

/** This filter is used to insert a graphics into a document. */
public class LinkedImagesEmbedderFilter implements Filter {

  // This class has been inspired by these examples:
  // http://api.libreoffice.org/examples/java/Text/GraphicsInserter.java
  // https://forum.openoffice.org/en/forum/viewtopic.php?t=50114#p252402

  private static final Logger LOGGER = LoggerFactory.getLogger(LinkedImagesEmbedderFilter.class);

  @Override
  public void doFilter(
      final OfficeContext context, final XComponent document, final FilterChain chain)
      throws Exception {

    LOGGER.debug("Applying the LinkedImagesEmbedderFilter");

    // This filter can be used only with text document
    if (Write.isText(document)) {
      convertLinkedImagesToEmbedded(((LocalOfficeContext) context).getComponentContext(), document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private static void convertLinkedImagesToEmbedded(
      final XComponentContext context, final XComponent document) throws Exception {

    // Create a GraphicProvider.
    final XGraphicProvider graphicProvider =
        Lo.createInstanceMCF(
            context, XGraphicProvider.class, "com.sun.star.graphic.GraphicProvider");
    final XIndexAccess indexAccess =
        Lo.qi(
            XIndexAccess.class,
            Lo.qi(XTextGraphicObjectsSupplier.class, document).getGraphicObjects());
    for (int i = 0; i < indexAccess.getCount(); i++) {
      final Any xImageAny = (Any) indexAccess.getByIndex(i);
      final Object xImageObject = xImageAny.getObject();
      final XTextContent xImage = (XTextContent) xImageObject;
      final XServiceInfo xInfo = Lo.qi(XServiceInfo.class, xImage);
      if (xInfo.supportsService("com.sun.star.text.TextGraphicObject")) {
        final XPropertySet xPropSet = Lo.qi(XPropertySet.class, xImage);
        if (Info.isLibreOffice(context)
            && Info.compareVersions(Info.getOfficeVersionShort(context), "6.1", 2) >= 0) {
          final XGraphic xGraphic =
              (XGraphic)
                  AnyConverter.toObject(XGraphic.class, xPropSet.getPropertyValue("Graphic"));
          // Only ones that are not embedded
          final XPropertySet xGraphixPropSet = Lo.qi(XPropertySet.class, xGraphic);
          boolean linked = (boolean) xGraphixPropSet.getPropertyValue("Linked");
          if (linked) {
            // Since 6.1, we must use "Graphic" instead of "GraphicURL"
            xPropSet.setPropertyValue(
                "Graphic",
                graphicProvider.queryGraphic(
                    Props.makeProperties(
                        "URL",
                        xGraphixPropSet.getPropertyValue("OriginURL").toString(),
                        "LoadAsLink",
                        false)));
          }
        } else {
          final String name = xPropSet.getPropertyValue("LinkDisplayName").toString();
          final String graphicURL = xPropSet.getPropertyValue("GraphicURL").toString();
          // Only ones that are not embedded
          if (graphicURL.indexOf("vnd.sun.") == -1) {
            // Creating bitmap container service
            final XNameContainer bitmapContainer =
                Lo.createInstanceMSF(
                    document, XNameContainer.class, "com.sun.star.drawing.BitmapTable");
            if (!bitmapContainer.hasByName(name)) {
              bitmapContainer.insertByName(name, graphicURL);
              xPropSet.setPropertyValue("GraphicURL", bitmapContainer.getByName(name).toString());
            }
          }
        }
      }
    }
  }
}
