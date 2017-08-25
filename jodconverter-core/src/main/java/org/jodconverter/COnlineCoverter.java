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

package org.jodconverter;

import java.util.Map;

import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.filter.RefreshFilter;
import org.jodconverter.job.*;
import org.jodconverter.office.InstalledOfficeManagerHolder;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.COnlineConversationTask;

public class COnlineCoverter extends AbstractConverter {

  protected String connectionURL = null;

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static COnlineCoverter.Builder builder() {
    return new COnlineCoverter.Builder();
  }

  /**
   * Creates a new {@link COnlineCoverter} using with default configuration. The {@link
   * OfficeManager} that will be used is the one holden by the {@link InstalledOfficeManagerHolder}
   * class, if any.
   *
   * @param connectionURL URL of remote LibreOffice server
   * @return A {@link COnlineCoverter} with default configuration.
   */
  public static COnlineCoverter make(final String connectionURL) {
    COnlineCoverter converter = builder().build();
    converter.connectionURL = connectionURL;
    return converter;
  }

  private COnlineCoverter(
      final OfficeManager officeManager,
      final DocumentFormatRegistry formatRegistry,
      final Map<String, Object> defaultLoadProperties,
      final String connectionURL) {
    super(officeManager, formatRegistry, defaultLoadProperties);
    this.connectionURL = connectionURL;
  }

  @Override
  protected AbstractConversionJobWithSourceFormatUnspecified convert(
      AbstractSourceDocumentSpecs source) {
    return new COnlineCoverter.COOnlineConversionJobWithSourceFormatUnspecified(source);
  }

  private class COOnlineConversionJobWithSourceFormatUnspecified
      extends AbstractConversionJobWithSourceFormatUnspecified {

    private COOnlineConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source) {
      super(source, COnlineCoverter.this.officeManager, COnlineCoverter.this.formatRegistry);
    }

    @Override
    protected AbstractConversionJobWithTargetFormatUnspecified to(
        final AbstractTargetDocumentSpecs target) {

      return new COnlineCoverter.COnlineConversionJobWithTargetFormatUnspecified(
          source, target, filterChain == null ? RefreshFilter.CHAIN : filterChain);
    }
  }

  private class COnlineConversionJobWithTargetFormatUnspecified
      extends AbstractConversionJobWithTargetFormatUnspecified {

    private COnlineConversionJobWithTargetFormatUnspecified(
        final AbstractSourceDocumentSpecs source,
        final AbstractTargetDocumentSpecs target,
        final FilterChain filterChain) {
      super(source, target, filterChain);
    }

    @Override
    public void doExecute() throws OfficeException {

      // Create a default conversion task and execute it
      final COnlineConversationTask task =
          new COnlineConversationTask(
              source, target, defaultLoadProperties, filterChain, connectionURL);
      officeManager.execute(task);
    }
  }

  /** A builder for constructing a {@link COnlineCoverter}. */
  public static final class Builder extends AbstractConverter.AbstractConverterBuilder<Builder> {

    private String connectionURL = null;

    public COnlineCoverter build(final String connectionURL) {
      COnlineCoverter converter = build();
      converter.connectionURL = connectionURL;
      return converter;
    }

    // Private ctor so only DefaultConverter can create an instance of this builder.
    private Builder() {}

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    public COnlineCoverter build() {

      // Create the converter
      return new COnlineCoverter(
          officeManager, formatRegistry, defaultLoadProperties, connectionURL);
    }
  }
}
