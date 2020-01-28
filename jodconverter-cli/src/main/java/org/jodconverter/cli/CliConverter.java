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

package org.jodconverter.cli;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;

/**
 * Converts document(s) according to the command line given to the {@link Convert} class.
 *
 * @see Convert
 */
public final class CliConverter {

  private final PrintWriter out;
  private final DocumentConverter converter;

  /**
   * Creates a new instance of the class that will use the specified manager.
   *
   * @param converter The converter responsible of the conversion.
   */
  public CliConverter(final DocumentConverter converter) {

    this.out = new PrintWriter(System.out);
    this.converter = converter;
  }

  /**
   * Converts the specified files into the specified format.
   *
   * @param filenames An array containing the filenames of the files to convert.
   * @param outputFormat The output format to convert to files to.
   * @param outputDirPath The directory where to create a converted file. If null, a converted file
   *     will be created into the same directory as the input file.
   * @param overwrite Indicates whether an output file that already exists must be overwritten.
   * @throws org.jodconverter.core.office.OfficeException If an error occurs while converting the
   *     files.
   */
  public void convert(
      final String[] filenames,
      final String outputFormat,
      final String outputDirPath,
      final boolean overwrite)
      throws OfficeException {

    Validate.notEmpty(filenames, "The validated filenames array is empty");
    Validate.notEmpty(outputFormat, "The validated output format is empty");

    // Prepare the output directory
    final File outputDir = outputDirPath == null ? null : new File(outputDirPath);
    prepareOutputDir(outputDir);

    // For all the filenames... Note that a filename may contains wildcards.
    for (final String filename : filenames) {

      // Create a file instance with the argument and also get the parent directory
      final File inputFile = new File(filename);

      // If the filename is a file, we will have only 1
      // file to convert for this loop iteration
      if (inputFile.isFile()) {

        // Convert the file
        convertFile(
            inputFile,
            outputDir == null ? inputFile.getParentFile() : outputDir,
            FilenameUtils.getBaseName(inputFile.getName()) + "." + outputFormat,
            overwrite);

      } else {

        // If the filename is not a file, check if it has wildcards
        // to match multiple files
        final File inputFileParent = inputFile.getParentFile();
        if (inputFileParent.isDirectory()) {
          final String wildcard = FilenameUtils.getBaseName(filename);
          final File[] files =
              inputFileParent.listFiles((FileFilter) new WildcardFileFilter(wildcard));
          if (files != null) {
            for (final File file : files) {

              // Convert the file
              convertFile(
                  file,
                  outputDir == null ? inputFile.getParentFile() : outputDir,
                  FilenameUtils.getBaseName(file.getName()) + "." + outputFormat,
                  overwrite);
            }
          }
        } else {
          printInfo("Skipping filename '%s' since it doesn't match an existing file...", inputFile);
        }
      }
    }
  }

  /**
   * Converts the specified files. The output format are generate from the output filenames.
   *
   * @param inputFilenames An array containing the filenames of the files to convert.
   * @param outputFilenames An array containing the output filenames to convert into.
   * @param outputDirPath The directory where to create a converted file if not specified in the
   *     output filename. If null and the directory is not specified in the output filename, a
   *     converted file will be created into the same directory as the input files.
   * @param overwrite Indicates whether an output file that already exists must be overwritten.
   * @throws org.jodconverter.core.office.OfficeException If an error occurs while converting the
   *     files.
   */
  public void convert(
      final String[] inputFilenames,
      final String[] outputFilenames,
      final String outputDirPath,
      final boolean overwrite)
      throws OfficeException {

    Validate.notEmpty(inputFilenames, "The validated input filenames array is empty");
    Validate.notEmpty(outputFilenames, "The validated output filenames array is empty");

    final int inputLength = inputFilenames.length;
    final int outputLength = outputFilenames.length;

    // Make sure lengths are ok, these need to be equal
    Validate.isTrue(
        inputLength == outputLength,
        "input filenames array length [%d] and output filenames array length [%d] don't match",
        inputLength,
        outputLength);

    // Prepare the output directory
    final File outputDir = outputDirPath == null ? null : new File(outputDirPath);
    prepareOutputDir(outputDir);

    // For all the input/output filename pairs...
    for (int i = 0; i < inputFilenames.length; i++) {

      // Get the input and output files
      final String inputFilename = inputFilenames[i];
      final String inputFullPath = FilenameUtils.getFullPath(inputFilename);
      final String outputFilename = outputFilenames[i];
      final String outputFullPath = FilenameUtils.getFullPath(outputFilename);
      final File outputDirectory =
          StringUtils.isBlank(outputFullPath)
              ? outputDir == null
                  ? StringUtils.isBlank(inputFullPath) ? new File(".") : new File(inputFullPath)
                  : outputDir
              : new File(outputFullPath);

      // Convert the file
      convertFile(
          new File(inputFilename),
          outputDirectory,
          FilenameUtils.getName(outputFilename),
          overwrite);
    }
  }

  private void convert(final File inputFile, final File outputFile) throws OfficeException {

    printInfo("Converting '%s' to '%s'", inputFile, outputFile);
    converter.convert(inputFile).to(outputFile).execute();
  }

  private void convertFile(
      final File inputFile,
      final File outputDir,
      final String outputFilename,
      final boolean overwrite)
      throws OfficeException {

    // First validate the input file is a valid source file
    if (validateInputFile(inputFile)) {

      // Create output file instance and validate that it is a valid target
      final File outputFile = new File(outputDir, outputFilename);
      if (validateOutputFile(inputFile, outputFile, overwrite)) {

        // We can now convert the document
        convert(inputFile, outputFile);
      }
    }
  }

  private void prepareOutputDir(final File outputDir) throws OfficeException {

    if (outputDir != null) {
      try {

        if (outputDir.exists()) {
          if (outputDir.isFile()) {
            throw new IOException(
                "Invalid output directory '" + outputDir + "' that exists but is a file");
          }

          if (!outputDir.canWrite()) {
            throw new IOException(
                "Invalid output directory '" + outputDir + "' that cannot be written to");
          }

        } else {
          // Create the output directory
          outputDir.mkdirs();
        }

      } catch (IOException ex) {
        throw new OfficeException("Unable to prepare the output directory", ex);
      }
    }
  }

  private void printInfo(final String message, final Object... values) {

    out.println(String.format(message, values));
    out.flush();
  }

  private boolean validateInputFile(final File inputFile) {

    if (inputFile.exists()) {
      if (inputFile.isDirectory()) {
        printInfo("Skipping file '%s' that exists but is a directory", inputFile);
        return false;
      }

      if (!inputFile.canRead()) {
        printInfo("Skipping file '%s' that cannot be read", inputFile);
        return false;
      }

    } else {
      printInfo("Skipping file '%s' that does not exist", inputFile);
      return false;
    }
    return true;
  }

  private boolean validateOutputFile(
      final File inputFile, final File outputFile, final boolean overwrite) {

    if (outputFile.exists()) {

      if (outputFile.isDirectory()) {
        printInfo(
            "Skipping file '%s' because the output file '%s' already exists and is a directory",
            inputFile, outputFile);
        return false;
      }

      if (!overwrite) {
        printInfo(
            "Skipping file '%s' because the output file '%s' already exists and the "
                + "overwrite switch is off",
            inputFile, outputFile);
        return false;
      }

      if (!outputFile.delete()) {
        printInfo(
            "Skipping file '%s' because the output file '%s' already exists and cannot be deleted",
            inputFile, outputFile);
        return false;
      }
    }

    return true;
  }
}
