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

import static org.assertj.core.api.Assertions.assertThat;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.office.LocalOfficeManager;

/**
 * Tests the hardening recipe of the security page of the documentation: what an office with its
 * default settings fetches when it loads a document that links to an image, and what the settings
 * of a template profile directory change. The profile of the recipe is {@code
 * src/integTest/resources/hardenedProfileDir}.
 */
class HardenedProfileITest {

  // A port of its own: the test starts offices with different profiles.
  private static final int PORT = 2014;
  private static final File HARDENED_PROFILE =
      new File("src/integTest/resources/hardenedProfileDir");

  private static final String XCU_START =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<oor:items xmlns:oor=\"http://openoffice.org/2001/registry\""
          + " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
          + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
  private static final String XCU_END = "</oor:items>\n";

  // The first line of each request received by the fake web server, which answers 404.
  private final List<String> requests = new CopyOnWriteArrayList<>();
  private ServerSocket server;

  @BeforeEach
  void startServer() throws IOException {

    server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
    final var thread =
        new Thread(
            () -> {
              while (!server.isClosed()) {
                try (var socket = server.accept()) {
                  final var reader =
                      new BufferedReader(
                          new InputStreamReader(
                              socket.getInputStream(), StandardCharsets.ISO_8859_1));
                  requests.add(String.valueOf(reader.readLine()));
                  socket
                      .getOutputStream()
                      .write(
                          "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                              .getBytes(StandardCharsets.ISO_8859_1));
                } catch (IOException ignored) {
                  // The server is closed, or the office closed the connection first.
                }
              }
            },
            "fake-web-server");
    thread.setDaemon(true);
    thread.start();
  }

  @AfterEach
  void stopServer() throws IOException {
    server.close();
  }

  // A text document with one image, linked to the given URL.
  private static File documentLinkingTo(final File dir, final String name, final String url)
      throws IOException {

    final var document = new File(dir, name + ".fodt");
    Files.writeString(
        document.toPath(),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<office:document"
            + " xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\""
            + " xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\""
            + " xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\""
            + " xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\""
            + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" office:version=\"1.3\""
            + " office:mimetype=\"application/vnd.oasis.opendocument.text\">"
            + "<office:body><office:text><text:p>Linked image "
            + "<draw:frame draw:name=\"image\" text:anchor-type=\"as-char\" svg:width=\"2cm\""
            + " svg:height=\"2cm\"><draw:image xlink:href=\""
            + url
            + "\" xlink:type=\"simple\" xlink:show=\"embed\" xlink:actuate=\"onLoad\"/>"
            + "</draw:frame></text:p></office:text></office:body></office:document>\n",
        StandardCharsets.UTF_8);
    return document;
  }

  private static int imageCount(final File pdf) throws IOException {

    try (var document = Loader.loadPDF(pdf)) {
      final var resources = document.getPage(0).getResources();
      var count = 0;
      for (final var name : resources.getXObjectNames()) {
        if (resources.getXObject(name) instanceof PDImageXObject) {
          count++;
        }
      }
      return count;
    }
  }

  // Converts each document to PDF with an office that uses the given template profile, if any.
  private static void convert(final File templateProfileDir, final File... documents)
      throws OfficeException {

    final var manager =
        LocalOfficeManager.builder()
            .portNumbers(PORT)
            .templateProfileDir(templateProfileDir)
            .build();
    manager.start();
    try {
      final var converter = LocalConverter.builder().officeManager(manager).build();
      for (final var document : documents) {
        converter.convert(document).to(pdfOf(document)).execute();
      }
    } finally {
      OfficeUtils.stopQuietly(manager);
    }
  }

  private static File pdfOf(final File document) {
    return new File(document.getParentFile(), document.getName() + ".pdf");
  }

  private String serverUrl(final String path) {
    return "http://127.0.0.1:" + server.getLocalPort() + path;
  }

  @Test
  void withDefaultSettings_ShouldFetchTheLinkedImages(final @TempDir File dir) throws Exception {

    final var local =
        documentLinkingTo(dir, "local", documentFile("smiley.jpg").toURI().toString());
    final var remote = documentLinkingTo(dir, "remote", serverUrl("/image.png"));

    convert(null, local, remote);

    // The image of the file system is in the PDF, and the web server was asked for the other.
    assertThat(imageCount(pdfOf(local))).isEqualTo(1);
    assertThat(requests).isNotEmpty().allMatch(request -> request.contains(" /image.png "));
  }

  @Test
  void withHardenedProfile_ShouldNotFetchTheLinkedImages(final @TempDir File dir) throws Exception {

    final var local =
        documentLinkingTo(dir, "local", documentFile("smiley.jpg").toURI().toString());
    final var remote = documentLinkingTo(dir, "remote", serverUrl("/image.png"));

    convert(HARDENED_PROFILE, local, remote);

    assertThat(imageCount(pdfOf(local))).isZero();
    assertThat(requests).isEmpty();
  }

  @Test
  void withProxySettings_ShouldSendTheRequestsToTheProxy(final @TempDir File dir) throws Exception {

    // The fake web server plays the proxy; the host of the link does not exist.
    final var inet = "<item oor:path=\"/org.openoffice.Inet/Settings\"><prop oor:name=\"";
    final var fuse = "\" oor:op=\"fuse\"><value>";
    final var end = "</value></prop></item>\n";
    final var port = server.getLocalPort();
    final var user = new File(dir, "profile/user");
    Files.createDirectories(user.toPath());
    Files.writeString(
        new File(user, "registrymodifications.xcu").toPath(),
        XCU_START
            + inet
            + "ooInetProxyType"
            + fuse
            + 2
            + end
            + inet
            + "ooInetHTTPProxyName"
            + fuse
            + "127.0.0.1"
            + end
            + inet
            + "ooInetHTTPProxyPort"
            + fuse
            + port
            + end
            + inet
            + "ooInetHTTPSProxyName"
            + fuse
            + "127.0.0.1"
            + end
            + inet
            + "ooInetHTTPSProxyPort"
            + fuse
            + port
            + end
            + XCU_END,
        StandardCharsets.UTF_8);
    final var http = documentLinkingTo(dir, "http", "http://jodconverter.invalid/image.png");
    final var https = documentLinkingTo(dir, "https", "https://jodconverter.invalid/image.png");

    convert(user.getParentFile(), http, https);

    assertThat(requests)
        .anyMatch(request -> request.contains(" http://jodconverter.invalid/image.png "))
        .anyMatch(request -> request.startsWith("CONNECT jodconverter.invalid:443 "));
  }
}
