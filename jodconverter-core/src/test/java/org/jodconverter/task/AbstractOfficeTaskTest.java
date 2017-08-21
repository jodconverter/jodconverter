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

package org.jodconverter.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.task.ErrorCodeIOException;

import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.job.AbstractSourceDocumentSpecs;
import org.jodconverter.job.SourceDocumentSpecs;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;

/**
 * Contains tests for the {@link AbstractOfficeTask} class.
 *
 * @see AbstractOfficeTask
 */
public class AbstractOfficeTaskTest {

  private static final File SOURCE_FILE = new File("src/test/resources/documents/test.txt");

  private static class FooOfficeTask extends AbstractOfficeTask {

    public FooOfficeTask(final SourceDocumentSpecs source) {
      super(source);
    }

    @Override
    public void execute(final OfficeContext context) throws OfficeException {
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
    public Map<String, Object> getCustomLoadProperties() {
      return new HashMap<>();
    }

    @Override
    public void onConsumed(final File file) {
      // Do nothing here
    }
  }

  @Test
  public void loadDocument_CatchIllegalArgumentException_ThrowOfficeException() throws Exception {

    final XComponentLoader loader = mock(XComponentLoader.class);
    final OfficeContext context = mock(OfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willThrow(IllegalArgumentException.class);
    given(context.getComponentLoader()).willReturn(loader);

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    try {
      task.loadDocument(context, SOURCE_FILE);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void loadDocument_CatchErrorCodeIoException_ThrowOfficeException() throws Exception {

    final XComponentLoader loader = mock(XComponentLoader.class);
    final OfficeContext context = mock(OfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willThrow(ErrorCodeIOException.class);
    given(context.getComponentLoader()).willReturn(loader);

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    try {
      task.loadDocument(context, SOURCE_FILE);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(ErrorCodeIOException.class);
    }
  }

  @Test
  public void loadDocument_CatchIoException_ThrowOfficeException() throws Exception {

    final XComponentLoader loader = mock(XComponentLoader.class);
    final OfficeContext context = mock(OfficeContext.class);
    given(
            loader.loadComponentFromURL(
                isA(String.class), isA(String.class), isA(int.class), isA(PropertyValue[].class)))
        .willThrow(IOException.class);
    given(context.getComponentLoader()).willReturn(loader);

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    try {
      task.loadDocument(context, SOURCE_FILE);
    } catch (Exception ex) {
      assertThat(ex)
          .isExactlyInstanceOf(OfficeException.class)
          .hasCauseExactlyInstanceOf(IOException.class);
    }
  }

  @Test
  public void closeDocument_WithNull_doNothing() {

    final FooOfficeTask task = new FooOfficeTask(new FooSourceSpecs(SOURCE_FILE));
    task.closeDocument(null);
  }
}
