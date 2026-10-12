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

package org.jodconverter.local.filter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.style.XStyleFamiliesSupplier;

import org.jodconverter.local.office.utils.UnoRuntime;

/** Builds mocked documents for the tests of the filters that change the page styles. */
final class PageStylesMocks {

  /**
   * Creates a document that supports the given service only.
   *
   * @param unoRuntime The mocked runtime.
   * @param service The service of the document, or null for a document that supports none.
   * @return The document.
   */
  /* default */ static XComponent document(final UnoRuntime unoRuntime, final String service) {

    final var document = mock(XComponent.class);
    final var serviceInfo = mock(XServiceInfo.class);
    given(unoRuntime.queryInterface(XServiceInfo.class, document)).willReturn(serviceInfo);
    given(serviceInfo.supportsService(anyString())).willReturn(false);
    if (service != null) {
      given(serviceInfo.supportsService(service)).willReturn(true);
    }
    return document;
  }

  /**
   * Gives page styles to a document.
   *
   * @param unoRuntime The mocked runtime.
   * @param document The document.
   * @param names The names of the page styles.
   * @return The property sets of the page styles, in the order of the names.
   * @throws Exception Never thrown by the mocks.
   */
  /* default */ static List<XPropertySet> pageStyles(
      final UnoRuntime unoRuntime, final XComponent document, final String... names)
      throws Exception {

    final var supplier = mock(XStyleFamiliesSupplier.class);
    final var families = mock(XNameAccess.class);
    final var family = mock(XNameAccess.class);
    final var familyAny = new Object();
    given(unoRuntime.queryInterface(XStyleFamiliesSupplier.class, document)).willReturn(supplier);
    given(supplier.getStyleFamilies()).willReturn(families);
    given(families.getByName("PageStyles")).willReturn(familyAny);
    given(unoRuntime.queryInterface(XNameAccess.class, familyAny)).willReturn(family);
    given(family.getElementNames()).willReturn(names);

    final var styles = new ArrayList<XPropertySet>();
    for (final var name : names) {
      final var styleAny = new Object();
      final var style = mock(XPropertySet.class);
      given(family.getByName(name)).willReturn(styleAny);
      given(unoRuntime.queryInterface(XPropertySet.class, styleAny)).willReturn(style);
      styles.add(style);
    }
    return styles;
  }

  private PageStylesMocks() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
