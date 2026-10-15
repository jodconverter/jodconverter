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

package org.jodconverter.local.task;

import com.sun.star.task.DocumentMSPasswordRequest;
import com.sun.star.task.DocumentPasswordRequest;
import com.sun.star.task.PasswordRequest;
import com.sun.star.task.XInteractionHandler;
import com.sun.star.task.XInteractionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handler used to detect password-protected file. */
final class PasswordInteractionHandler implements XInteractionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordInteractionHandler.class);

  private PasswordRequest passwordRequest;
  private String documentName;

  /** Something that loads a document with a given interaction handler. */
  /* default */ interface Load {

    /**
     * Loads the document.
     *
     * @param handler The handler that the office must be given.
     * @throws Exception If the document cannot be loaded.
     */
    void with(PasswordInteractionHandler handler) throws Exception;
  }

  /**
   * Gets the password request that the office makes when a document is loaded, if it makes one: the
   * way to know that a document is protected.
   *
   * @param load Loads the document with the handler it is given, without any password.
   * @return The password request made by the office, or null if it made none, whether the document
   *     was loaded or not.
   */
  /* default */ static PasswordRequest requestOf(final Load load) {

    final var handler = new PasswordInteractionHandler();
    try {
      load.with(handler);
    } catch (Exception exception) {
      // A load that fails says nothing more: only the request matters.
      LOGGER.debug("Loading the document without its password failed", exception);
    }
    return handler.getPasswordRequest();
  }

  /**
   * Gets the password interaction request that has been made, if any.
   *
   * @return The password interaction request that has been made, ot null if no password interaction
   *     request was made.
   */
  /* default */ PasswordRequest getPasswordRequest() {
    return passwordRequest;
  }

  /**
   * Gets whether a password interaction request has been made.
   *
   * @return {@code true} if a password interaction request was made, {@code false} otherwise.
   */
  /* default */ boolean hasPasswordInteractionRequest() {
    return passwordRequest != null;
  }

  /**
   * Gets the name of the document (more properly, the URL of the document), for which the request
   * was made.
   *
   * @return The name of the document for which the request was made, null if no password
   *     interaction request was made, or "NA" if an interaction was made, but the document name is
   *     unknown.
   */
  /* default */ String getDocumentName() {
    return documentName;
  }

  @Override
  public void handle(final XInteractionRequest interactionRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Interaction detected with request {}", interactionRequest.getRequest());
    }

    final var request = interactionRequest.getRequest();

    if (request instanceof PasswordRequest pwdRequest) {
      passwordRequest = pwdRequest;
      documentName = "NA";
      if (request instanceof DocumentPasswordRequest docRequest) {
        documentName = docRequest.Name;
      } else if (request instanceof DocumentMSPasswordRequest msDocRequest) {
        documentName = msDocRequest.Name;
      }
      LOGGER.debug("Password interaction detected for {}", documentName);
    }
  }
}
