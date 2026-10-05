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

package org.jodconverter.core.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.TargetOptions;
import org.jodconverter.core.util.AssertUtils;

/**
 * Options to convert a document to PDF, given to a conversion with {@link
 * org.jodconverter.core.job.ConversionJob#with(TargetOptions)}:
 *
 * <pre>{@code
 * converter.convert(source).to(target).with(PdfOptions.archive()).execute();
 *
 * PdfOptions options =
 *     PdfOptions.builder()
 *         .version(PdfVersion.PDF_A_2B)
 *         .images(images -> images.jpegQuality(85).maxResolution(300))
 *         .pages(pages -> pages.range("1-5"))
 *         .watermark(watermark -> watermark.text("DRAFT"))
 *         .build();
 * }</pre>
 *
 * <p>These options become the {@code FilterData} of the PDF export filter of the office
 * installation. Only the options that are set are sent, and they take precedence over the {@code
 * FilterData} of the target format and of the converter.
 *
 * <p><b>Options that are not set:</b> without any option, the office installation exports with the
 * PDF settings of its configuration, the ones of its PDF export dialog. As soon as one option is
 * set, the office installation no longer reads its configuration: every option that is not set
 * takes the default value built into the export filter, which is not always the same. With current
 * LibreOffice versions, setting any option turns off the tagged PDF and exports the comments as PDF
 * annotations, for example. Set the options that matter for the result explicitly.
 *
 * <p><b>Office versions:</b> an office installation silently ignores an option it does not know.
 * The documentation of each option tells which versions support it, and a local conversion logs a
 * warning for each option that the office installation is too old for.
 */
public final class PdfOptions implements TargetOptions {

  private static final String FILTER_DATA = "FilterData";
  private static final String PDF_EXTENSION = "pdf";
  private static final String PDF_MEDIA_TYPE = "application/pdf";

  private final Map<PdfOption, Object> values;
  private final Map<String, Object> extraFilterData;

  private PdfOptions(
      final Map<PdfOption, Object> values, final Map<String, Object> extraFilterData) {
    this.values = Collections.unmodifiableMap(new EnumMap<>(values));
    this.extraFilterData = Collections.unmodifiableMap(new LinkedHashMap<>(extraFilterData));
  }

  /**
   * Gets options for long-term archiving: PDF/A-2b, tagged, with bookmarks and images compressed
   * without loss. PDF/A-2b requires LibreOffice 6.3 or later.
   *
   * @return The options.
   */
  public static @NonNull PdfOptions archive() {
    return builder()
        .version(PdfVersion.PDF_A_2B)
        .tagged(true)
        .bookmarks(bookmarks -> bookmarks.export(true))
        .images(images -> images.lossless(true))
        .build();
  }

  /**
   * Gets options for an accessible document: PDF/UA, tagged, with bookmarks. PDF/UA requires
   * LibreOffice 7.0 or later.
   *
   * @return The options.
   */
  public static @NonNull PdfOptions accessible() {
    return builder()
        .pdfUa(true)
        .tagged(true)
        .bookmarks(bookmarks -> bookmarks.export(true))
        .build();
  }

  /**
   * Gets options for a small file: images compressed as JPEG with a quality of 75 and reduced to
   * 150 DPI, in a tagged PDF.
   *
   * @return The options.
   */
  public static @NonNull PdfOptions compact() {
    return builder()
        .tagged(true)
        .images(images -> images.lossless(false).jpegQuality(75).maxResolution(150))
        .build();
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
   * Creates a builder initialized with these options, to derive other options from them.
   *
   * @return A new builder instance.
   */
  public @NonNull Builder toBuilder() {
    final Builder builder = new Builder();
    builder.values.putAll(values);
    builder.extraFilterData.putAll(extraFilterData);
    return builder;
  }

  /**
   * Gets the properties these options set in the {@code FilterData} of the PDF export filter.
   *
   * @return The properties, by name. Empty when no option is set.
   */
  public @NonNull Map<@NonNull String, @NonNull Object> getFilterData() {
    final Map<String, Object> filterData = new LinkedHashMap<>();
    values.forEach((option, value) -> filterData.put(option.getFilterDataName(), value));
    filterData.putAll(extraFilterData);
    return filterData;
  }

  @Override
  public boolean supports(final @NonNull DocumentFormat format) {
    return PDF_EXTENSION.equalsIgnoreCase(format.getExtension())
        || PDF_MEDIA_TYPE.equalsIgnoreCase(format.getMediaType());
  }

  @Override
  public void applyTo(final @NonNull Map<@NonNull String, @NonNull Object> storeProperties) {

    final Map<String, Object> filterData = new LinkedHashMap<>();

    // Keep the FilterData properties already set by the target format or by the converter.
    final Object existing = storeProperties.get(FILTER_DATA);
    if (existing instanceof Map<?, ?> existingMap) {
      existingMap.forEach((name, value) -> filterData.put(String.valueOf(name), value));
    } else if (existing != null) {
      throw new IllegalStateException(
          "PdfOptions cannot be combined with a FilterData store property of type "
              + existing.getClass().getName()
              + "; it must be a Map");
    }

    filterData.putAll(getFilterData());

    // Without FilterData, the office installation uses the settings of its configuration.
    // Sending an empty one would change that.
    if (!filterData.isEmpty()) {
      storeProperties.put(FILTER_DATA, filterData);
    }
  }

  @Override
  public @NonNull List<@NonNull String> getUnsupportedOptions(
      final boolean libreOffice, final @NonNull String officeVersion) {

    final List<String> unsupported = new ArrayList<>();
    values.forEach(
        (option, value) -> {
          if (!option.getSupport().isSupportedBy(libreOffice, officeVersion)) {
            unsupported.add(
                option.getFilterDataName() + " " + requirement(option.getSupport(), libreOffice));
          }
          if (option == PdfOption.SELECT_PDF_VERSION) {
            final PdfVersion version = PdfVersion.fromValue((Integer) value);
            if (version != null
                && !version.getSupport().isSupportedBy(libreOffice, officeVersion)) {
              unsupported.add(
                  option.getFilterDataName()
                      + "="
                      + value
                      + " ("
                      + version
                      + ") "
                      + requirement(version.getSupport(), libreOffice));
            }
          }
        });
    return unsupported;
  }

  private static String requirement(final OfficeSupport support, final boolean libreOffice) {
    return libreOffice ? "requires " + support.describe() : "is not supported by Apache OpenOffice";
  }

  @Override
  public @NonNull String toString() {
    final StringBuilder builder = new StringBuilder("PdfOptions{");
    String separator = "";
    for (final Map.Entry<PdfOption, Object> entry : values.entrySet()) {
      builder
          .append(separator)
          .append(entry.getKey().getFilterDataName())
          .append('=')
          .append(entry.getKey().isSecret() ? "***" : entry.getValue());
      separator = ", ";
    }
    for (final Map.Entry<String, Object> entry : extraFilterData.entrySet()) {
      builder
          .append(separator)
          .append(entry.getKey())
          .append('=')
          .append(isSecretName(entry.getKey()) ? "***" : entry.getValue());
      separator = ", ";
    }
    return builder.append('}').toString();
  }

  private static boolean isSecretName(final String name) {
    final String lowerName = name.toLowerCase(Locale.ROOT);
    return lowerName.contains("password") || lowerName.endsWith("pem");
  }

  /**
   * A builder for constructing a {@link PdfOptions}. The options are organized in groups; each
   * group is configured with a function:
   *
   * <pre>{@code
   * PdfOptions.builder()
   *     .security(security -> security.openPassword("secret"))
   *     .build();
   * }</pre>
   *
   * @see PdfOptions
   */
  @SuppressWarnings("PMD.TooManyMethods")
  public static final class Builder {

    private final Map<PdfOption, Object> values = new EnumMap<>(PdfOption.class);
    private final Map<String, Object> extraFilterData = new LinkedHashMap<>();

    // Private constructor so only PdfOptions can initialize an instance of this builder.
    private Builder() {
      super();
    }

    /**
     * Specifies the version of the PDF specification, or the PDF/A conformance, of the document.
     *
     * <p>FilterData: {@code SelectPdfVersion}. See {@link PdfVersion} for the office versions that
     * support each value.
     *
     * @param version The PDF version.
     * @return This builder instance.
     */
    public @NonNull Builder version(final @NonNull PdfVersion version) {
      AssertUtils.notNull(version, "version must not be null");
      values.put(PdfOption.SELECT_PDF_VERSION, version.getValue());
      return this;
    }

    /**
     * Specifies whether the document complies with PDF/UA (universal accessibility), which also
     * makes it a tagged PDF.
     *
     * <p>FilterData: {@code PDFUACompliance}. Requires LibreOffice 7.0 or later.
     *
     * @param pdfUa {@code true} for a PDF/UA document.
     * @return This builder instance.
     */
    public @NonNull Builder pdfUa(final boolean pdfUa) {
      values.put(PdfOption.PDF_UA_COMPLIANCE, pdfUa);
      return this;
    }

    /**
     * Specifies whether the document is a tagged PDF, which contains the structure of the document
     * (headings, paragraphs, tables...) for screen readers and text extraction.
     *
     * <p>FilterData: {@code UseTaggedPDF}. Supported by all the office versions.
     *
     * @param tagged {@code true} for a tagged PDF.
     * @return This builder instance.
     */
    public @NonNull Builder tagged(final boolean tagged) {
      values.put(PdfOption.USE_TAGGED_PDF, tagged);
      return this;
    }

    /**
     * Specifies whether the source document is embedded in the PDF document (hybrid PDF), so that
     * the office application can open the PDF document and edit it.
     *
     * <p>FilterData: {@code IsAddStream}. Supported by all the office versions.
     *
     * @param embed {@code true} to embed the source document.
     * @return This builder instance.
     */
    public @NonNull Builder embedSourceDocument(final boolean embed) {
      values.put(PdfOption.IS_ADD_STREAM, embed);
      return this;
    }

    /**
     * Specifies whether the PDF images of the source document are exported as reference XObjects,
     * instead of form XObjects.
     *
     * <p>FilterData: {@code UseReferenceXObject}. Requires LibreOffice 5.4 or later.
     *
     * @param use {@code true} to use reference XObjects.
     * @return This builder instance.
     */
    public @NonNull Builder referenceXObjects(final boolean use) {
      values.put(PdfOption.USE_REFERENCE_XOBJECT, use);
      return this;
    }

    /**
     * Configures the options for the images.
     *
     * @param images A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder images(final @NonNull Consumer<@NonNull PdfImageOptions> images) {
      images.accept(new PdfImageOptions(values));
      return this;
    }

    /**
     * Configures the options for the pages and the content to export.
     *
     * @param pages A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder pages(final @NonNull Consumer<@NonNull PdfPageOptions> pages) {
      pages.accept(new PdfPageOptions(values));
      return this;
    }

    /**
     * Configures the options for the comments of the source document.
     *
     * @param comments A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder comments(final @NonNull Consumer<@NonNull PdfCommentOptions> comments) {
      comments.accept(new PdfCommentOptions(values));
      return this;
    }

    /**
     * Configures the options for the bookmarks (outline).
     *
     * @param bookmarks A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder bookmarks(
        final @NonNull Consumer<@NonNull PdfBookmarkOptions> bookmarks) {
      bookmarks.accept(new PdfBookmarkOptions(values));
      return this;
    }

    /**
     * Configures the options for the form fields.
     *
     * @param forms A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder forms(final @NonNull Consumer<@NonNull PdfFormOptions> forms) {
      forms.accept(new PdfFormOptions(values));
      return this;
    }

    /**
     * Configures the options for the hyperlinks.
     *
     * @param links A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder links(final @NonNull Consumer<@NonNull PdfLinkOptions> links) {
      links.accept(new PdfLinkOptions(values));
      return this;
    }

    /**
     * Configures the way the document is displayed when it is opened.
     *
     * @param initialView A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder initialView(
        final @NonNull Consumer<@NonNull PdfInitialViewOptions> initialView) {
      initialView.accept(new PdfInitialViewOptions(values));
      return this;
    }

    /**
     * Configures the window of the PDF viewer when the document is opened.
     *
     * @param viewer A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder viewer(final @NonNull Consumer<@NonNull PdfViewerOptions> viewer) {
      viewer.accept(new PdfViewerOptions(values));
      return this;
    }

    /**
     * Configures the encryption and the permissions of the document.
     *
     * @param security A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder security(
        final @NonNull Consumer<@NonNull PdfSecurityOptions> security) {
      security.accept(new PdfSecurityOptions(values));
      return this;
    }

    /**
     * Configures a text watermark printed on each page.
     *
     * @param watermark A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder watermark(
        final @NonNull Consumer<@NonNull PdfWatermarkOptions> watermark) {
      watermark.accept(new PdfWatermarkOptions(values));
      return this;
    }

    /**
     * Configures the digital signature of the document.
     *
     * @param signature A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder signature(
        final @NonNull Consumer<@NonNull PdfSignatureOptions> signature) {
      signature.accept(new PdfSignatureOptions(values));
      return this;
    }

    /**
     * Configures the options that only apply to presentations (Impress documents).
     *
     * @param presentation A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder presentation(
        final @NonNull Consumer<@NonNull PdfPresentationOptions> presentation) {
      presentation.accept(new PdfPresentationOptions(values));
      return this;
    }

    /**
     * Configures the options that only apply to spreadsheets (Calc documents).
     *
     * @param spreadsheet A function that sets the options of the group.
     * @return This builder instance.
     */
    public @NonNull Builder spreadsheet(
        final @NonNull Consumer<@NonNull PdfSpreadsheetOptions> spreadsheet) {
      spreadsheet.accept(new PdfSpreadsheetOptions(values));
      return this;
    }

    /**
     * Sets any property of the {@code FilterData} of the PDF export filter, for the properties that
     * have no dedicated option. These properties are applied last, so they take precedence over the
     * other options. They are not validated.
     *
     * @param name The name of the property.
     * @param value The value of the property. {@code null} removes the property.
     * @return This builder instance.
     */
    public @NonNull Builder filterData(final @NonNull String name, final @Nullable Object value) {
      AssertUtils.notBlank(name, "name must not be blank");
      if (value == null) {
        extraFilterData.remove(name);
      } else {
        extraFilterData.put(name, value);
      }
      return this;
    }

    /**
     * Creates the options that are specified by this builder.
     *
     * @return The options that are specified by this builder.
     * @throws IllegalArgumentException If the options cannot be used together: a password with a
     *     PDF/A version, a permission without a permission password, PDF/UA without tags, or
     *     signature details without a certificate.
     */
    public @NonNull PdfOptions build() {

      validateEncryption();
      validatePermissions();
      AssertUtils.isTrue(
          !(isTrue(PdfOption.PDF_UA_COMPLIANCE) && isFalse(PdfOption.USE_TAGGED_PDF)),
          "PDF/UA requires a tagged PDF: pdfUa(true) cannot be used with tagged(false)");
      validateSignature();

      return new PdfOptions(values, extraFilterData);
    }

    private boolean isTrue(final PdfOption option) {
      return Boolean.TRUE.equals(values.get(option));
    }

    private boolean isFalse(final PdfOption option) {
      return Boolean.FALSE.equals(values.get(option));
    }

    // PDF/A does not allow encryption, and the office installation would silently drop it.
    private void validateEncryption() {
      final Object versionValue = values.get(PdfOption.SELECT_PDF_VERSION);
      final PdfVersion version =
          versionValue == null ? null : PdfVersion.fromValue((Integer) versionValue);
      if (version != null && version.isPdfA()) {
        AssertUtils.isTrue(
            !isTrue(PdfOption.ENCRYPT_FILE) && !isTrue(PdfOption.RESTRICT_PERMISSIONS),
            "PDF/A does not allow encryption: a password cannot be used with " + version);
      }
    }

    // The permissions are ignored without a permission password.
    private void validatePermissions() {
      if (!isTrue(PdfOption.RESTRICT_PERMISSIONS)) {
        for (final PdfOption option :
            List.of(
                PdfOption.PRINTING,
                PdfOption.CHANGES,
                PdfOption.ENABLE_COPYING_OF_CONTENT,
                PdfOption.ENABLE_TEXT_ACCESS_FOR_ACCESSIBILITY_TOOLS)) {
          AssertUtils.isTrue(
              !values.containsKey(option),
              option.getFilterDataName() + " requires a permission password");
        }
      }
    }

    // The signature details are ignored without a certificate to sign with.
    private void validateSignature() {
      if (!isTrue(PdfOption.SIGN_PDF) && !extraFilterData.containsKey("SignatureCertificate")) {
        for (final PdfOption option :
            List.of(
                PdfOption.SIGN_CERTIFICATE_CA_PEM,
                PdfOption.SIGNATURE_PASSWORD,
                PdfOption.SIGNATURE_LOCATION,
                PdfOption.SIGNATURE_REASON,
                PdfOption.SIGNATURE_CONTACT_INFO,
                PdfOption.SIGNATURE_TSA)) {
          AssertUtils.isTrue(
              !values.containsKey(option),
              option.getFilterDataName() + " requires a certificate to sign with");
        }
      }
    }
  }
}
