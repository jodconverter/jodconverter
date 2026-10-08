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

package org.jodconverter.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import org.jodconverter.core.pdf.PdfOptions;

/** Contains tests for the {@link JodConverterPdfProperties} class. */
class JodConverterPdfPropertiesTest {

  private static JodConverterPdfProperties bind(final Map<String, String> values) {
    // The environment brings the conversion of the text values (enums, resources...).
    final var environment = new MockEnvironment();
    values.forEach(environment::setProperty);
    return Binder.get(environment)
        .bindOrCreate("jodconverter.pdf", Bindable.of(JodConverterPdfProperties.class));
  }

  // A value for every property, as an application would write them.
  private static Map<String, String> everyProperty(final File pemFile) {
    final var p = new HashMap<String, String>();
    final var prefix = "jodconverter.pdf.";
    p.put(prefix + "version", "pdf-1-7");
    p.put(prefix + "pdf-ua", "false");
    p.put(prefix + "tagged", "true");
    p.put(prefix + "embed-source-document", "true");
    p.put(prefix + "reference-xobjects", "true");
    p.put(prefix + "images.lossless", "true");
    p.put(prefix + "images.jpeg-quality", "80");
    p.put(prefix + "images.reduce-resolution", "true");
    p.put(prefix + "images.max-resolution", "150");
    p.put(prefix + "pages.range", "1-3");
    p.put(prefix + "pages.skip-empty-pages", "true");
    p.put(prefix + "pages.placeholders", "true");
    p.put(prefix + "pages.tracked-changes", "true");
    p.put(prefix + "comments.as-pdf-annotations", "true");
    p.put(prefix + "comments.in-margin", "true");
    p.put(prefix + "bookmarks.export", "true");
    p.put(prefix + "bookmarks.open-levels", "2");
    p.put(prefix + "bookmarks.as-named-destinations", "true");
    p.put(prefix + "forms.export", "true");
    p.put(prefix + "forms.submit-format", "xml");
    p.put(prefix + "forms.allow-duplicate-names", "true");
    p.put(prefix + "links.relative-file-links", "true");
    p.put(prefix + "links.convert-odf-targets-to-pdf", "true");
    p.put(prefix + "links.cross-document-links", "browser");
    p.put(prefix + "initial-view.pane", "thumbnails");
    p.put(prefix + "initial-view.page", "2");
    p.put(prefix + "initial-view.magnification", "fit-page");
    p.put(prefix + "initial-view.zoom", "75");
    p.put(prefix + "initial-view.layout", "continuous-facing");
    p.put(prefix + "viewer.resize-to-initial-page", "true");
    p.put(prefix + "viewer.center-window", "true");
    p.put(prefix + "viewer.full-screen", "true");
    p.put(prefix + "viewer.display-document-title", "true");
    p.put(prefix + "viewer.hide-menubar", "true");
    p.put(prefix + "viewer.hide-toolbar", "true");
    p.put(prefix + "viewer.hide-window-controls", "true");
    p.put(prefix + "security.open-password", "open");
    p.put(prefix + "security.permission-password", "owner");
    p.put(prefix + "security.printing", "low-resolution");
    p.put(prefix + "security.changes", "forms-and-comments");
    p.put(prefix + "security.copying", "false");
    p.put(prefix + "security.accessibility-access", "false");
    p.put(prefix + "watermark.text", "DRAFT");
    p.put(prefix + "watermark.tiled-text", "TILED");
    p.put(prefix + "watermark.color", "#FF0000");
    p.put(prefix + "watermark.font-name", "Liberation Sans");
    p.put(prefix + "watermark.font-height", "12");
    p.put(prefix + "watermark.rotation", "45");
    p.put(prefix + "signature.certificate-subject-name", "CN=Me");
    p.put(prefix + "signature.certificate", "file:" + pemFile.getPath());
    p.put(prefix + "signature.private-key", "file:" + pemFile.getPath());
    p.put(prefix + "signature.certificate-authorities", "file:" + pemFile.getPath());
    p.put(prefix + "signature.password", "secret");
    p.put(prefix + "signature.location", "Montreal");
    p.put(prefix + "signature.reason", "Approval");
    p.put(prefix + "signature.contact-info", "me@example.com");
    p.put(prefix + "signature.timestamp-authority", "http://tsa.example.com");
    p.put(prefix + "presentation.hidden-slides", "true");
    p.put(prefix + "presentation.notes-pages", "true");
    p.put(prefix + "presentation.only-notes-pages", "true");
    p.put(prefix + "presentation.transitions", "true");
    p.put(prefix + "spreadsheet.single-page-sheets", "true");
    p.put(prefix + "spreadsheet.sheet-range", "1-2");
    p.put(prefix + "filter-data.Custom", "text");
    p.put(prefix + "filter-data.Count", "3");
    return p;
  }

  @Test
  void withEveryProperty_ShouldSetEveryPdfOption(final @TempDir File testFolder)
      throws IOException {

    final var pem = new File(testFolder, "test.pem");
    Files.writeString(pem.toPath(), "PEM", StandardCharsets.UTF_8);

    final var properties = bind(everyProperty(pem));
    final var options = properties.toPdfOptions();

    // The 66 properties of PdfOptions, every one set by a property, plus the two FilterData
    // entries.
    assertThat(options.getFilterData()).hasSize(68);
    assertThat(options.getFilterData())
        .containsEntry("SelectPdfVersion", 17)
        .containsEntry("Quality", 80)
        .containsEntry("WatermarkColor", 0xFF0000)
        .containsEntry("WatermarkRotateAngle", 450)
        .containsEntry("SignCertificateCertPem", "PEM")
        .containsEntry("Custom", "text")
        .containsEntry("Count", 3);
  }

  @Test
  void withEveryProperty_ShouldReadThemBack(final @TempDir File testFolder) throws Exception {

    final var pem = new File(testFolder, "test.pem");
    Files.writeString(pem.toPath(), "PEM", StandardCharsets.UTF_8);

    final var properties = bind(everyProperty(pem));

    // The groups are JavaBeans for Spring: once bound, every getter returns a value.
    for (final var group :
        new Object[] {
          properties.getImages(),
          properties.getPages(),
          properties.getComments(),
          properties.getBookmarks(),
          properties.getForms(),
          properties.getLinks(),
          properties.getInitialView(),
          properties.getViewer(),
          properties.getSecurity(),
          properties.getWatermark(),
          properties.getSignature(),
          properties.getPresentation(),
          properties.getSpreadsheet()
        }) {
      for (final Method method : group.getClass().getDeclaredMethods()) {
        if (Modifier.isPublic(method.getModifiers())
            && method.getParameterCount() == 0
            && method.getName().startsWith("get")) {
          assertThat(method.invoke(group))
              .as("%s.%s", group.getClass().getSimpleName(), method.getName())
              .isNotNull();
        }
      }
    }
    assertThat(properties.getVersion()).isNotNull();
    assertThat(properties.getPdfUa()).isFalse();
    assertThat(properties.getTagged()).isTrue();
    assertThat(properties.getEmbedSourceDocument()).isTrue();
    assertThat(properties.getReferenceXObjects()).isTrue();
    assertThat(properties.getFilterData()).containsEntry("Count", "3");
  }

  @Test
  void withPreset_ShouldStartFromIt() {

    final var archive = bind(Map.of("jodconverter.pdf.preset", "archive"));
    final var accessible = bind(Map.of("jodconverter.pdf.preset", "accessible"));
    final var compact =
        bind(Map.of("jodconverter.pdf.preset", "compact", "jodconverter.pdf.tagged", "false"));

    assertThat(archive.toPdfOptions().getFilterData()).containsEntry("SelectPdfVersion", 2);
    assertThat(accessible.toPdfOptions().getFilterData()).containsEntry("PDFUACompliance", true);
    assertThat(compact.toPdfOptions().getFilterData())
        .containsEntry("Quality", PdfOptions.compact().getFilterData().get("Quality"))
        .containsEntry("UseTaggedPDF", false);
  }

  @Test
  void withoutProperty_ShouldGiveEmptyOptions() {

    assertThat(bind(Map.of()).toPdfOptions().getFilterData()).isEmpty();
  }

  @Test
  void withOptionsThatCannotBeUsedTogether_ShouldThrowIllegalArgumentException() {

    final var properties =
        bind(
            Map.of(
                "jodconverter.pdf.version", "pdf-a-2b",
                "jodconverter.pdf.security.open-password", "secret"));

    assertThatIllegalArgumentException()
        .isThrownBy(properties::toPdfOptions)
        .withMessageContaining("PDF/A does not allow encryption");
  }
}
