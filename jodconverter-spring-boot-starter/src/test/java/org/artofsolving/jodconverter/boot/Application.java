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

package org.artofsolving.jodconverter.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot main application class. Serves as both the runtime application entry point and the
 * central Java configuration class.
 */
@SpringBootApplication
public class Application {

  /**
   * Entry point for the application.
   *
   * @param args Command line arguments.
   * @throws Exception Thrown when an unexpected Exception is thrown from the application.
   */
  public static void main(final String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }
}
