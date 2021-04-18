/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
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

package org.jodconverter.local.interaction;

import com.sun.star.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordInteractionHandler implements XInteractionHandler {

  public ThreadLocal<PasswordRequest> passwordRequests = ThreadLocal.withInitial(() -> null);

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordInteractionHandler.class);

  @Override
  public void handle(XInteractionRequest xInteractionRequest) {
    LOGGER.debug(
        "Interaction detected with following request {}", xInteractionRequest.getRequest());

    Object request = xInteractionRequest.getRequest();

    if (request instanceof PasswordRequest) {
      String documentPath = "n.a.";
      if (request instanceof DocumentPasswordRequest) {
        documentPath = ((DocumentPasswordRequest) request).Name;
      }

      if (request instanceof DocumentMSPasswordRequest) {
        documentPath = ((DocumentMSPasswordRequest) request).Name;
      }

      LOGGER.debug("Password interaction detected for " + documentPath);

      passwordRequests.set((PasswordRequest) request);
    }
  }
}
