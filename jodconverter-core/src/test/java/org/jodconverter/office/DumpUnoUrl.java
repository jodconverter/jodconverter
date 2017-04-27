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

package org.jodconverter.office;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lib.uno.helper.UnoUrl;

public class DumpUnoUrl {

  private static final Logger logger = LoggerFactory.getLogger(DumpUnoUrl.class);

  /**
   * Main entry point of the program used to test this class.
   *
   * @param args program arguments.
   * @throws Exception if an error occurs.
   */
  public static void main(final String[] args) {

    // Here we must use a try catch since OpenOffice and LibreOffice doesn't
    // have the same UnoUrl.parseUnoUrl signature
    try {
      final com.sun.star.lib.uno.helper.UnoUrl url =
          UnoUrl.parseUnoUrl(
              "socket,host=127.0.0.1,port=2002,tcpNoDelay=1;urp;StarOffice.ServiceManager");

      logger.info("url.getConnection():" + url.getConnection());
      logger.info(
          "url.getConnectionAndParametersAsString():" + url.getConnectionAndParametersAsString());
      logger.info("url.getConnectionParametersAsString():" + url.getConnectionParametersAsString());
      logger.info("url.getConnectionParameters():" + url.getConnectionParameters());
      logger.info("url.getProtocol():" + url.getProtocol());
      logger.info(
          "url.getProtocolAndParametersAsString():" + url.getProtocolAndParametersAsString());
      logger.info("url.getProtocolParametersAsString():" + url.getProtocolParametersAsString());
      logger.info("url.getProtocolParameters():" + url.getProtocolParameters());
      logger.info("url.getRootOid():" + url.getRootOid());
    } catch (Exception ex) { // NOSONAR
      throw new IllegalArgumentException(ex);
    }
  }
}
