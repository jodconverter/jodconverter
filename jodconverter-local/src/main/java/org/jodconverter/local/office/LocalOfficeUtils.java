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

package org.jodconverter.local.office;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.util.OSUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.office.utils.Info;
import org.jodconverter.local.office.utils.Lo;
import org.jodconverter.local.office.utils.Props;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.PureJavaProcessManager;
import org.jodconverter.local.process.UnixProcessManager;
import org.jodconverter.local.process.WindowsProcessManager;

/** Provides helper functions for local office. */
public final class LocalOfficeUtils {

  public static final int DEFAULT_PORT = 2002;

  private static final String EXECUTABLE_DEFAULT = "program/soffice.bin";
  private static final String EXECUTABLE_MAC = "program/soffice";
  private static final String EXECUTABLE_MAC_41 = "MacOS/soffice";
  private static final String EXECUTABLE_WINDOWS = "program/soffice.exe";
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfficeUtils.class);

  /**
   * Creates the default office home only on demand. This is the Initialization-on-demand holder
   * idiom: <a
   * href="https://www.wikiwand.com/en/Initialization-on-demand_holder_idiom">Initialization-on-demand
   * holder idiom</a>
   */
  private static final class DefaultOfficeHomeHolder {

    /* default */ static final File INSTANCE;

    static {
      if (StringUtils.isNotBlank(System.getProperty("office.home"))) {
        INSTANCE = new File(System.getProperty("office.home"));

      } else if (OSUtils.IS_OS_WINDOWS) {

        // The most recent LibreOffice first, 64-bit before 32-bit (%ProgramFiles(x86)% on
        // 64-bit machines; %ProgramFiles% on 32-bit ones), then OpenOffice.
        final var programFiles64 = System.getenv("ProgramFiles");
        final var programFiles32 = System.getenv("ProgramFiles(x86)");

        final var homes = new ArrayList<String>();
        homes.addAll(listOfficeHomes("LibreOffice", programFiles64, programFiles32));
        homes.add(programFiles32 + File.separator + "OpenOffice 4");
        homes.add(programFiles32 + File.separator + "OpenOffice.org 3");
        INSTANCE = findOfficeHome(EXECUTABLE_WINDOWS, homes);

      } else if (OSUtils.IS_OS_MAC) {

        final var homes =
            List.of(
                "/Applications/LibreOffice.app/Contents",
                "/Applications/OpenOffice.app/Contents",
                "/Applications/OpenOffice.org.app/Contents");
        var homeDir = findOfficeHome(EXECUTABLE_MAC_41, homes);
        if (homeDir == null) {
          homeDir = findOfficeHome(EXECUTABLE_MAC, homes);
        }
        INSTANCE = homeDir;

      } else {

        // UNIX

        // Linux or other *nix variants: the LibreOffice of the distribution or of the
        // packages of The Document Foundation (libreoffice24.2...), the most recent first
        // (https://github.com/jodconverter/jodconverter/issues/386), then OpenOffice.
        final var homes = new ArrayList<String>();
        homes.addAll(
            listOfficeHomes(
                "libreoffice",
                "/usr/lib64",
                "/usr/lib",
                "/usr/local/lib64",
                "/usr/local/lib",
                "/opt"));
        homes.addAll(
            List.of(
                "/usr/lib64/openoffice",
                "/usr/lib64/openoffice.org3",
                "/usr/lib64/openoffice.org",
                "/usr/lib/openoffice",
                "/usr/lib/openoffice.org3",
                "/usr/lib/openoffice.org",
                "/opt/openoffice4",
                "/opt/openoffice.org3"));
        INSTANCE = findOfficeHome(EXECUTABLE_DEFAULT, homes);
      }

      LOGGER.debug("Default office home set to {}", INSTANCE);
    }

    private static File findOfficeHome(final String executablePath, final List<String> homePaths) {

      return homePaths.stream()
          .filter(homePath -> Files.isRegularFile(Path.of(homePath, executablePath)))
          .findFirst()
          .map(File::new)
          .orElse(null);
    }
  }

  /**
   * Lists the directories of the given parents whose name starts with the given prefix, ignoring
   * the case: {@code libreoffice}, {@code libreoffice24.2}, {@code LibreOffice 7}... The
   * directories named with the prefix only come first (the installation of the distribution, or a
   * link to the latest), then the versioned ones, the most recent first; the parents keep their
   * order. A parent that is null, missing or not readable is skipped.
   *
   * @param prefix The start of the directory names.
   * @param parents The directories to look into.
   * @return The paths of the directories found.
   */
  /* default */
  static List<String> listOfficeHomes(final String prefix, final String... parents) {

    final Comparator<String> byVersion =
        (name1, name2) -> {
          final var version1 = name1.substring(prefix.length()).trim();
          final var version2 = name2.substring(prefix.length()).trim();
          if (version1.isEmpty() || version2.isEmpty()) {
            return Boolean.compare(version2.isEmpty(), version1.isEmpty());
          }
          return Info.compareVersions(version2, version1, 2);
        };

    final var homes = new ArrayList<String>();
    for (final var parent : parents) {
      if (parent == null) {
        continue;
      }
      try (var children = Files.list(Path.of(parent))) {
        children
            .filter(Files::isDirectory)
            .map(child -> child.getFileName().toString())
            .filter(name -> name.regionMatches(true, 0, prefix, 0, prefix.length()))
            .sorted(byVersion)
            .map(name -> parent + File.separator + name)
            .forEach(homes::add);
      } catch (IOException | RuntimeException ex) {
        LOGGER.trace("Could not list the directory '{}'", parent, ex);
      }
    }
    return homes;
  }

  /**
   * Find the best process manager that will be used to retrieve a process PID and to kill a process
   * by PID.
   *
   * @return The best process manager according to the current OS.
   */
  public static @NonNull ProcessManager findBestProcessManager() {

    if (OSUtils.IS_OS_UNIX) {
      // Linux, macOS, FreeBSD...: the JVM reads the command lines of the processes.
      return UnixProcessManager.getDefault();
    } else if (OSUtils.IS_OS_WINDOWS) {
      final var windowsProcessManager = WindowsProcessManager.getDefault();
      if (windowsProcessManager.isUsable()) {
        return windowsProcessManager;
      }
      LOGGER.warn(
          "The running processes cannot be listed with PowerShell;"
              + " an office process that is already running will not be detected.");
      return PureJavaProcessManager.getDefault();
    } else {
      return PureJavaProcessManager.getDefault();
    }
  }

  /**
   * Finds the specified number of distinct TCP ports that are free at the time of the call. The
   * ports are released before returning, so another program could take one of them before it is
   * used.
   *
   * @param count The number of ports to find, greater than 0.
   * @return The free port numbers.
   * @throws IllegalStateException If the free ports cannot be found.
   */
  // The sockets are all kept open until every port is found, so that the ports are distinct,
  // and closed together in the finally block.
  @SuppressWarnings({"PMD.CloseResource", "PMD.UseTryWithResources"})
  /* default */ static @NonNull List<@NonNull Integer> findFreePorts(final int count) {
    final var sockets = new ArrayList<ServerSocket>(count);
    try {
      for (var i = 0; i < count; i++) {
        sockets.add(new ServerSocket(0));
      }
      return sockets.stream().map(ServerSocket::getLocalPort).toList();
    } catch (IOException ex) {
      throw new IllegalStateException(String.format("Could not find %d free ports", count), ex);
    } finally {
      for (final var socket : sockets) {
        try {
          socket.close();
        } catch (IOException ex) {
          LOGGER.debug("Could not close the socket used to find a free port", ex);
        }
      }
    }
  }

  /**
   * Builds an array of {@link OfficeUrl} from an array of port numbers and an array of pipe names.
   *
   * @param portNumbers The port numbers from which office URLs will be created, may be null.
   * @param pipeNames The pipe names from which office URLs will be created, may be null.
   * @return an list of office URL. If both arguments are null, then an array is returned with a
   *     single office URL, using the default port number 2002.
   */
  /* default */ static @NonNull List<@NonNull OfficeUrl> buildOfficeUrls(
      final @Nullable List<@NonNull Integer> portNumbers,
      final @Nullable List<@NonNull String> pipeNames) {
    return buildOfficeUrls(null, portNumbers, pipeNames, null);
  }

  /**
   * Builds an array of {@link OfficeUrl} from an array of port numbers and an array of pipe names.
   *
   * @param host The host to which open ports belong, may be null.
   * @param portNumbers The port numbers from which office URLs will be created, may be null.
   * @param pipeNames The pipe names from which office URLs will be created, may be null.
   * @param websocketUrls The websocket urls from which office URLs will be created, may be null.
   * @return a list of office URL. If both arguments are null, then an array is returned with a
   *     single office URL, using the default port number 2002.
   */
  /* default */
  static @NonNull List<@NonNull OfficeUrl> buildOfficeUrls(
      final @Nullable String host,
      final @Nullable List<@NonNull Integer> portNumbers,
      final @Nullable List<@NonNull String> pipeNames,
      final @Nullable List<@NonNull String> websocketUrls) {

    // Assign default value if no pipe names, port numbers or websocketUrls have been specified.
    if ((portNumbers == null || portNumbers.isEmpty())
        && (pipeNames == null || pipeNames.isEmpty())
        && (websocketUrls == null || websocketUrls.isEmpty())) {
      return List.of(new OfficeUrl(host, DEFAULT_PORT));
    }

    // Build the office URL list and return it
    final var officeUrls = new ArrayList<OfficeUrl>();
    if (portNumbers != null) {
      portNumbers.stream().map(p -> new OfficeUrl(host, p)).forEach(officeUrls::add);
    }
    if (pipeNames != null) {
      pipeNames.stream().map(OfficeUrl::new).forEach(officeUrls::add);
    }
    if (websocketUrls != null) {
      websocketUrls.stream().map(OfficeUrl::createForWebsocket).forEach(officeUrls::add);
    }
    return officeUrls;
  }

  /**
   * Gets the default office home directory, which is auto-detected.
   *
   * @return A {@code File} instance that is the directory where lives the first detected office
   *     installation.
   */
  public static @NonNull File getDefaultOfficeHome() {
    return DefaultOfficeHomeHolder.INSTANCE;
  }

  /**
   * Gets the {@link DocumentFamily} of the specified document, without throwing an exception if not
   * found.
   *
   * @param document The document whose family will be returned. Must not be null.
   * @return The {@link DocumentFamily} for the specified document, or {@code null} if the document
   *     does not represent any supported document family.
   */
  public static @Nullable DocumentFamily getDocumentFamilySilently(
      final @NonNull XComponent document) {
    Objects.requireNonNull(document, "document must not be null");

    final var serviceInfo = Lo.qi(XServiceInfo.class, document);
    // NOTE: a GenericTextDocument is either a TextDocument, a WebDocument, or a GlobalDocument.
    // So we must test for WebDocument first.
    if (serviceInfo.supportsService(Lo.WEB_SERVICE)) {
      return DocumentFamily.WEB;
    } else if (serviceInfo.supportsService(Lo.WRITER_SERVICE)) {
      return DocumentFamily.TEXT;
    } else if (serviceInfo.supportsService(Lo.CALC_SERVICE)) {
      return DocumentFamily.SPREADSHEET;
    } else if (serviceInfo.supportsService(Lo.IMPRESS_SERVICE)) {
      return DocumentFamily.PRESENTATION;
    } else if (serviceInfo.supportsService(Lo.DRAW_SERVICE)) {
      return DocumentFamily.DRAWING;
    }

    return null;
  }

  /**
   * Gets the {@link DocumentFamily} of the specified document.
   *
   * @param document The document whose family will be returned. Must not be null.
   * @return The {@link DocumentFamily} for the specified document.
   * @throws OfficeException If the document family cannot be retrieved.
   */
  public static @NonNull DocumentFamily getDocumentFamily(final @NonNull XComponent document)
      throws OfficeException {

    final var family = getDocumentFamilySilently(document);
    if (family == null) {
      throw new OfficeException("Document of unknown family: " + document.getClass().getName());
    }
    return family;
  }

  /**
   * Gets the office executable within an office installation.
   *
   * @param officeHome The root (home) directory of the office installation.
   * @return Aninstance of the executable file.
   */
  public static @NonNull File getOfficeExecutable(final @NonNull File officeHome) {

    // Mac
    if (OSUtils.IS_OS_MAC) {
      // Starting with LibreOffice 4.1 the location of the executable has changed on Mac.
      // It's now in program/soffice. Handle both cases!
      var executableFile = new File(officeHome, EXECUTABLE_MAC_41);
      if (!executableFile.isFile()) {
        executableFile = new File(officeHome, EXECUTABLE_MAC);
      }
      return executableFile;
    }

    // Windows
    if (OSUtils.IS_OS_WINDOWS) {
      return new File(officeHome, EXECUTABLE_WINDOWS);
    }

    // Everything else
    return new File(officeHome, EXECUTABLE_DEFAULT);
  }

  /**
   * Converts a regular java map to an array of {@code PropertyValue}, usable as arguments with UNO
   * interface types.
   *
   * @param properties The map to convert.
   * @return An array of {@code PropertyValue}.
   */
  public static @NonNull PropertyValue[] toUnoProperties(
      final @NonNull Map<@NonNull String, @NonNull Object> properties) {

    final var propertyValues = new ArrayList<PropertyValue>(properties.size());
    for (final var entry : properties.entrySet()) {
      var value = entry.getValue();
      if (value instanceof Map<?, ?> subProperties) {
        @SuppressWarnings("unchecked")
        final var typed = (Map<String, Object>) subProperties;
        value = toUnoProperties(typed);
      }
      propertyValues.add(Props.makeProperty(entry.getKey(), value));
    }
    return propertyValues.toArray(new PropertyValue[0]);
  }

  /**
   * Constructs a URL from the specified file as expected by office.
   *
   * @param file The file for which a URL will be constructed.
   * @return A valid office URL.
   */
  public static @NonNull String toUrl(final @NonNull File file) {

    final var path = file.toURI().getRawPath();
    final var url = path.startsWith("//") ? "file:" + path : "file://" + path;
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  /**
   * Validates that the specified File instance is a valid office home directory.
   *
   * @param officeHome The home to validate.
   * @throws IllegalStateException If the specified directory if not a valid office home directory.
   */
  public static void validateOfficeHome(final @NonNull File officeHome) {
    Objects.requireNonNull(officeHome, "officeHome must not be null");

    if (!officeHome.isDirectory()) {
      throw new IllegalStateException(
          "officeHome doesn't exist or is not a directory: " + officeHome);
    }

    if (!getOfficeExecutable(officeHome).isFile()) {
      throw new IllegalStateException(
          "Invalid officeHome: it doesn't contain the office executable: "
              + getOfficeExecutable(officeHome));
    }
  }

  /**
   * Validates that the specified File instance is a valid office template profile directory.
   *
   * @param templateProfileDir The directory to validate.
   * @throws IllegalStateException If the specified directory is not a valid office template profile
   *     directory.
   */
  public static void validateOfficeTemplateProfileDirectory(
      final @Nullable File templateProfileDir) {

    // Template profile directory is not required.
    if (templateProfileDir == null || new File(templateProfileDir, "user").isDirectory()) {
      return;
    }

    throw new IllegalStateException(
        "templateProfileDir doesn't appear to contain a user profile: " + templateProfileDir);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private LocalOfficeUtils() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
