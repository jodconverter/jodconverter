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

package org.jodconverter.job;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import com.sun.star.document.UpdateDocMode;

import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.office.OfficeManager;

/**
 * Base class for all document converter implementations.
 *
 * @see DocumentSpecs
 */
public abstract class AbstractConverter implements DocumentConverter {

  protected final OfficeManager officeManager;
  protected final DocumentFormatRegistry formatRegistry;
  protected final Map<String, Object> defaultLoadProperties;

  // Provides default properties to use when we load (open) a document before
  // a conversion, regardless the input type of the document.
  private static Map<String, Object> createDefaultLoadProperties() {

    final Map<String, Object> loadProperties = new HashMap<>();
    loadProperties.put("Hidden", true);
    loadProperties.put("ReadOnly", true);
    loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
    return loadProperties;
  }

  protected AbstractConverter(
      final OfficeManager officeManager,
      final DocumentFormatRegistry formatRegistry,
      final Map<String, Object> defaultLoadProperties) {
    super();

    // An office manager is required
    if (officeManager == null) {
      throw new IllegalStateException(
          "An office manager is required in order to build a converter.");
    }

    this.officeManager = officeManager;
    this.formatRegistry =
        formatRegistry == null ? DefaultDocumentFormatRegistry.getInstance() : formatRegistry;
    this.defaultLoadProperties =
        defaultLoadProperties == null ? createDefaultLoadProperties() : defaultLoadProperties;
  }

  @Override
  public ConversionJobWithSourceSpecified convert(final File source) {

    DocumentFormat sourceFormat =
        formatRegistry.getFormatByExtension(FilenameUtils.getExtension(source.getName()));
    Validate.notNull(sourceFormat, "Unsupported source document format");
    return convert(FileSourceDocumentSpecs.make(source, sourceFormat));
  }

  @Override
  public ConversionJobWithSourceSpecified convert(final File source, final DocumentFormat format) {

    return convert(FileSourceDocumentSpecs.make(source, format));
  }

  @Override
  public DocumentFormatRegistry getFormatRegistry() {
    return formatRegistry;
  }

  /** A builder for constructing an {@link AbstractConverter}. */
  public abstract static class AbstractConverterBuilder<T extends AbstractConverterBuilder<T>> {

    protected OfficeManager officeManager;
    protected DocumentFormatRegistry formatRegistry;
    protected Map<String, Object> defaultLoadProperties;

    // Protected ctor so only subclasses can initialize an instance of this builder.
    protected AbstractConverterBuilder() {}

    /**
     * Specifies the {@link OfficeManager} the converter will use to execute office tasks.
     *
     * @param manager The office manager this converter will use.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T officeManager(final OfficeManager manager) {

      Validate.notNull(manager);
      this.officeManager = manager;
      return (T) this;
    }

    /**
     * Specifies the {@link DocumentFormatRegistry} the which contains the document formats that
     * will be supported by this converter.
     *
     * @param registry The registry that contains the supported formats.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T formatRegistry(final DocumentFormatRegistry registry) {

      this.formatRegistry = registry;
      return (T) this;
    }

    /**
     * Specifies the default properties that will be applied when a document is loaded during a
     * conversion task, regardless of the input format of the document.
     *
     * <p>Using this function will replace the default load properties map of the builder, so be
     * sure to call it first if you add other properties afterward.
     *
     * @param properties A map containing the default properties to apply when loading a document.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T defaultLoadProperties(final Map<String, Object> properties) {

      this.defaultLoadProperties = properties;
      return (T) this;
    }

    /**
     * Adds default properties that will be applied when a document is loaded during a conversion
     * task, regardless of the input format of the document.
     *
     * @param properties A map containing default properties to apply when loading a document.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T addDefaultLoadProperties(final Map<String, Object> properties) {

      if (properties != null && properties.size() != 0) {
        if (defaultLoadProperties == null) {
          defaultLoadProperties = createDefaultLoadProperties();
        }
        defaultLoadProperties.putAll(properties);
      }
      return (T) this;
    }

    /**
     * Adds a the default property that will be applied when a document is loaded during a
     * conversion task, regardless of the input format of the document.
     *
     * @param name The property name of the property to apply when loading a document.
     * @param value The property value of the property to apply when loading a document.
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    public T addDefaultLoadProperty(final String name, final Object value) {

      Validate.notBlank(name);
      Validate.notNull(value);
      if (defaultLoadProperties == null) {
        defaultLoadProperties = createDefaultLoadProperties();
      }
      defaultLoadProperties.put(name, value);
      return (T) this;
    }

    /**
     * Creates the converter that is specified by this builder.
     *
     * @return The converter that is specified by this builder.
     */
    protected abstract AbstractConverter build();
  }
}
