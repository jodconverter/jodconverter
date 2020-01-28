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

package org.jodconverter.sample.rest;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class SpringBootRestApplication {

  public static void main(final String[] args) {
    SpringApplication.run(SpringBootRestApplication.class, args);
  }

  @Configuration
  @EnableSwagger2
  public static class SwaggerConfig {
    @Bean
    public Docket api() {
      return new Docket(DocumentationType.SWAGGER_2)
          .useDefaultResponseMessages(false)
          .select()
          .apis(RequestHandlerSelectors.basePackage("org.jodconverter.sample.rest"))
          .paths(PathSelectors.regex("/lool/convert-to.*"))
          .build()
          .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
      return new ApiInfo(
          "JODConverter REST API",
          "JODConverter REST API for Online conversion. JODConverter automates conversions between office document formats using LibreOffice or Apache OpenOffice.",
          "0.1",
          "Terms of service",
          new Contact("John Doe", "www.jodconverter.org", "johndoe@company.com"),
          "Apache License Version 2.0",
          "https://www.apache.org/licenses/LICENSE-2.0",
          Collections.emptyList());
    }
  }
}
