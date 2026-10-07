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

package org.jodconverter.local;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.sun.star.document.UpdateDocMode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.job.*;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.core.util.AssertUtils;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.filter.DefaultFilterChain;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.FilterChain;
import org.jodconverter.local.filter.RefreshFilter;
import org.jodconverter.local.filter.text.DocumentInserterFilter;
import org.jodconverter.local.office.AttachedOfficeManager;
import org.jodconverter.local.task.LoadDocumentMode;
import org.jodconverter.local.task.LocalConversionTask;
import org.jodconverter.local.task.PageImagesTask;

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
   * The default behavior regarding the loading of a document. (string value used in the spring-boot
   * project)
   */
  public static final String DEFAULT_LOAD_DOCUMENT_MODE_STRING = "auto";

  /**
   * The properties which are applied by default when loading a document if not manually overridden.
   */
  public static final Map<String, Object> DEFAULT_LOAD_PROPERTIES;

  private final LoadDocumentMode loadDocumentMode;
  private final Map<String, Object> loadProperties;
  private final Map<String, Object> storeProperties;
  private final FilterChain filterChain;

  static {
    DEFAULT_LOAD_PROPERTIES =
        Map.of("Hidden", true, "ReadOnly", true, "UpdateDocMode", UpdateDocMode.NO_UPDATE);
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
      final FilterChain filterChain,
      final List<TargetOptions> defaultTargetOptions) {
    super(officeManager, formatRegistry, defaultTargetOptions);

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

  /**
   * Exports each page of a presentation or a drawing as an image: one image per slide or draw page,
   * written in the directory given to the job.
   *
   * <pre>
   * List&lt;File&gt; images =
   *     converter.exportPages(new File("deck.pptx")).to(new File("out")).execute();
   * </pre>
   *
   * @param source The presentation or drawing.
   * @return The export job: the directory, the format, the size and the pages are given to it, then
   *     it is executed.
   */
  public @NonNull PageImagesJob exportPages(final @NonNull File source) {
    Objects.requireNonNull(source, "source must not be null");
    final var baseName = FileUtils.getBaseName(source.getName());
    return new PageImagesJob(
        sourceSpecs(source),
        baseName == null || baseName.isBlank() ? "page" : baseName,
        officeManager,
        this::pageImagesTask);
  }

  /**
   * Exports each page of a presentation or a drawing read from a stream as an image. The stream is
   * closed once read. The images are named {@code page-01.png}, {@code page-02.png}... unless a
   * base name is given to the job.
   *
   * @param source The presentation or drawing.
   * @return The export job.
   */
  public @NonNull PageImagesJob exportPages(final @NonNull InputStream source) {
    return exportPages(source, true);
  }

  /**
   * Exports each page of a presentation or a drawing read from a stream as an image. The images are
   * named {@code page-01.png}, {@code page-02.png}... unless a base name is given to the job.
   *
   * @param source The presentation or drawing.
   * @param closeStream Whether the stream is closed once read.
   * @return The export job.
   */
  public @NonNull PageImagesJob exportPages(
      final @NonNull InputStream source, final boolean closeStream) {
    Objects.requireNonNull(source, "source must not be null");
    return new PageImagesJob(
        new SourceDocumentSpecsFromInputStream(source, officeManager, closeStream),
        "page",
        officeManager,
        this::pageImagesTask);
  }

  // Creates the task of a page images job, with the load properties and the filters of this
  // converter.
  private PageImagesTask pageImagesTask(final PageImagesJob job) {
    return new PageImagesTask(
        job.getSource(),
        useStreamAdapters(),
        loadProperties,
        filterChain,
        job.getDirectory(),
        job.getBaseName(),
        job.getFormat(),
        job.getWidth(),
        job.getHeight(),
        job.getQuality(),
        job.getPages(),
        job.isHiddenSlides());
  }

  // Whether the documents go through streams rather than files: always with the remote mode, and
  // with the auto mode when the office processes may run elsewhere.
  private boolean useStreamAdapters() {
    return loadDocumentMode == LoadDocumentMode.REMOTE
        || (loadDocumentMode == LoadDocumentMode.AUTO
            && officeManager instanceof AttachedOfficeManager);
  }

  /** Local implementation of a conversion job with source format unspecified. */
  /**
   * Merges text documents into one: the first document is loaded, the others are inserted at its
   * end, each one starting on a new page, and the result is converted like any document.
   *
   * <pre>
   * converter
   *     .merge(new File("chapter1.docx"), new File("chapter2.docx"), new File("chapter3.docx"))
   *     .to(new File("book.pdf"))
   *     .execute();
   * </pre>
   *
   * <p>The filters of the converter are applied after the insertions. To insert the documents
   * without a page break, or at another place, use a {@link
   * org.jodconverter.local.filter.text.DocumentInserterFilter} in the filter chain instead.
   *
   * @param first The first document; its page styles, headers and footers are those of the result.
   * @param others The documents inserted after it, in order.
   * @return The conversion job of the merged document.
   * @throws IllegalArgumentException If a document to insert does not exist.
   */
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified merge(
      final @NonNull File first, final @NonNull File... others) {
    Objects.requireNonNull(others, "others must not be null");
    return merge(Stream.concat(Stream.of(first), Stream.of(others)).toList());
  }

  /**
   * Merges text documents into one: the first document is loaded, the others are inserted at its
   * end, each one starting on a new page, and the result is converted like any document.
   *
   * @param documents The documents, in order; at least one.
   * @return The conversion job of the merged document.
   * @throws IllegalArgumentException If the list is empty, or if a document to insert does not
   *     exist.
   * @see #merge(File, File...)
   */
  public @NonNull ConversionJobWithOptionalSourceFormatUnspecified merge(
      final @NonNull List<@NonNull File> documents) {
    AssertUtils.notEmpty(documents, "documents must not be null nor empty");
    final var inserters = new ArrayList<Filter>();
    for (final var document : documents.subList(1, documents.size())) {
      Objects.requireNonNull(document, "documents must not contain null");
      AssertUtils.isTrue(document.isFile(), "File not found: " + document);
      inserters.add(new DocumentInserterFilter(document, true));
    }
    return new LocalConversionJobWithSourceFormatUnspecified(
        sourceSpecs(documents.get(0)), jobFilterChain(inserters));
  }

  // The filters of a job, followed by the filters of the converter.
  private FilterChain jobFilterChain(final List<Filter> filters) {
    final var converterChain = filterChain == null ? RefreshFilter.CHAIN : filterChain;
    final var all = new ArrayList<>(filters);
    all.add(
        (context, document, chain) -> {
          converterChain.copy().doFilter(context, document);
          chain.doFilter(context, document);
        });
    return new DefaultFilterChain(false, all.toArray(new Filter[0]));
  }

  private class LocalConversionJobWithSourceFormatUnspecified
      extends AbstractConversionJobWithSourceFormatUnspecified {

    // The filter chain of this job, or null for the filter chain of the converter.
    private final @Nullable FilterChain jobFilterChain;

    private LocalConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source) {
      this(source, null);
    }

    private LocalConversionJobWithSourceFormatUnspecified(
        final AbstractSourceDocumentSpecs source, final @Nullable FilterChain jobFilterChain) {
      super(source, LocalConverter.this.officeManager, LocalConverter.this.formatRegistry);
      this.jobFilterChain = jobFilterChain;
      setDefaultTargetOptions(LocalConverter.this.defaultTargetOptions);
    }

    @Override
    protected @NonNull AbstractConversionJob to(final @NonNull AbstractTargetDocumentSpecs target) {
      return new LocalConversionJob(source, target, jobFilterChain);
    }
  }

  /** Local implementation of a conversion job. */
  private class LocalConversionJob extends AbstractConversionJob {

    private final @Nullable FilterChain jobFilterChain;

    private LocalConversionJob(
        final AbstractSourceDocumentSpecs source,
        final AbstractTargetDocumentSpecs target,
        final @Nullable FilterChain jobFilterChain) {
      super(source, target);
      this.jobFilterChain = jobFilterChain;
    }

    @Override
    protected @NonNull OfficeManager getOfficeManager() {
      return officeManager;
    }

    @Override
    protected @NonNull OfficeTask createTask() {

      return new LocalConversionTask(
          source,
          target,
          useStreamAdapters(),
          loadProperties,
          storeProperties,
          jobFilterChain == null ? filterChain : jobFilterChain);
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
      var manager = officeManager;
      if (manager == null) {
        manager = InstalledOfficeManagerHolder.getInstance();
        if (manager == null) {
          throw new IllegalStateException(
              "An office manager is required in order to build a converter.");
        }
      }

      final var loadProperties = new HashMap<String, Object>();
      if (applyDefaultLoadProperties) {
        loadProperties.putAll(DEFAULT_LOAD_PROPERTIES);
        if (useUnsafeQuietUpdate) {
          loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
        }
      }
      if (this.loadProperties != null) {
        loadProperties.putAll(this.loadProperties);
      }

      // Create the converter, with its own copies of the maps so that a reused builder does not
      // change it.
      return new LocalConverter(
          manager,
          formatRegistry == null ? DefaultDocumentFormatRegistry.getInstance() : formatRegistry,
          loadDocumentMode,
          Collections.unmodifiableMap(loadProperties),
          storeProperties == null
              ? Map.of()
              : Collections.unmodifiableMap(new HashMap<>(storeProperties)),
          filterChain,
          defaultTargetOptions);
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
     * Specifies how a document is loaded/stored when converting a document, whether it is loaded
     * assuming the office process has access to the file on disk or not. If not, the conversion
     * process will use stream adapters
     *
     * <p>&nbsp; <b><i>Default</i></b>: LoadDocumentMode.AUTO
     *
     * @param loadDocumentMode The load document mode.
     * @return This builder instance.
     */
    public @NonNull Builder loadDocumentMode(final @Nullable String loadDocumentMode) {

      return StringUtils.isBlank(loadDocumentMode)
          ? this
          : loadDocumentMode(LoadDocumentMode.valueOf(loadDocumentMode.toUpperCase(Locale.ROOT)));
    }

    /**
     * Specifies a property, for this converter, that will be applied when a document is loaded
     * during a conversion task, regardless of the input format of the document.
     *
     * <p>When building the load properties map that will be used to load a source document, the
     * load properties of the input {@link org.jodconverter.core.document.DocumentFormat}, if any,
     * are put in the map first. Then, the {@link #DEFAULT_LOAD_PROPERTIES}, if required, are added
     * to the map. Finally, any properties specified with this method or {@link
     * #loadProperties(Map)} are put in the map.
     *
     * <p>Any property set here will override the property with the same name from the input
     * document format or the default load properties.
     *
     * @param name The property name.
     * @param value The property value.
     * @return This builder instance.
     */
    public @NonNull Builder loadProperty(final @NonNull String name, final @NonNull Object value) {

      Objects.requireNonNull(name, "name must not be null");
      Objects.requireNonNull(value, "value must not be null");
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
     * to the map. Finally, any properties specified with {@link #loadProperty(String, Object)} or
     * this method are put in the map.
     *
     * <p>Any property set here will override the property with the same name from the input
     * document format or the default load properties.
     *
     * @param loadProperties A map containing the properties to apply when loading a document.
     * @return This builder instance.
     */
    public @NonNull Builder loadProperties(
        final @NonNull Map<@NonNull String, @NonNull Object> loadProperties) {

      Objects.requireNonNull(loadProperties, "loadProperties must not be null");
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

      Objects.requireNonNull(filterChain, "filterChain must not be null");
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

      Objects.requireNonNull(name, "name must not be null");
      Objects.requireNonNull(value, "value must not be null");
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

      Objects.requireNonNull(storeProperties, "storeProperties must not be null");
      if (this.storeProperties == null) {
        this.storeProperties = new HashMap<>();
      }
      this.storeProperties.putAll(storeProperties);
      return this;
    }
  }
}
