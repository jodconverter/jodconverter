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

import com.sun.star.document.UpdateDocMode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.job.AbstractConversionJob;
import org.jodconverter.core.job.AbstractConversionJobWithSourceFormatUnspecified;
import org.jodconverter.core.job.AbstractConverter;
import org.jodconverter.core.job.AbstractSourceDocumentSpecs;
import org.jodconverter.core.job.AbstractTargetDocumentSpecs;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.local.filter.DefaultFilterChain;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.jodconverter.local.task.LoadDocumentMode;
import org.jodconverter.local.task.LocalConversionTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of a document converter. This implementation will use a provided office
 * manager to perform document conversion. The provided office manager must be started in order to
 * be used by this converter.
 *
 * @see org.jodconverter.core.DocumentConverter
 * @see org.jodconverter.core.office.OfficeManager
 */
public final class LocalConverter extends AbstractConverter {

  /** The default behavior regarding the usage of the default load properties. */
  public static final boolean DEFAULT_APPLY_DEFAULT_LOAD_PROPS = true;

  /**
   * The default behavior regarding the default load property {@code UpdateDocMode}, which has been
   * changed from {@code UpdateDocMode.QUIET_UPDATE} to {@code UpdateDocMode.NO_UPDATE} for security
   * reason.
   */
  public static final boolean DEFAULT_USE_UNSAFE_QUIET_UPDATE = false;

  /** The default behavior regarding the loading of a document. */
  public static final LoadDocumentMode DEFAULT_LOAD_DOCUMENT_MODE = LoadDocumentMode.AUTO;

  /**
   * The properties which are applied by default when loading a document if not manually overridden.
   */
  public static final Map<String, Object> DEFAULT_LOAD_PROPERTIES;

  private final LoadDocumentMode loadDocumentMode;
  private final Map<String, Object> loadProperties;
  private final Map<String, Object> storeProperties;
  private final FilterChain filterChain;

  static {
    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.NO_UPDATE);

    DEFAULT_LOAD_PROPERTIES = Collections.unmodifiableMap(loadProperties);
  }

  /**
   * Creates a new builder instance.
   *
   * @return A new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new {@link LocalConverter} with default configuration. The {@link
   * org.jodconverter.core.office.OfficeManager} that will be used is the one holden by the {@link
   * org.jodconverter.core.office.InstalledOfficeManagerHolder} class, if any.
   *
   * @return A {@link LocalConverter} with default configuration.
   */
  public static @NonNull LocalConverter make() {
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
  public static @NonNull LocalConverter make(final @NonNull OfficeManager officeManager) {
    return builder().officeManager(officeManager).build();
  }

  private LocalConverter(
      final OfficeManager officeManager,
      final DocumentFormatRegistry formatRegistry,
      final LoadDocumentMode loadDocumentMode,
      final Map<String, Object> loadProperties,
      final Map<String, Object> storeProperties,
      final FilterChain filterChain) {
    super(officeManager, formatRegistry);

    this.loadDocumentMode = loadDocumentMode;
    this.loadProperties = loadProperties;
    this.storeProperties = storeProperties;
    this.filterChain = filterChain;
  }

  @Override
  protected @NonNull AbstractConversionJobWithSourceFormatUnspecified convert(
      final @NonNull AbstractSourceDocumentSpecs source) {

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
    protected @NonNull AbstractConversionJob to(final @NonNull AbstractTargetDocumentSpecs target) {
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

      // Determine whether we must use stream adapters.
      final boolean useStreamAdapters =
          loadDocumentMode == LoadDocumentMode.REMOTE
              || loadDocumentMode == LoadDocumentMode.AUTO
                  && officeManager instanceof ExternalOfficeManager;

      // Create a conversion task and execute it.
      final LocalConversionTask task =
          new LocalConversionTask(
              source, target, useStreamAdapters, loadProperties, storeProperties, filterChain);
      officeManager.execute(task);
    }
  }

  /**
   * A builder for constructing a {@link LocalConverter}.
   *
   * @see LocalConverter
   */
  public static final class Builder extends AbstractConverterBuilder<Builder> {

    private boolean applyDefaultLoadProperties = DEFAULT_APPLY_DEFAULT_LOAD_PROPS;
    private boolean useUnsafeQuietUpdate = DEFAULT_USE_UNSAFE_QUIET_UPDATE;
    private LoadDocumentMode loadDocumentMode = DEFAULT_LOAD_DOCUMENT_MODE;
    private FilterChain filterChain;
    private Map<String, Object> loadProperties;
    private Map<String, Object> storeProperties;

    // Private constructor so only LocalConverter can create an instance of this builder.
    private Builder() {
      super();
    }

    @Override
    public @NonNull LocalConverter build() {

      // An office manager is required.
      OfficeManager manager = officeManager;
      if (manager == null) {
        manager = InstalledOfficeManagerHolder.getInstance();
        if (manager == null) {
          throw new IllegalStateException(
              "An office manager is required in order to build a converter.");
        }
      }

      final Map<String, Object> loadProperties = new HashMap<>();
      if (applyDefaultLoadProperties) {
        loadProperties.putAll(DEFAULT_LOAD_PROPERTIES);
        if (useUnsafeQuietUpdate) {
          loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
        }
      }
      if (this.loadProperties != null) {
        loadProperties.putAll(this.loadProperties);
      }

      // Create the converter
      return new LocalConverter(
          manager,
          formatRegistry == null ? DefaultDocumentFormatRegistry.getInstance() : formatRegistry,
          loadDocumentMode,
          loadProperties,
          storeProperties,
          filterChain);
    }

    /**
     * Specifies this converter will apply the default load properties when loading a source
     * document.
     *
     * <p>&nbsp; <b><i>Default</i></b>: true
     *
     * <p>Default load properties are:
     *
     * <ul>
     *   <li><b>Hidden</b>: true
     *   <li><b>ReadOnly</b>: true
     *   <li><b>UpdateDocMode</b>: UpdateDocMode.NO_UPDATE
     * </ul>
     *
     * <p>When building the load properties map that will be used to load a source document, the
     * load properties of the input {@link org.jodconverter.core.document.DocumentFormat}, if any,
     * are put in the map first. Then, the {@link #DEFAULT_LOAD_PROPERTIES}, if required, are added
     * to the map. Finally, any properties specified in the {@link #loadProperty(String, Object)} or
     * {@link #loadProperties(Map)} are put in the map.
     *
     * @param applyDefaultLoadProperties {@code true} to apply the default load properties {@code
     *     false} otherwise.
     * @return This builder instance.
     */
    public @NonNull Builder applyDefaultLoadProperties(final boolean applyDefaultLoadProperties) {

      this.applyDefaultLoadProperties = applyDefaultLoadProperties;
      return this;
    }

    /**
     * Specifies whether this converter will use the unsafe {@code UpdateDocMode.QUIET_UPDATE} as
     * default for the {@code UpdateDocMode} load property, which was the default until JODConverter
     * version 4.4.4.
     *
     * <p>See this article for more detail;s about the security issue:
     *
     * <p><a
     * href="https://buer.haus/2019/10/18/a-tale-of-exploitation-in-spreadsheet-file-conversions/">A
     * Tale of Exploitation in Spreadsheet File Conversions</a>
     *
     * @param useUnsafeQuietUpdate {@code true} to use the unsafe quiet update property, {@code
     *     false} otherwise.
     * @return This builder instance.
     */
    public @NonNull Builder useUnsafeQuietUpdate(final boolean useUnsafeQuietUpdate) {

      this.useUnsafeQuietUpdate = useUnsafeQuietUpdate;
      return this;
    }

    /**
     * Specifies how a document is loaded/stored when converting a document, whether it is loaded
     * assuming the office process has access to the file on disk or not. If not, the conversion
     * process will use stream adapters
     *
     * <p>&nbsp; <b><i>Default</i></b>: LoadDocumentMode.AUTO
     *
     * @param loadDocumentMode The load document mode.
     * @return This builder instance.
     */
    public @NonNull Builder loadDocumentMode(final @Nullable LoadDocumentMode loadDocumentMode) {

      if (loadDocumentMode != null) {
        this.loadDocumentMode = loadDocumentMode;
      }
      return this;
    }

    /**
     * Specifies a property, for this converter, that will be applied when a document is loaded
     * during a conversion task, regardless of the input format of the document.
     *
     * <p>When building the load properties map that will be used to load a source document, the
     * load properties of the input {@link org.jodconverter.core.document.DocumentFormat}, if any,
     * are put in the map first. Then, the {@link #DEFAULT_LOAD_PROPERTIES}, if required, are added
     * to the map. Finally, any properties specified in the {@link #loadProperty(String, Object)} or
     * {@link #loadProperties(Map)} are put in the map.
     *
     * <p>Any property set here will override the property with the same name from the input
     * document format or the default load properties.
     *
     * @param name The property name.
     * @param value The property value.
     * @return This builder instance.
     */
    public @NonNull Builder loadProperty(final @NonNull String name, final @NonNull Object value) {

      AssertUtils.notNull(name, "name must not be null");
      AssertUtils.notNull(value, "value must not be null");
      if (this.loadProperties == null) {
        this.loadProperties = new HashMap<>();
      }
      this.loadProperties.put(name, value);
      return this;
    }

    /**
     * Specifies properties, for this converter, that will be applied when a document is loaded
     * during a conversion task, regardless of the input format of the document.
     *
     * <p>When building the load properties map that will be used to load a source document, the
     * load properties of the input {@link org.jodconverter.core.document.DocumentFormat}, if any,
     * are put in the map first. Then, the {@link #DEFAULT_LOAD_PROPERTIES}, if required, are added
     * to the map. Finally, any properties specified in the {@link #loadProperty(String, Object)} or
     * {@link #loadProperties(Map)} are put in the map.
     *
     * <p>Any property set here will override the property with the same name from the input
     * document format or the default load properties.
     *
     * @param loadProperties A map containing the properties to apply when loading a document.
     * @return This builder instance.
     */
    public @NonNull Builder loadProperties(
        final @NonNull Map<@NonNull String, @NonNull Object> loadProperties) {

      AssertUtils.notNull(loadProperties, "loadProperties must not be null");
      if (this.loadProperties == null) {
        this.loadProperties = new HashMap<>();
      }
      this.loadProperties.putAll(loadProperties);
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
    public @NonNull Builder filterChain(final @NonNull Filter... filters) {

      AssertUtils.notEmpty(filters, "filters must not be null nor empty");
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
    public @NonNull Builder filterChain(final @NonNull FilterChain filterChain) {

      AssertUtils.notNull(filterChain, "filterChain must not be null");
      this.filterChain = filterChain;
      return this;
    }

    /**
     * Specifies a property, for this converter, that will be applied when a document is stored
     * during a conversion task, regardless of the output format of the document.
     *
     * <p>Custom properties are applied after the store properties of the target {@link
     * org.jodconverter.core.document.DocumentFormat}, so any property set here will override the
     * property with the same name from the document format.
     *
     * @param name The property name.
     * @param value The property value.
     * @return This builder instance.
     */
    public @NonNull Builder storeProperty(final @NonNull String name, final @NonNull Object value) {

      AssertUtils.notNull(name, "name must not be null");
      AssertUtils.notNull(value, "value must not be null");
      if (this.storeProperties == null) {
        this.storeProperties = new HashMap<>();
      }
      this.storeProperties.put(name, value);
      return this;
    }

    /**
     * Specifies the properties that will be applied when a document is stored during the conversion
     * task, regardless of the output format of the document.
     *
     * <p>Custom properties are applied after the store properties of the target {@link
     * org.jodconverter.core.document.DocumentFormat}, so any property set here will override the
     * property with the same name from the document format.
     *
     * @param storeProperties A map containing the custom properties to apply when storing a
     *     document.
     * @return This builder instance.
     */
    public @NonNull Builder storeProperties(
        final @NonNull Map<@NonNull String, @NonNull Object> storeProperties) {

      AssertUtils.notNull(storeProperties, "storeProperties must not be null");
      if (this.storeProperties == null) {
        this.storeProperties = new HashMap<>();
      }
      this.storeProperties.putAll(storeProperties);
      return this;
    }
  }
}
