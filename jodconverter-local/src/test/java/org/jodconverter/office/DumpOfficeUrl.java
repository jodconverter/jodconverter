/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2019 Simon Braconnier and contributors
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

public final class DumpOfficeUrl {

  private static final Logger LOGGER = LoggerFactory.getLogger(DumpOfficeUrl.class);

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
      OfficeUrl url = new OfficeUrl(2002);

      LOGGER.info("WITH PORT");
      LOGGER.info("url.getConnection(): {}", url.getConnection());
      LOGGER.info(
          "url.getConnectionAndParametersAsString(): {}", url.getConnectionAndParametersAsString());
      LOGGER.info(
          "url.getConnectionParametersAsString(): {}", url.getConnectionParametersAsString());
      LOGGER.info("url.getConnectionParameters(): {}", url.getConnectionParameters());
      LOGGER.info("url.getProtocol(): {}", url.getProtocol());
      LOGGER.info(
          "url.getProtocolAndParametersAsString(): {}", url.getProtocolAndParametersAsString());
      LOGGER.info("url.getProtocolParametersAsString(): {}", url.getProtocolParametersAsString());
      LOGGER.info("url.getProtocolParameters(): {}", url.getProtocolParameters());
      LOGGER.info("url.getRootOid(): {}", url.getRootOid());

      LOGGER.info("");
      LOGGER.info("");

      url = new OfficeUrl("office");

      LOGGER.info("WITH PIPE");
      LOGGER.info("url.getConnection(): {}", url.getConnection());
      LOGGER.info(
          "url.getConnectionAndParametersAsString(): {}", url.getConnectionAndParametersAsString());
      LOGGER.info(
          "url.getConnectionParametersAsString(): {}", url.getConnectionParametersAsString());
      LOGGER.info("url.getConnectionParameters(): {}", url.getConnectionParameters());
      LOGGER.info("url.getProtocol(): {}", url.getProtocol());
      LOGGER.info(
          "url.getProtocolAndParametersAsString(): {}", url.getProtocolAndParametersAsString());
      LOGGER.info("url.getProtocolParametersAsString(): {}", url.getProtocolParametersAsString());
      LOGGER.info("url.getProtocolParameters(): {}", url.getProtocolParameters());
      LOGGER.info("url.getRootOid(): {}", url.getRootOid());
    } catch (Exception ex) { // NOSONAR
      throw new IllegalArgumentException(ex);
    }
  }

  private DumpOfficeUrl() {
    super();
  }
}
