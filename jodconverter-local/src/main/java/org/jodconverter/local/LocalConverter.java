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

package org.jodconverter.local;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sun.star.document.UpdateDocMode;
import org.apache.commons.lang3.Validate;

import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.job.AbstractConversionJob;
import org.jodconverter.core.job.AbstractConversionJobWithSourceFormatUnspecified;
import org.jodconverter.core.job.AbstractConverter;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.job.AbstractTargetDocumentSpecs;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.filter.DefaultFilterChain;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.task.LocalConversionTask;

/**
 * Default implementation of a document converter. This implementation will use a provided office
 * manager to perform document conversion. The provided office manager must be started in order to
 * be used by this converter.
 *
 * @see org.jodconverter.core.DocumentConverter
 * @see org.jodconverter.core.office.OfficeManager
 */
public class LocalConverter extends AbstractConverter {

  /**
   * The properties which are applied by default when loading a document if not manually overridden.
   */
  public static final Map<String, Object> DEFAULT_LOAD_PROPERTIES;

  private Map<String, Object> loadProperties;
  private FilterChain filterChain;
  private final Map<String, Object> storeProperties;

  static {
    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
    DEFAULT_LOAD_PROPERTIES = Collections.unmodifiableMap(loadProperties);
  }

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link LocalConverter} with default configuration. The {@link
   * org.jodconverter.core.office.OfficeManager} that will be used is the one holden by the {@link
   * org.jodconverter.core.office.InstalledOfficeManagerHolder} class, if any.
   *
   * @return A {@link LocalConverter} with default configuration.
   */
  public static LocalConverter make() {

    return builder().build();
  }

  /**
   * Creates a new {@link LocalConverter} using the specified {@link
   * org.jodconverter.core.office.OfficeManager} with default configuration.
   *
   * @param officeManager The {@link org.jodconverter.core.office.OfficeManager} the converter will
   *     use to convert document.
   * @return A {@link org.jodconverter.local.LocalConverter} with default configuration.
   */
  public static LocalConverter make(final OfficeManager officeManager) {
    return builder().officeManager(officeManager).build();
  }

  private LocalConverter(
      final OfficeManager officeManager,
      final DocumentFormatRegistry formatRegistry,
      final Map<String, Object> loadProperties,
      final FilterChain filterChain,
      final Map<String, Object> storeProperties) {
    super(officeManager, formatRegistry);

    this.loadProperties = loadProperties;
    this.filterChain = filterChain;
    this.storeProperties = storeProperties;
  }

  @Override
  protected AbstractConversionJobWithSourceFormatUnspecified convert(
      final AbstractSourceDocumentSpecs source) {

    return new LocalConversionJobWithSourceFormatUnspecified(source);
  }

  /** Local implementation of a conversion job with source format unspecified. */
  private class LocalConversionJobWithSourceFormatUnspecified
      extends AbstractConversionJobWithSourceFormatUnspecified {

    private LocalConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source) {
      super(source, LocalConverter.this.officeManager, LocalConverter.this.formatRegistry);
    }

    @Override
    protected AbstractConversionJob to(final AbstractTargetDocumentSpecs target) {

      return new LocalConversionJob(source, target);
    }
  }

  /** Local implementation of a conversion job. */
  private class LocalConversionJob extends AbstractConversionJob {

    private LocalConversionJob(
        final AbstractSourceDocumentSpecs source, final AbstractTargetDocumentSpecs target) {
      super(source, target);
    }

    @Override
    public void doExecute() throws OfficeException {

      // Create a default conversion task and execute it
      final LocalConversionTask task =
          new LocalConversionTask(source, target, loadProperties, filterChain, storeProperties);
      officeManager.execute(task);
    }
  }

  /**
   * A builder for constructing a {@link LocalConverter}.
   *
   * @see LocalConverter
   */
  public static final class Builder extends AbstractConverterBuilder<Builder> {

    private Map<String, Object> loadProperties;
    private FilterChain filterChain;
    private Map<String, Object> storeProperties;

    // Private ctor so only LocalConverter can create an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public LocalConverter build() {

      // Create the converter
      return new LocalConverter(
          officeManager, formatRegistry, loadProperties, filterChain, storeProperties);
    }

    /**
     * Specifies the load properties, for this converter, that will be applied when a document is
     * loaded during a conversion task, regardless of the input format of the document.
     *
     * <p>Using this function will replace the default load properties map.
     *
     * @param properties A map containing the properties to apply when loading a document.
     * @return This builder instance.
     */
    public Builder loadProperties(final Map<String, Object> properties) {

      this.loadProperties = properties;
      return this;
    }

    /**
     * Specifies the filters to apply when converting a document. Filter may be used to modify the
     * document before the conversion (after it has been loaded). Filters are applied in the same
     * order they appear as arguments.
     *
     * @param filters The filters to be applied after the document is loaded and before it is stored
     *     (converted) in the new document format.
     * @return This builder instance.
     */
    public Builder filterChain(final Filter... filters) {

      Validate.notEmpty(filters);
      this.filterChain = new DefaultFilterChain(filters);
      return this;
    }

    /**
     * Specifies the whole filter chain to apply when converting a document. A FilterChain is used
     * to modify the document before the conversion (after it has been loaded). Filters are applied
     * in the same order they appear in the chain.
     *
     * @param filterChain The FilterChain to be applied after the document is loaded and before it
     *     is stored (converted) in the new document format.
     * @return This builder instance.
     */
    public Builder filterChain(final FilterChain filterChain) {

      Validate.notNull(filterChain);
      this.filterChain = filterChain;
      return this;
    }

    /**
     * Specifies the properties that will be applied when a document is stored during the conversion
     * task.
     *
     * <p>Custom properties are applied after the store properties of the target {@link
     * org.jodconverter.core.document.DocumentFormat}.
     *
     * @param properties A map containing the custom properties to apply when storing a document.
     * @return This builder instance.
     */
    public Builder storeProperties(final Map<String, Object> properties) {

      this.storeProperties = properties;
      return this;
    }
  }
}
