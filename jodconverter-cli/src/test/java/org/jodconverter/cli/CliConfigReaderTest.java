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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.local.filter.DefaultFilterChain;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.local.filter.NoopFilter;
import org.jodconverter.local.filter.PagesSelectorFilter;
import org.jodconverter.local.filter.RefreshFilter;
import org.jodconverter.local.filter.text.DocumentIndexesUpdaterFilter;
import org.jodconverter.local.filter.text.DocumentInserterFilter;
import org.jodconverter.local.filter.text.GraphicInserterFilter;
import org.jodconverter.local.filter.text.LinkedImagesEmbedderFilter;
import org.jodconverter.local.filter.text.PageMarginsFilter;
import org.jodconverter.local.filter.text.TextInserterFilter;
import org.jodconverter.local.filter.text.TextReplacerFilter;

/** Contains tests for the {@link CliConfigReader} class. */
class CliConfigReaderTest {

  private static final String IMAGE = "src/integTest/resources/images/sample-1.jpg";

  /** A filter with a public no-argument constructor, for the custom filter entries. */
  public static class CustomFilter extends NoopFilter {}

  /** A filter without a no-argument constructor. */
  public static class FilterWithArgument extends NoopFilter {
    public FilterWithArgument(final String ignored) {
      super();
    }
  }

  /** A filter whose constructor fails. */
  public static class FailingFilter extends NoopFilter {
    public FailingFilter() {
      super();
      throw new IllegalStateException("The constructor failed");
    }
  }

  private static File write(final File dir, final String name, final String content)
      throws IOException {
    final var file = new File(dir, name);
    Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    return file;
  }

  private static List<Filter> filters(final String yaml, final File dir) throws IOException {
    return CliConfigReader.read(write(dir, "config.yml", yaml)).filters();
  }

  @Nested
  class Read {

    @Test
    void withYamlFile_ShouldReadSslAndFilters(final @TempDir File dir) throws IOException {

      final var config =
          CliConfigReader.read(
              write(
                  dir,
                  "config.yaml",
                  """
                  ssl:
                    enabled: true
                    ciphers: [A, B]
                    enabled-protocols: TLSv1.2, TLSv1.3
                    key-alias: alias
                    key-password: keypwd
                    key-store: /key.jks
                    key-store-password: kspwd
                    key-store-type: JKS
                    key-store-provider: SUN
                    trust-store: /trust.p12
                    trust-store-password: tspwd
                    trust-store-type: PKCS12
                    trust-store-provider: BC
                    protocol: TLS
                    trust-all: true
                    verify-hostname: false
                  filters:
                    - type: pages-selector
                      pages: [1, 3]
                  """));

      final var ssl = config.ssl();
      assertThat(ssl).isNotNull();
      assertThat(ssl.isEnabled()).isTrue();
      assertThat(ssl.getCiphers()).containsExactly("A", "B");
      assertThat(ssl.getEnabledProtocols()).containsExactly("TLSv1.2", "TLSv1.3");
      assertThat(ssl.getKeyAlias()).isEqualTo("alias");
      assertThat(ssl.getKeyPassword()).isEqualTo("keypwd");
      assertThat(ssl.getKeyStore()).isEqualTo("/key.jks");
      assertThat(ssl.getKeyStorePassword()).isEqualTo("kspwd");
      assertThat(ssl.getKeyStoreType()).isEqualTo("JKS");
      assertThat(ssl.getKeyStoreProvider()).isEqualTo("SUN");
      assertThat(ssl.getTrustStore()).isEqualTo("/trust.p12");
      assertThat(ssl.getTrustStorePassword()).isEqualTo("tspwd");
      assertThat(ssl.getTrustStoreType()).isEqualTo("PKCS12");
      assertThat(ssl.getTrustStoreProvider()).isEqualTo("BC");
      assertThat(ssl.getProtocol()).isEqualTo("TLS");
      assertThat(ssl.isTrustAll()).isTrue();
      assertThat(ssl.isVerifyHostname()).isFalse();
      assertThat(config.filters()).hasSize(1).first().isInstanceOf(PagesSelectorFilter.class);
      assertThat(config.filters().get(0)).extracting("pages").isEqualTo(List.of(1, 3));
      assertThat(config.filterChain()).isInstanceOf(DefaultFilterChain.class);
    }

    @Test
    void withJsonFile_ShouldReadSslAndFilters(final @TempDir File dir) throws IOException {

      final var config =
          CliConfigReader.read(
              write(
                  dir,
                  "config.json",
                  """
                  {
                    "ssl": { "enabled": "true", "trust-store": "/trust.p12", "verify-hostname": false },
                    "filters": [
                      { "type": "pages-selector", "pages": 2 },
                      { "type": "table-of-content-updater", "level": 3 }
                    ]
                  }
                  """));

      assertThat(config.ssl()).isNotNull();
      assertThat(config.ssl().isEnabled()).isTrue();
      assertThat(config.ssl().getTrustStore()).isEqualTo("/trust.p12");
      assertThat(config.ssl().isVerifyHostname()).isFalse();
      assertThat(config.filters()).hasSize(2);
      assertThat(config.filters().get(0)).extracting("pages").isEqualTo(List.of(2));
      assertThat(config.filters().get(1))
          .isInstanceOf(DocumentIndexesUpdaterFilter.class)
          .extracting("level")
          .isEqualTo(3);
    }

    @Test
    void withEmptyFile_ShouldReadEmptyConfig(final @TempDir File dir) throws IOException {

      final var config = CliConfigReader.read(write(dir, "config.yml", ""));

      assertThat(config.ssl()).isNull();
      assertThat(config.filters()).isEmpty();
      assertThat(config.filterChain()).isNull();
    }

    @Test
    void withMissingFile_ShouldThrowIllegalArgumentException(final @TempDir File dir) {

      final var file = new File(dir, "missing.yml");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Could not read the configuration file");
    }

    @Test
    void withInvalidYaml_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "ssl: [\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Invalid configuration file");
    }

    @Test
    void withInvalidJson_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.json", "{ \"ssl\": ");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Invalid configuration file");
    }

    @Test
    void withUnknownRootKey_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "ssl: {}\nfilter: []\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Unknown key 'filter' in the configuration");
    }

    @Test
    void withRootThatIsNotAMap_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "- ssl\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("the configuration must be a map");
    }
  }

  @Nested
  class Ssl {

    @Test
    void withUnknownKey_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "ssl:\n  truststore: x\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Unknown key 'truststore' in 'ssl' of the configuration")
          .withMessageContaining("trust-store");
    }

    @Test
    void withInvalidBoolean_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "ssl:\n  enabled: yes please\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'enabled' expects true or false");
    }

    @Test
    void withNonTextValue_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "ssl:\n  key-store: [a, b]\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'key-store' expects a text");
    }
  }

  @Nested
  class Filters {

    @Test
    void withFiltersWithoutKeys_ShouldCreateThem(final @TempDir File dir) throws IOException {

      final var filters =
          filters(
              """
              filters:
                - type: noop
                - type: refresh
                - type: linked-images-embedder
                - type: LINKED_IMAGES_EMBEDDER
              """,
              dir);

      assertThat(filters)
          .hasSize(4)
          .satisfies(list -> assertThat(list.get(0)).isInstanceOf(NoopFilter.class))
          .satisfies(list -> assertThat(list.get(1)).isInstanceOf(RefreshFilter.class))
          .satisfies(list -> assertThat(list.get(2)).isInstanceOf(LinkedImagesEmbedderFilter.class))
          .satisfies(
              list -> assertThat(list.get(3)).isInstanceOf(LinkedImagesEmbedderFilter.class));
    }

    @Test
    void withDocumentInserter_ShouldCreateIt(final @TempDir File dir) throws IOException {

      final var filters =
          filters("filters:\n  - type: document-inserter\n    document: /docs/footer.odt\n", dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(DocumentInserterFilter.class);
      assertThat(filters.get(0))
          .extracting("documentToInsert")
          .isEqualTo(new File("/docs/footer.odt"));
    }

    @Test
    void withGraphicInserterAndPositions_ShouldCreateIt(final @TempDir File dir)
        throws IOException {

      final var filters =
          filters(
              """
              filters:
                - type: graphic-inserter
                  image: %s
                  horizontal-position: 50
                  vertical-position: 111
              """
                  .formatted(IMAGE),
              dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(GraphicInserterFilter.class);
      assertThat(filters.get(0)).extracting("imageFile").isEqualTo(new File(IMAGE));
      assertThat(filters.get(0))
          .extracting("shapeProperties")
          .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
          .containsEntry("HoriOrientPosition", 5000)
          .containsEntry("VertOrientPosition", 11100);
    }

    @Test
    void withGraphicInserterAndSize_ShouldCreateIt(final @TempDir File dir) throws IOException {

      final var filters =
          filters(
              """
              filters:
                - type: graphic-inserter
                  image: %s
                  width: 20
                  height: 10
                  horizontal-position: 50
                  vertical-position: 111
              """
                  .formatted(IMAGE),
              dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(GraphicInserterFilter.class);
      assertThat(filters.get(0)).extracting("rectSize").isEqualTo(new java.awt.Dimension(20, 10));
    }

    @Test
    void withGraphicInserterAndShapeProperties_ShouldCreateIt(final @TempDir File dir)
        throws IOException {

      final var filters =
          filters(
              """
              filters:
                - type: graphic-inserter
                  image: %s
                  shape-properties:
                    AnchorType: AT_PARAGRAPH
                    HoriOrientPosition: 1000
              """
                  .formatted(IMAGE),
              dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(GraphicInserterFilter.class);
      assertThat(filters.get(0))
          .extracting("shapeProperties")
          .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
          .containsEntry("AnchorType", "AT_PARAGRAPH")
          .containsEntry("HoriOrientPosition", 1000);
    }

    @Test
    void withGraphicInserterAndMissingImage_ShouldThrowIllegalArgumentException(
        final @TempDir File dir) throws IOException {

      final var file =
          write(
              dir,
              "config.yml",
              """
              filters:
                - type: graphic-inserter
                  image: /missing.jpg
                  horizontal-position: 50
                  vertical-position: 111
              """);
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Invalid filter 'graphic-inserter' in filter 1");
    }

    @Test
    void withGraphicInserterAndWidthOnly_ShouldThrowIllegalArgumentException(
        final @TempDir File dir) throws IOException {

      final var file =
          write(
              dir,
              "config.yml",
              """
              filters:
                - type: graphic-inserter
                  image: %s
                  width: 20
                  horizontal-position: 50
                  vertical-position: 111
              """
                  .formatted(IMAGE));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'width' and 'height' must be given together");
    }

    @Test
    void withTextInserter_ShouldCreateIt(final @TempDir File dir) throws IOException {

      final var filters =
          filters(
              """
              filters:
                - type: text-inserter
                  text: Hello
                  width: 100
                  height: 10
                  horizontal-position: 50
                  vertical-position: 100
                - type: text-inserter
                  text: World
                  width: 100
                  height: 10
                  shape-properties:
                    AnchorType: AT_PAGE
              """,
              dir);

      assertThat(filters)
          .hasSize(2)
          .allSatisfy(f -> assertThat(f).isInstanceOf(TextInserterFilter.class));
      assertThat(filters.get(0)).extracting("insertedText").isEqualTo("Hello");
      assertThat(filters.get(0))
          .extracting("shapeProperties")
          .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
          .containsEntry("HoriOrientPosition", 5000)
          .containsEntry("VertOrientPosition", 10000);
      assertThat(filters.get(1))
          .extracting("shapeProperties")
          .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
          .containsEntry("AnchorType", "AT_PAGE");
    }

    @Test
    void withTextReplacer_ShouldKeepTheOrder(final @TempDir File dir) throws IOException {

      final var filters =
          filters(
              """
              filters:
                - type: text-replacer
                  replacements:
                    text: Text
                    to insert: describing the image below
                    "2024": 2025
              """,
              dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(TextReplacerFilter.class);
      assertThat(filters.get(0))
          .extracting("searchList")
          .isEqualTo(new String[] {"text", "to insert", "2024"});
      assertThat(filters.get(0))
          .extracting("replacementList")
          .isEqualTo(new String[] {"Text", "describing the image below", "2025"});
    }

    @Test
    void withTextReplacerWithoutReplacements_ShouldThrowIllegalArgumentException(
        final @TempDir File dir) throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  - type: text-replacer\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'replacements' is required in filter 1");

      final var empty =
          write(dir, "empty.yml", "filters:\n  - type: text-replacer\n    replacements: {}\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(empty))
          .withMessageContaining("'replacements' must not be empty in filter 1");
    }

    @Test
    void withPageMargins_ShouldCreateIt(final @TempDir File dir) throws IOException {

      final var filters =
          filters("filters:\n  - type: page-margins\n    left: 10\n    bottom: 20\n", dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(PageMarginsFilter.class);
      assertThat(filters.get(0))
          .hasFieldOrPropertyWithValue("leftMargin", 10)
          .hasFieldOrPropertyWithValue("topMargin", null)
          .hasFieldOrPropertyWithValue("rightMargin", null)
          .hasFieldOrPropertyWithValue("bottomMargin", 20);
    }

    @Test
    void withDocumentIndexesUpdaterWithoutLevel_ShouldUseZero(final @TempDir File dir)
        throws IOException {

      final var filters = filters("filters:\n  - type: document-indexes-updater\n", dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(DocumentIndexesUpdaterFilter.class);
      assertThat(filters.get(0)).extracting("level").isEqualTo(0);
    }

    @Test
    void withTableOfContentUpdater_ShouldGiveTheDocumentIndexesUpdater(final @TempDir File dir)
        throws IOException {

      // The former name of the type is still accepted.
      final var filters = filters("filters:\n  - type: table-of-content-updater\n", dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(DocumentIndexesUpdaterFilter.class);
    }

    @Test
    void withPagesSelectorAndInvalidPage_ShouldThrowIllegalArgumentException(
        final @TempDir File dir) throws IOException {

      final var file =
          write(dir, "config.yml", "filters:\n  - type: pages-selector\n    pages: [1, two]\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'pages' expects an integer");
    }

    @Test
    void withMissingRequiredKey_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  - type: pages-selector\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'pages' is required in filter 1");
    }

    @Test
    void withUnknownKey_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file =
          write(
              dir,
              "config.yml",
              "filters:\n  - type: pages-selector\n    pages: 1\n    page: 2\n    foo: 3\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Unknown keys 'page', 'foo' in filter 1");
    }

    @Test
    void withUnknownType_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  - type: watermark\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("Unknown filter type 'watermark' in filter 1")
          .withMessageContaining(String.join(", ", FilterParser.getTypeNames()));
    }

    @Test
    void withEntryThatIsNotAMap_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  - noop\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("filter 1 must be a map");
    }

    @Test
    void withFiltersThatIsNotAList_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  type: noop\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'filters' of the configuration must be a list");
    }

    @Test
    void withEntryWithoutTypeNorClass_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  - pages: 1\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("'class' is required in filter 1");
    }

    @Test
    void withCustomClass_ShouldInstantiateIt(final @TempDir File dir) throws IOException {

      final var filters =
          filters("filters:\n  - class: " + CustomFilter.class.getName() + "\n", dir);

      assertThat(filters).hasSize(1).first().isInstanceOf(CustomFilter.class);
    }

    @Test
    void withCustomClassThatIsNotAFilter_ShouldThrowIllegalArgumentException(
        final @TempDir File dir) throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  - class: java.lang.Object\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("The class 'java.lang.Object' of filter 1 is not a filter");
    }

    @Test
    void withUnknownCustomClass_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file = write(dir, "config.yml", "filters:\n  - class: com.example.Missing\n");
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("The class 'com.example.Missing' of filter 1 was not found");
    }

    @Test
    void withCustomClassWithoutNoArgConstructor_ShouldThrowIllegalArgumentException(
        final @TempDir File dir) throws IOException {

      final var file =
          write(dir, "config.yml", "filters:\n  - class: " + FilterWithArgument.class.getName());
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("has no public no-argument constructor");
    }

    @Test
    void withCustomClassThatFails_ShouldThrowIllegalArgumentException(final @TempDir File dir)
        throws IOException {

      final var file =
          write(dir, "config.yml", "filters:\n  - class: " + FailingFilter.class.getName());
      assertThatIllegalArgumentException()
          .isThrownBy(() -> CliConfigReader.read(file))
          .withMessageContaining("could not be instantiated: The constructor failed");
    }
  }

  @Nested
  class Parse {

    @Test
    void withNull_ShouldReturnEmptyConfig() {
      assertThat(CliConfigReader.parse(null)).isSameAs(CliConfig.EMPTY);
    }

    @Test
    void withLongAndDoubleNumbers_ShouldAcceptIntegralValuesOnly() {

      assertThat(
              CliConfigReader.parse(
                      Map.of("filters", List.of(Map.of("type", "pages-selector", "pages", 2L))))
                  .filters()
                  .get(0))
          .extracting("pages")
          .isEqualTo(List.of(2));

      assertThatIllegalArgumentException()
          .isThrownBy(
              () ->
                  CliConfigReader.parse(
                      Map.of("filters", List.of(Map.of("type", "pages-selector", "pages", 2.5)))))
          .withMessageContaining("'pages' expects an integer");
    }
  }
}
