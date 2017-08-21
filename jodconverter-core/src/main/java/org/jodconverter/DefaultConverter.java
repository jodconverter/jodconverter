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

import java.util.HashMap;
import java.util.Map;

import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.filter.RefreshFilter;
import org.jodconverter.job.AbstractConversionJobWithSourceFormatUnspecified;
import org.jodconverter.job.AbstractConversionJobWithTargetFormatUnspecified;
import org.jodconverter.job.AbstractConverter;
import org.jodconverter.job.AbstractSourceDocumentSpecs;
import org.jodconverter.job.AbstractTargetDocumentSpecs;
import org.jodconverter.office.InstalledOfficeManagerHolder;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.task.DefaultConversionTask;

/**
 * Default implementation of a document converter. This implementation will use a provided office
 * manager to perform document conversion. The provided office manager must be started in order to
 * be used by this converter.
 *
 * @see DocumentConverter
 * @see OfficeManager
 */
public class DefaultConverter extends AbstractConverter {

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link DefaultConverter} using with default configuration. The {@link
   * OfficeManager} that will be used is the one holden by the {@link InstalledOfficeManagerHolder}
   * class, if any.
   *
   * @return A {@link DefaultConverter} with default configuration.
   */
  public static DefaultConverter make() {

    return builder().build();
  }

  /**
   * Creates a new {@link DefaultConverter} using the specified {@link OfficeManager} with default
   * configuration.
   *
   * @param officeManager The {@link OfficeManager} the converter will use to convert document.
   * @return A {@link DefaultConverter} with default configuration.
   */
  public static DefaultConverter make(final OfficeManager officeManager) {
    return builder().officeManager(officeManager).build();
  }

  private DefaultConverter(
      final OfficeManager officeManager,
      final DocumentFormatRegistry formatRegistry,
      final Map<String, Object> defaultLoadProperties) {
    super(officeManager, formatRegistry, defaultLoadProperties);
  }

  @Override
  protected AbstractConversionJobWithSourceFormatUnspecified convert(
      final AbstractSourceDocumentSpecs source) {

    return new DefaultConversionJobWithSourceFormatUnspecified(source);
  }

  /**
   * Sets the default properties to use when we load (open) a document before a conversion,
   * regardless the input type of the document. This function should be called only by
   * OfficeDocumentConverter for backward compatibility.
   *
   * @param defaultLoadProperties The default properties to apply when loading a document.
   */
  void setDefaultLoadProperties(final Map<String, Object> defaultLoadProperties) {

    if (this.defaultLoadProperties == null) {
      this.defaultLoadProperties = new HashMap<>();
    }

    this.defaultLoadProperties.clear();
    this.defaultLoadProperties.putAll(defaultLoadProperties);
  }

  private class DefaultConversionJobWithSourceFormatUnspecified
      extends AbstractConversionJobWithSourceFormatUnspecified {

    private DefaultConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source) {
      super(source, DefaultConverter.this.officeManager, DefaultConverter.this.formatRegistry);
    }

    @Override
    protected AbstractConversionJobWithTargetFormatUnspecified to(
        final AbstractTargetDocumentSpecs target) {

      return new DefaultConversionJobWithTargetFormatUnspecified(
          source, target, filterChain == null ? RefreshFilter.CHAIN : filterChain);
    }
  }

  private class DefaultConversionJobWithTargetFormatUnspecified
      extends AbstractConversionJobWithTargetFormatUnspecified {

    private DefaultConversionJobWithTargetFormatUnspecified(
        final AbstractSourceDocumentSpecs source,
        final AbstractTargetDocumentSpecs target,
        final FilterChain filterChain) {
      super(source, target, filterChain);
    }

    @Override
    public void doExecute() throws OfficeException {

      // Create a default conversion task and execute it
      final DefaultConversionTask task =
          new DefaultConversionTask(source, target, defaultLoadProperties, filterChain);
      officeManager.execute(task);
    }
  }

  /**
   * A builder for constructing a {@link DefaultConverter}.
   *
   * @see DefaultConverter
   */
  public static final class Builder extends AbstractConverter.AbstractConverterBuilder<Builder> {

    // Private ctor so only DefaultConverter can create an instance of this builder.
    private Builder() {
      super();
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    public DefaultConverter build() {

      // Create the converter
      return new DefaultConverter(officeManager, formatRegistry, defaultLoadProperties);
    }
  }
}
