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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.task.ErrorCodeIOException;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.office.LocalOfficeContext;

/** Contains tests for the {@link AbstractLocalOfficeTask} class. */
public class AbstractLocalOfficeTaskTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private static class FooOfficeTask extends AbstractLocalOfficeTask {

    public FooOfficeTask(final SourceDocumentSpecs source) {
      super(source);
    }

    @Override
    public void execute(final OfficeContext context) {
      // Do nothing here
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
  public void loadDocument_CatchIllegalArgumentException_ThrowOfficeException()
      throws IOException, IllegalArgumentException {

    final XComponentLoader loader = mock(XComponentLoader.class);
    final LocalOfficeContext context = mock(LocalOfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willThrow(IllegalArgumentException.class);
    given(context.getComponentLoader()).willReturn(loader);

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
        .withCauseExactlyInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void loadDocument_CatchErrorCodeIoException_ThrowOfficeException()
      throws IOException, IllegalArgumentException {

    final XComponentLoader loader = mock(XComponentLoader.class);
    final LocalOfficeContext context = mock(LocalOfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willThrow(ErrorCodeIOException.class);
    given(context.getComponentLoader()).willReturn(loader);

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
        .withCauseExactlyInstanceOf(ErrorCodeIOException.class);
  }

  @Test
  public void loadDocument_CatchIoException_ThrowOfficeException()
      throws IOException, IllegalArgumentException {

    final XComponentLoader loader = mock(XComponentLoader.class);
    final LocalOfficeContext context = mock(LocalOfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willThrow(IOException.class);
    given(context.getComponentLoader()).willReturn(loader);

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> task.loadDocument(context, SOURCE_FILE))
        .withCauseExactlyInstanceOf(IOException.class);
  }

  @Test
  public void closeDocument_WithNull_doNothing() {

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    task.closeDocument(null);
  }
}
