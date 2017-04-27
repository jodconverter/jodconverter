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

import com.sun.star.lib.uno.helper.UnoUrl;

/**
 * Helper class used to deal with UNO Interprocess Connection type and parameters.
 *
 * <p>OpenOffice.org supports two connection types: TCP sockets and named pipes. Named pipes are
 * marginally faster and do not take up a TCP port, but they require native libraries, which means
 * setting <em>java.library.path</em> when starting Java. E.g. on Linux
 *
 * <pre>
 * java -Djava.library.path=/opt/openoffice.org/ure/lib ...
 * </pre>
 *
 * <p>See <a href=
 * "http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/ProUNO/Opening_a_Connection">
 * Opening a Connection</a> in the OpenOffice.org Developer's Guide for more details.
 */
final class UnoUrlUtils {

  /**
   * Creates an UnoUrl for the specified pipe.
   *
   * @param pipe the pipe.
   * @return the created UnoUrl.
   */
  public static UnoUrl pipe(final String pipeName) {

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
  public static UnoUrl socket(final int port) {

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

  // Private ctor.
  private UnoUrlUtils() { // NOSONAR
    throw new AssertionError("utility class must not be instantiated");
  }
}
