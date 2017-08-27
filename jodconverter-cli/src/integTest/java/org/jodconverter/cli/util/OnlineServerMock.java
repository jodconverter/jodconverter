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

package org.jodconverter.cli.util;

import java.io.IOException;

//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpServer;

public class OnlineServerMock {
  //  protected HttpServer server;
  //
  //  public COnlineServerMock() {
  //    server = null;
  //  }
  //
  /** Starts a simple web server which simulates a LibreOffice Online server */
  public void listen() throws IOException {
    //server = HttpServer.create();
    //server.createContext("/lool/convert-to", new RequestHandler());
  }
  //
  public void stop() {
    //    if (server == null) {
    //      return;
    //    }
    //    server.stop(10);
  }
  //
  //  private static class RequestHandler implements HttpHandler {
  //
  //    @Override
  //    public void handle(HttpExchange httpExchange) throws IOException {
  //      String response = "Hello";
  //      httpExchange.sendResponseHeaders(200, response.length());
  //      OutputStream os = httpExchange.getResponseBody();
  //      os.write(response.getBytes());
  //      os.close();
  //    }
  //  }
}
