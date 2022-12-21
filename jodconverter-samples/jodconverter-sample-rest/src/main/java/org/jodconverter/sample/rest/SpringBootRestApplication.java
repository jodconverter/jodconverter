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

package org.jodconverter.sample.rest;

import java.util.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Main application. */
@SpringBootApplication
public class SpringBootRestApplication {

  /**
   * Main entry point of the application.
   *
   * @param args Command line arguments.
   */
  public static void main(final String[] args) {
    SpringApplication.run(SpringBootRestApplication.class, args);
  }

  @Configuration
  static class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
      final Info info =
          new Info()
              .title("JODConverter REST API")
              .description(
                  "JODConverter REST API for Remote conversion. JODConverter automates conversions between office document formats using LibreOffice or Apache OpenOffice.")
              .version("0.1")
              .termsOfService("Terms of service")
              .license(
                  new License()
                      .name("Apache License Version 2.0")
                      .url("https://www.apache.org/licenses/LICENSE-2.0"));

      Server apiServer = new Server();
      apiServer.setDescription("local");
      apiServer.setUrl("/");

      List<Server> servers = new ArrayList<>();
      servers.add(apiServer);

      return new OpenAPI().servers(servers).info(info);
    }
  }
}
