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

package org.jodconverter.local.filter.text;

import static org.jodconverter.local.office.LocalOfficeUtils.toUrl;

import java.io.File;
import java.util.Objects;

import com.sun.star.frame.DispatchResultEvent;
import com.sun.star.frame.DispatchResultState;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.XComponentContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;
import org.jodconverter.local.office.utils.Write;

/**
 * This filter compares the document being converted with an earlier version of it, as the <em>Edit,
 * Track Changes, Compare Document</em> command of the office does: the differences become tracked
 * changes of the document being converted. What it has and the original has not is recorded as an
 * insertion, what the original had and it has not as a deletion. Stored as a text document, the
 * result keeps the changes, to accept or to reject; exported to PDF, it shows them.
 *
 * <p>The office only compares a document it can edit, and a converter loads documents read-only by
 * default: the converter that uses this filter must load them with the {@code ReadOnly} load
 * property set to {@code false}.
 *
 * <pre>{@code
 * LocalConverter.builder()
 *     .loadProperty("ReadOnly", false)
 *     .filterChain(new DocumentComparerFilter(new File("contract-v1.docx")))
 *     .build()
 *     .convert(new File("contract-v2.docx"))
 *     .to(new File("changes.pdf"))
 *     .execute();
 * }</pre>
 *
 * <p>The filter applies to text documents; other documents are left as they are.
 */
public class DocumentComparerFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentComparerFilter.class);

  private static final String DISPATCH_HELPER = "com.sun.star.frame.DispatchHelper";
  private static final String COMPARE_COMMAND = ".uno:CompareDocuments";

  private final File original;

  /**
   * Creates a new filter that will compare the document being converted with the specified one.
   *
   * @param original The original document, that is, the earlier version of the document being
   *     converted.
   */
  public DocumentComparerFilter(final @NonNull File original) {
    super();

    Objects.requireNonNull(original, "original must not be null");
    this.original = original;
  }

  @Override
  public void doFilter(
      final @NonNull OfficeContext context,
      final @NonNull XComponent document,
      final @NonNull FilterChain chain)
      throws Exception {

    // This filter can only be used with text document
    if (Write.isText(document)) {
      LOGGER.debug("Applying the DocumentComparerFilter");
      compare(((LocalOfficeContext) context).getComponentContext(), document);
    }

    // Invoke the next filter in the chain
    chain.doFilter(context, document);
  }

  private void compare(final XComponentContext context, final XComponent document)
      throws OfficeException {

    checkComparable(document);

    final var helper = Lo.createInstance(context, XDispatchHelper.class, DISPATCH_HELPER);
    final var controller = Lo.qi(XModel.class, document).getCurrentController();
    if (helper == null || controller == null) {
      throw new OfficeException("The office cannot compare this document: it has no view");
    }

    // NoAcceptDialog: the changes are recorded without the dialog that accepts or rejects them.
    final var result =
        helper.executeDispatch(
            Lo.qi(XDispatchProvider.class, controller.getFrame()),
            COMPARE_COMMAND,
            "",
            0,
            Props.makeProperties("URL", toUrl(original), "NoAcceptDialog", true));
    if (!isSuccess(result)) {
      throw new OfficeException("The office could not compare the document with " + original);
    }
  }

  // The office refuses the command without saying why, so the reasons are checked first.
  private void checkComparable(final XComponent document) throws OfficeException {

    if (!original.isFile()) {
      throw new OfficeException("The original document does not exist: " + original);
    }
    if (Lo.qi(XStorable.class, document).isReadonly()) {
      throw new OfficeException(
          "A document loaded read-only cannot be compared: build the converter with"
              + " loadProperty(\"ReadOnly\", false)");
    }
  }

  private static boolean isSuccess(final Object result) {
    return result instanceof DispatchResultEvent event
        && event.State == DispatchResultState.SUCCESS;
  }
}
