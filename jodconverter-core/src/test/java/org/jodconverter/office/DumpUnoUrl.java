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
