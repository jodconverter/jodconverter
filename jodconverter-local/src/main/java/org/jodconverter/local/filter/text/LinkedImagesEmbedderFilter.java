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

import java.util.Objects;

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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.utils.Info;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;
import org.jodconverter.local.office.utils.Write;

/** This filter is used to embed linked images while converting a document. */
public class LinkedImagesEmbedderFilter implements Filter {

  // See: https://github.com/sbraconnier/jodconverter/issues/110

  private static final Logger LOGGER = LoggerFactory.getLogger(LinkedImagesEmbedderFilter.class);

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    // This filter can be used only with text document
    if (Write.isText(document)) {
      LOGGER.debug("Applying the LinkedImagesEmbedderFilter");
      convertLinkedImagesToEmbedded(((LocalOfficeContext) context).getComponentContext(), document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private static void convertLinkedImagesToEmbedded(
      final XComponentContext context, final XComponent document) throws Exception {

    final XIndexAccess indexAccess =
        Lo.qi(
            XIndexAccess.class,
            Lo.qi(XTextGraphicObjectsSupplier.class, document).getGraphicObjects());
    final boolean useGraphic =
        Info.isLibreOffice(context)
            && Info.compareVersions(Info.getOfficeVersionShort(context), "6.1", 2) >= 0;
    // Create a GraphicProvider if required.
    final XGraphicProvider graphicProvider =
        useGraphic
            ? Lo.createInstance(
                context, XGraphicProvider.class, "com.sun.star.graphic.GraphicProvider")
            : null;
    for (int i = 0; i < indexAccess.getCount(); i++) {
      final Any xImageAny = (Any) indexAccess.getByIndex(i);
      final Object xImageObject = xImageAny.getObject();
      final XTextContent xImage = (XTextContent) xImageObject;
      final XServiceInfo xInfo = Lo.qi(XServiceInfo.class, xImage);
      if (xInfo.supportsService("com.sun.star.text.TextGraphicObject")) {
        final XPropertySet xPropSet = Lo.qi(XPropertySet.class, xImage);
        if (useGraphic) {
          embedImageUsingGraphic(graphicProvider, xPropSet);
        } else {
          embedImageUsingGraphicUrl(document, xPropSet);
        }
      }
    }
  }

  private static void embedImageUsingGraphic(
      final XGraphicProvider graphicProvider, final XPropertySet propSet) throws Exception {
    final XGraphic xGraphic =
        (XGraphic) AnyConverter.toObject(XGraphic.class, propSet.getPropertyValue("Graphic"));
    // Only ones that are not embedded
    final XPropertySet xGraphicPropSet = Lo.qi(XPropertySet.class, xGraphic);
    final boolean linked = (boolean) xGraphicPropSet.getPropertyValue("Linked");
    if (linked) {
      // Since 6.1, we must use "Graphic" instead of "GraphicURL"
      Objects.requireNonNull(graphicProvider);
      propSet.setPropertyValue(
          "Graphic",
          graphicProvider.queryGraphic(
              Props.makeProperties(
                  "URL",
                  xGraphicPropSet.getPropertyValue("OriginURL").toString(),
                  "LoadAsLink",
                  false)));
    }
  }

  private static void embedImageUsingGraphicUrl(
      final XComponent document, final XPropertySet propSet) throws Exception {
    final String name = propSet.getPropertyValue("LinkDisplayName").toString();
    final String graphicUrl = propSet.getPropertyValue("GraphicURL").toString();
    // Only ones that are not embedded
    if (!graphicUrl.contains("vnd.sun.")) {
      // Creating bitmap container service
      final XNameContainer bitmapContainer =
          Lo.createInstance(document, XNameContainer.class, "com.sun.star.drawing.BitmapTable");
      if (!bitmapContainer.hasByName(name)) {
        bitmapContainer.insertByName(name, graphicUrl);
        propSet.setPropertyValue("GraphicURL", bitmapContainer.getByName(name).toString());
      }
    }
  }
}
