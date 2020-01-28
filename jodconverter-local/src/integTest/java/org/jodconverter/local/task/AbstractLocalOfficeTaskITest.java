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

package org.jodconverter.local.task;

import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;

import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseListener;
import com.sun.star.util.XCloseable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalOfficeManagerExtension;
import org.jodconverter.local.office.LocalOfficeContext;
import org.jodconverter.local.office.utils.Lo;

@ExtendWith(LocalOfficeManagerExtension.class)
public class AbstractLocalOfficeTaskITest {

  private static final File SOURCE_FILE = documentFile("test.txt");

  private static class VetoCloseListener implements XCloseListener {

    @Override
    public void disposing(final EventObject event) {
      // Nothing to do here
    }

    @Override
    public void queryClosing(final EventObject event, final boolean getOwnerShip)
        throws CloseVetoException {

      // This will prevent a clean closing
      throw new CloseVetoException();
    }

    @Override
    public void notifyClosing(final EventObject event) {
      // Nothing to do here
    }
  }

  private static class VetoCloseOfficeTask extends AbstractLocalOfficeTask {

    private final FooSourceSpecs source;
    private final VetoCloseListener closeListener = new VetoCloseListener();
    private XComponent document;

    public VetoCloseOfficeTask(final FooSourceSpecs source) {
      super(source);

      this.source = source;
    }

    @Override
    protected XComponent loadDocument(final LocalOfficeContext context, final File sourceFile)
        throws OfficeException {

      document = super.loadDocument(context, sourceFile);
      Lo.qiOptional(XCloseable.class, document)
          .ifPresent(closeable -> closeable.addCloseListener(closeListener));
      return document;
    }

    @Override
    public void execute(final OfficeContext context) throws OfficeException {

      final LocalOfficeContext localContext = (LocalOfficeContext) context;
      final XComponent document = loadDocument(localContext, source.getFile());
      closeDocument(document);
    }

    /* default */ void closeForGood() {

      Lo.qiOptional(XCloseable.class, document)
          .ifPresent(
              closeable -> {
                closeable.removeCloseListener(closeListener);
                try {
                  closeable.close(true);
                } catch (CloseVetoException e) {
                  // Swallow
                }
              });
    }
  }

  private static class FooSourceSpecs extends AbstractSourceDocumentSpecs {

    public FooSourceSpecs(final File source) {
      super(source);
    }

    @Override
    public DocumentFormat getFormat() {
      return DefaultDocumentFormatRegistry.TXT;
    }

    @Override
    public void onConsumed(final File file) {
      // Do nothing here
    }
  }

  @Test
  public void close_WhenVetoCloseExceptionCatch_DocumentNotClosed(OfficeManager manager)
      throws OfficeException {

    final VetoCloseOfficeTask task = new VetoCloseOfficeTask(new FooSourceSpecs(SOURCE_FILE));

    try {
      manager.execute(task);

      // TODO: How could we check that the document is not closed ?

    } finally {
      task.closeForGood();
    }
  }
}
