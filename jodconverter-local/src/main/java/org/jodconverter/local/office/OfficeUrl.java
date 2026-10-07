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

import com.sun.star.lib.uno.helper.UnoUrl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wrapper class around an UnoUrl, so we are not importing the com.sun.star.lib.uno.helper.UnoUrl
 * package everywhere. UnoUrl are used to deal with UNO Interprocess Connection type and parameters.
 *
 * <p>OpenOffice.org supports two connection types: TCP sockets and named pipes. Named pipes are
 * marginally faster and do not take up a TCP port, but they require native libraries, which means
 * setting <em>java.library.path</em> when starting Java. E.g. on Linux
 *
 * <pre>
 * java -Djava.library.path=/opt/openoffice.org/ure/lib ...
 * </pre>
 *
 * <p>See <a
 * href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/ProUNO/Opening_a_Connection">Opening
 * a Connection</a> and <a href="http://www.openoffice.org/udk/common/man/spec/uno-url.html">UNO Url
 * - Specification</a> in the OpenOffice.org Developer's Guide for more details.
 */
record OfficeUrl(UnoUrl unoUrl) {

  /**
   * Creates an UnoUrl for the specified pipe.
   *
   * @param pipeName The pipe name.
   * @return The created UnoUrl.
   */
  /* default */
  static UnoUrl pipe(final @NonNull String pipeName) {

    // Here we must use a try catch since OpenOffice and LibreOffice doesn't
    // have the same UnoUrl.parseUnoUrl signature
    try {
      return UnoUrl.parseUnoUrl("pipe,name=" + pipeName + ";urp;StarOffice.ServiceManager");
    } catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * Creates an UnoUrl for the specified port on host 127.0.0.1.
   *
   * @param port The port.
   * @return The created UnoUrl.
   */
  /* default */
  static UnoUrl socket(final int port) {
    return socket(null, port);
  }

  /**
   * Creates an UnoUrl for the specified port.
   *
   * @param host The host. Uses 127.0.0.1 if null.
   * @param port The port.
   * @return The created UnoUrl.
   */
  /* default */
  static UnoUrl socket(final String host, final int port) {

    final var h = host == null ? LocalOfficeManager.DEFAULT_HOSTNAME : host;
    // Here we must use a try catch since OpenOffice and LibreOffice doesn't
    // have the same UnoUrl.parseUnoUrl signature
    try {
      return UnoUrl.parseUnoUrl(
          "socket,host=" + h + ",port=" + port + ",tcpNoDelay=1;urp;StarOffice.ServiceManager");
    } catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * Creates an UnoUrl for the specified websocket url.
   *
   * @param url The url.
   * @return The created UnoUrl.
   */
  /* default */
  static UnoUrl websocket(final String url) {
    try {
      return UnoUrl.parseUnoUrl("uno:websocket,url=" + url + ";urp;StarOffice.ServiceManager");
    } catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * Creates an OfficeUrl for the specified websocket url.
   *
   * @param url The websocket url.
   */
  /* default */
  static OfficeUrl createForWebsocket(final String url) {
    return new OfficeUrl(websocket(url));
  }

  /**
   * Creates an OfficeUrl for the specified pipe.
   *
   * @param pipeName The pipe name.
   */
  public OfficeUrl(final @NonNull String pipeName) {
    this(pipe(pipeName));
  }

  /**
   * Creates an OfficeUrl for the specified port on host 127.0.0.1
   *
   * @param port The port.
   */
  public OfficeUrl(final int port) {
    this(LocalOfficeManager.DEFAULT_HOSTNAME, port);
  }

  /**
   * Creates an OfficeUrl for the specified port.
   *
   * @param host The host, may be null.
   * @param port The port.
   */
  public OfficeUrl(final @Nullable String host, final int port) {
    this(socket(host, port));
  }

  /**
   * Creates an OfficeUrl from a given UnoUrl.
   *
   * @param unoUrl The UnoUrl
   */
  OfficeUrl(final @NonNull UnoUrl unoUrl) {
    this.unoUrl = unoUrl;
  }

  /**
   * Returns the string that should be used as --accept argument when an office process is launched.
   *
   * @return The "accept" string.
   */
  public String getAcceptString() {
    return unoUrl.getConnectionAndParametersAsString()
        + ";"
        + unoUrl.getProtocolAndParametersAsString()
        + ";"
        + unoUrl.getRootOid();
  }

  /**
   * Returns the raw specification of the connection name and parameters. Encoded characters like
   * '%41' are not decoded.
   *
   * @return The uninterpreted connection name and parameters as string.
   */
  public String getConnectString() {
    return unoUrl.getConnectionAndParametersAsString();
  }

  @Override
  public String toString() {
    return unoUrl.toString();
  }
}
