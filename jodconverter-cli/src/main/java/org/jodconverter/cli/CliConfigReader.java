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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.ToNumberPolicy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

import org.jodconverter.core.util.FileUtils;
import org.jodconverter.local.filter.Filter;
import org.jodconverter.remote.ssl.SslConfig;

/**
 * Reads the configuration file given to the command line with {@code --config}. The file is YAML
 * when its extension is {@code .yml} or {@code .yaml}, and JSON otherwise. It holds two optional
 * sections, {@code ssl} and {@code filters}:
 *
 * <pre>{@code
 * ssl:
 *   enabled: true
 *   trust-store: /path/to/truststore.p12
 *   trust-store-password: secret
 * filters:
 *   - type: pages-selector
 *     pages: [1, 3]
 *   - type: text-replacer
 *     replacements:
 *       Draft: Final
 *   - class: com.example.MyFilter
 * }</pre>
 *
 * <p>The keys of the {@code ssl} section are the properties of {@link SslConfig}, in kebab case.
 * Each filter is a built-in filter, named by {@code type}, with its own keys, or a custom {@link
 * Filter} class with a public no-argument constructor, named by {@code class}; see {@link
 * FilterParser}.
 */
final class CliConfigReader {

  private static final String SSL = "ssl";
  private static final String FILTERS = "filters";

  private static final Map<String, BiConsumer<SslConfig, Object>> SSL_OPTIONS = initSslOptions();

  // Suppresses default constructor, ensuring non-instantiability.
  private CliConfigReader() {
    throw new AssertionError("Utility class must not be instantiated");
  }

  /**
   * Reads the given configuration file.
   *
   * @param file The file to read, JSON or YAML.
   * @return The configuration.
   * @throws IllegalArgumentException If the file cannot be read, or if its content is not valid.
   */
  static CliConfig read(final File file) {

    final Object root;
    try (var reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      root = isYaml(file) ? loadYaml(reader) : loadJson(reader);
    } catch (IOException ex) {
      throw new IllegalArgumentException(
          "Could not read the configuration file '" + file + "': " + ex.getMessage(), ex);
    } catch (YamlEngineException | JsonParseException ex) {
      throw new IllegalArgumentException(
          "Invalid configuration file '" + file + "': " + ex.getMessage(), ex);
    }

    try {
      return parse(root);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "Invalid configuration file '" + file + "': " + ex.getMessage(), ex);
    }
  }

  /**
   * Parses the content of a configuration file, already loaded as maps, lists and scalars.
   *
   * @param root The root of the file; an empty file gives an empty configuration.
   * @return The configuration.
   * @throws IllegalArgumentException If the content is not valid.
   */
  /* default */
  static CliConfig parse(final @Nullable Object root) {

    if (root == null) {
      return CliConfig.EMPTY;
    }
    final var section = new Section("the configuration", root);
    final var ssl = section.section(SSL).map(CliConfigReader::parseSsl).orElse(null);
    final var filters = section.list(FILTERS).map(FilterParser::parse).orElse(List.<Filter>of());
    section.ensureNoUnknownKeys();

    return new CliConfig(ssl, filters);
  }

  private static boolean isYaml(final File file) {

    final var extension = FileUtils.getExtension(file.getName());
    return extension != null
        && ("yml".equalsIgnoreCase(extension) || "yaml".equalsIgnoreCase(extension));
  }

  private static @Nullable Object loadYaml(final Reader reader) {
    return new Load(LoadSettings.builder().build()).loadFromReader(reader);
  }

  private static @Nullable Object loadJson(final Reader reader) {
    return new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()
        .fromJson(reader, Object.class);
  }

  private static SslConfig parseSsl(final Section section) {

    final var config = new SslConfig();
    for (final var key : section.keys()) {
      final var setter = SSL_OPTIONS.get(key);
      if (setter == null) {
        throw new IllegalArgumentException(
            "Unknown key '"
                + key
                + "' in "
                + section.name()
                + "; expected one of: "
                + String.join(", ", SSL_OPTIONS.keySet()));
      }
      setter.accept(config, section.value(key));
    }
    return config;
  }

  private static Map<String, BiConsumer<SslConfig, Object>> initSslOptions() {

    final var map = new LinkedHashMap<String, BiConsumer<SslConfig, Object>>();
    map.put("enabled", (c, v) -> c.setEnabled(Values.bool(v, "enabled")));
    map.put("ciphers", (c, v) -> c.setCiphers(Values.strings(v, "ciphers")));
    map.put(
        "enabled-protocols",
        (c, v) -> c.setEnabledProtocols(Values.strings(v, "enabled-protocols")));
    map.put("key-alias", (c, v) -> c.setKeyAlias(Values.string(v, "key-alias")));
    map.put("key-password", (c, v) -> c.setKeyPassword(Values.string(v, "key-password")));
    map.put("key-store", (c, v) -> c.setKeyStore(Values.string(v, "key-store")));
    map.put(
        "key-store-password",
        (c, v) -> c.setKeyStorePassword(Values.string(v, "key-store-password")));
    map.put("key-store-type", (c, v) -> c.setKeyStoreType(Values.string(v, "key-store-type")));
    map.put(
        "key-store-provider",
        (c, v) -> c.setKeyStoreProvider(Values.string(v, "key-store-provider")));
    map.put("trust-store", (c, v) -> c.setTrustStore(Values.string(v, "trust-store")));
    map.put(
        "trust-store-password",
        (c, v) -> c.setTrustStorePassword(Values.string(v, "trust-store-password")));
    map.put(
        "trust-store-type", (c, v) -> c.setTrustStoreType(Values.string(v, "trust-store-type")));
    map.put(
        "trust-store-provider",
        (c, v) -> c.setTrustStoreProvider(Values.string(v, "trust-store-provider")));
    map.put("protocol", (c, v) -> c.setProtocol(Values.string(v, "protocol")));
    map.put("trust-all", (c, v) -> c.setTrustAll(Values.bool(v, "trust-all")));
    map.put("verify-hostname", (c, v) -> c.setVerifyHostname(Values.bool(v, "verify-hostname")));
    return map;
  }

  /** Converts the scalars of a configuration file, which are typed in YAML and strings in JSON. */
  static final class Values {

    private Values() {
      throw new AssertionError("Utility class must not be instantiated");
    }

    static boolean bool(final Object value, final String name) {
      if (value instanceof Boolean bool) {
        return bool;
      }
      if (value instanceof String string) {
        if ("true".equalsIgnoreCase(string)) {
          return true;
        }
        if ("false".equalsIgnoreCase(string)) {
          return false;
        }
      }
      throw new IllegalArgumentException("'" + name + "' expects true or false");
    }

    static int integer(final Object value, final String name) {
      if (value instanceof Number number
          && number.longValue() == number.doubleValue()
          && number.longValue() == (int) number.longValue()) {
        return number.intValue();
      }
      if (value instanceof String string) {
        try {
          return Integer.parseInt(string.trim());
        } catch (NumberFormatException ignored) {
          // Reported below.
        }
      }
      throw new IllegalArgumentException("'" + name + "' expects an integer");
    }

    static String string(final Object value, final String name) {
      if (value instanceof String || value instanceof Number || value instanceof Boolean) {
        return value.toString();
      }
      throw new IllegalArgumentException("'" + name + "' expects a text");
    }

    // A list of texts, or a comma-separated text.
    static String[] strings(final Object value, final String name) {
      if (value instanceof List<?> list) {
        return list.stream().map(item -> string(item, name)).toArray(String[]::new);
      }
      return Arrays.stream(string(value, name).split(",")).map(String::trim).toArray(String[]::new);
    }

    // A list of integers, or a single integer.
    static int[] integers(final Object value, final String name) {
      if (value instanceof List<?> list) {
        return list.stream().mapToInt(item -> integer(item, name)).toArray();
      }
      return new int[] {integer(value, name)};
    }
  }

  /** A map of a configuration file, which reports the keys it does not expect. */
  static final class Section {

    private final String name;
    private final Map<String, Object> values;
    private final Set<String> used = new HashSet<>();

    Section(final String name, final Object value) {
      if (!(value instanceof Map<?, ?> map)) {
        throw new IllegalArgumentException(name + " must be a map of keys and values");
      }
      this.name = name;
      this.values = new LinkedHashMap<>();
      map.forEach((key, val) -> values.put(String.valueOf(key), val));
    }

    String name() {
      return name;
    }

    Set<String> keys() {
      used.addAll(values.keySet());
      return values.keySet();
    }

    boolean has(final String key) {
      return values.get(key) != null;
    }

    @Nullable Object value(final String key) {
      used.add(key);
      return values.get(key);
    }

    Optional<Object> optional(final String key) {
      return Optional.ofNullable(value(key));
    }

    Object required(final String key) {
      return optional(key)
          .orElseThrow(() -> new IllegalArgumentException("'" + key + "' is required in " + name));
    }

    Optional<Section> section(final String key) {
      return optional(key).map(value -> new Section("'" + key + "' of " + name, value));
    }

    Optional<List<?>> list(final String key) {
      return optional(key)
          .map(
              value -> {
                if (value instanceof List<?> list) {
                  return list;
                }
                throw new IllegalArgumentException("'" + key + "' of " + name + " must be a list");
              });
    }

    // A map of texts, in the order of the file.
    Optional<Map<String, String>> strings(final String key) {
      return section(key)
          .map(
              section -> {
                final var map = new LinkedHashMap<String, String>();
                for (final var entry : section.keys()) {
                  map.put(entry, Values.string(section.value(entry), entry));
                }
                return map;
              });
    }

    Optional<Map<String, Object>> map(final String key) {
      return section(key)
          .map(
              section -> {
                final var map = new LinkedHashMap<String, Object>();
                for (final var entry : section.keys()) {
                  map.put(entry, section.value(entry));
                }
                return map;
              });
    }

    void ensureNoUnknownKeys() {
      final var unknown = new ArrayList<>(values.keySet());
      unknown.removeAll(used);
      if (!unknown.isEmpty()) {
        throw new IllegalArgumentException(
            "Unknown key"
                + (unknown.size() > 1 ? "s " : " ")
                + unknown.stream().map(key -> "'" + key + "'").reduce((a, b) -> a + ", " + b).get()
                + " in "
                + name);
      }
    }
  }
}
