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

package org.jodconverter.office;

import java.util.Map;

import com.sun.star.lib.uno.helper.UnoUrl;

/**
 * Wrapper class around an UnoUrl so we are not importing the com.sun.star.lib.uno.helper.UnoUrl
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
 * <p>See <a href= <a
 * href="https://wiki.openoffice.org/wiki/Documentation/DevGuide/ProUNO/Opening_a_Connection">Opening
 * a Connection</a> and <a href="http://www.openoffice.org/udk/common/man/spec/uno-url.html">UNO Url
 * - Specification</a> in the OpenOffice.org Developer's Guide for more details.
 */
public class OfficeUrl {

  /**
   * Creates an UnoUrl for the specified pipe.
   *
   * @param pipeName the pipe name.
   * @return the created UnoUrl.
   */
  private static UnoUrl pipe(final String pipeName) {

    // Here we must use a try catch since OpenOffice and LibreOffice doesn't
    // have the same UnoUrl.parseUnoUrl signature
    try {
      return UnoUrl.parseUnoUrl("pipe,name=" + pipeName + ";urp;StarOffice.ServiceManager");
    } catch (Exception ex) { // NOSONAR
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * Creates an UnoUrl for the specified port.
   *
   * @param port the port.
   * @return the created UnoUrl.
   */
  private static UnoUrl socket(final int port) {

    // Here we must use a try catch since OpenOffice and LibreOffice doesn't
    // have the same UnoUrl.parseUnoUrl signature
    try {
      return UnoUrl.parseUnoUrl(
          "socket,host=127.0.0.1,port=" // NOSONAR
              + port
              + ",tcpNoDelay=1;urp;StarOffice.ServiceManager");
    } catch (Exception ex) { // NOSONAR
      throw new IllegalArgumentException(ex);
    }
  }

  private final UnoUrl unoUrl;

  /**
   * Creates an OfficeUrl for the specified pipe.
   *
   * @param pipeName the pipe name.
   */
  public OfficeUrl(final String pipeName) {

    this.unoUrl = pipe(pipeName);
  }

  /**
   * Creates an OfficeUrl for the specified port.
   *
   * @param port the port.
   */
  public OfficeUrl(final int port) {

    this.unoUrl = socket(port);
  }

  /**
   * Returns the name of the connection of this Uno Url. Encoded characters are not allowed.
   *
   * @return The connection name as string.
   */
  public String getConnection() {
    return unoUrl.getConnection();
  }

  /**
   * Returns the name of the protocol of this Uno Url. Encoded characters are not allowed.
   *
   * @return The protocol name as string.
   */
  public String getProtocol() {
    return unoUrl.getProtocol();
  }

  /**
   * Return the object name. Encoded character are not allowed.
   *
   * @return The object name as String.
   */
  public String getRootOid() {
    return unoUrl.getRootOid();
  }

  /**
   * Returns the protocol parameters as a Hashmap with key/value pairs. Encoded characters like
   * '%41' are decoded.
   *
   * @return a HashMap with key/value pairs for protocol parameters.
   */
  public Map<String, String> getProtocolParameters() {
    return unoUrl.getProtocolParameters();
  }

  /**
   * Returns the connection parameters as a Hashmap with key/value pairs. Encoded characters like
   * '%41' are decoded.
   *
   * @return a HashMap with key/value pairs for connection parameters.
   */
  public Map<String, String> getConnectionParameters() {
    return unoUrl.getConnectionParameters();
  }

  /**
   * Returns the raw specification of the protocol parameters. Encoded characters like '%41' are not
   * decoded.
   *
   * @return The uninterpreted protocol parameters as string.
   */
  public String getProtocolParametersAsString() {
    return unoUrl.getProtocolParametersAsString();
  }

  /**
   * Returns the raw specification of the connection parameters. Encoded characters like '%41' are
   * not decoded.
   *
   * @return The uninterpreted connection parameters as string.
   */
  public String getConnectionParametersAsString() {
    return unoUrl.getConnectionParametersAsString();
  }

  /**
   * Returns the raw specification of the protocol name and parameters. Encoded characters like
   * '%41' are not decoded.
   *
   * @return The uninterpreted protocol name and parameters as string.
   */
  public String getProtocolAndParametersAsString() {
    return unoUrl.getProtocolAndParametersAsString();
  }

  /**
   * Returns the raw specification of the connection name and parameters. Encoded characters like
   * '%41' are not decoded.
   *
   * @return The uninterpreted connection name and parameters as string.
   */
  public String getConnectionAndParametersAsString() {
    return unoUrl.getConnectionAndParametersAsString();
  }
}
