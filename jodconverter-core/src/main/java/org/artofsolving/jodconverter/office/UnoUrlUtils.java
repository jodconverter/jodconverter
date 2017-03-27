/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.office;

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
    return UnoUrl.parseUnoUrl("pipe,name=" + pipeName + ";urp;StarOffice.ServiceManager");
  }

  /**
   * Creates an UnoUrl for the specified port.
   *
   * @param port the port.
   * @return the created UnoUrl.
   */
  public static UnoUrl socket(final int port) {
    return UnoUrl.parseUnoUrl(
        "socket,host=127.0.0.1,port=" // NOSONAR
            + port
            + ",tcpNoDelay=1;urp;StarOffice.ServiceManager");
  }

  // Private ctor.
  private UnoUrlUtils() { // NOSONAR
    throw new AssertionError("utility class must not be instantiated");
  }
}
