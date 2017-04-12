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

package org.artofsolving.jodconverter.process;

/** Contains the required information used to query for a running process.= */
public class ProcessQuery {

  private final String command;
  private final String argument;

  /**
   * Constructs a new instance with the given command and argument.
   *
   * @param command the process command.
   * @param argument the process argument.
   */
  public ProcessQuery(final String command, final String argument) {
    this.command = command;
    this.argument = argument;
  }

  /**
   * Gets the arguments of the process to query.
   *
   * @return the process argument.
   */
  public String getArgument() {
    return argument;
  }

  /**
   * Gets the command of the process to query.
   *
   * @return the process command.
   */
  public String getCommand() {
    return command;
  }
}
