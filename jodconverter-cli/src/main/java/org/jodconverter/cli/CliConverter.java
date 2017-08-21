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

package org.jodconverter.cli;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import org.jodconverter.DefaultConverter;
import org.jodconverter.document.DocumentFormatRegistry;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.filter.RefreshFilter;
import org.jodconverter.office.OfficeException;

/**
 * Converts document(s) according to the command line given to the {@link Convert} class.
 *
 * @see Convert
 */
public final class CliConverter {

  private final PrintWriter out;
  private final DefaultConverter converter;

  /**
   * Creates a new instance of the class that will use the specified manager.
   *
   * @param registry the document registry used by the created instance.
   */
  public CliConverter(final DocumentFormatRegistry registry) {

    this.out = new PrintWriter(System.out); // NOSONAR
    this.converter = DefaultConverter.builder().formatRegistry(registry).build();
  }

  private void convert(final FilterChain filterChain, final File inputFile, final File outputFile)
      throws OfficeException {

    printInfo("Converting '" + inputFile.getPath() + "' to '" + outputFile.getPath() + "'");
    converter
        .convert(inputFile)
        .filterWith(filterChain == null ? RefreshFilter.CHAIN : filterChain)
        .to(outputFile)
        .execute();
  }

  /**
   * Converts the specified files into the specified format.
   *
   * @param filenames an array containing the filenames of the files to convert.
   * @param outputFormat the output format to convert to files to.
   * @param outputDirPath the directory where to create a converted file. If null, a converted file
   *     will be created into the same directory as the input file.
   * @param overwrite indicates whether an output file that already exists must be overwritten.
   * @param filterChain filter chain to apply when converting a file.
   * @throws OfficeException if an error occurs while converting the files.
   */
  public void convert(
      final String[] filenames,
      final String outputFormat,
      final String outputDirPath,
      final boolean overwrite,
      final FilterChain filterChain)
      throws OfficeException {

    Validate.notEmpty(filenames, "The validated filenames array is empty");
    Validate.notEmpty(outputFormat, "The validated output format is empty");

    // Prepare the output directory
    File outputDir = null;
    if (outputDirPath != null) {
      outputDir = new File(outputDirPath);
      try {
        prepareOutputDir(outputDir);
      } catch (IOException ex) {
        throw new OfficeException("Unable to prepare the output directory", ex);
      }
    }

    // For all the filenames... Note that a filename may contains wildcards.
    for (final String filename : filenames) {

      // Create a file instance with the argument and also get the parent directory
      final File inputFile = new File(filename);
      final File inputFileParent = inputFile.getParentFile();

      // If the filename is a file, we will have only 1
      // file to convert for this loop iteration
      if (inputFile.isFile()) {

        // Create output file instance and validate that it can be converted
        final File outputFile =
            new File(
                outputDir == null ? inputFileParent : outputDir,
                FilenameUtils.getBaseName(filename) + "." + outputFormat);
        if (validateOutputFile(inputFile, outputFile, overwrite)) {

          // We can now convert the document
          convert(filterChain, inputFile, outputFile);
        }

      } else {

        // If the filename is not a file, check if it has wildcards
        // to match multiple files
        if (inputFileParent.isDirectory()) {
          final String wildcard = FilenameUtils.getBaseName(filename);
          final File[] files =
              inputFileParent.listFiles((FileFilter) new WildcardFileFilter(wildcard));
          for (final File file : files) {

            // Create output file instance and validate that it can be converted
            final File outputFile =
                new File(
                    outputDir == null ? inputFileParent : outputDir,
                    FilenameUtils.getBaseName(file.getName()) + "." + outputFormat);
            if (validateOutputFile(inputFile, outputFile, overwrite)) {

              convert(filterChain, file, outputFile);
            }
          }
        } else {
          printInfo(
              "Skipping filename '" + inputFile + "' since it doesn't match an existing file...");
        }
      }
    }
  }

  /**
   * Converts the specified files. The output format are generate from the output filenames.
   *
   * @param inputFilenames an array containing the filenames of the files to convert.
   * @param outputFilenames an array containing the output filenames to convert into.
   * @param outputDirPath the directory where to create a converted file if not specified in the
   *     output filename. If null and the directory is not specified in the output filename, a
   *     converted file will be created into the same directory as the input files.
   * @param overwrite indicates whether an output file that already exists must be overwritten.
   * @param filterChain filter chain to apply when converting a file.
   * @throws OfficeException if an error occurs while converting the files.
   */
  public void convert(
      final String[] inputFilenames,
      final String[] outputFilenames,
      final String outputDirPath,
      final boolean overwrite,
      final FilterChain filterChain)
      throws OfficeException {

    Validate.notEmpty(inputFilenames, "The validated input filenames array is empty");
    Validate.notEmpty(outputFilenames, "The validated output filenames array is empty");

    final int inputLength = inputFilenames.length;
    final int outputLength = outputFilenames.length;

    // Make sure lengths are ok, these need to be equal
    if (inputLength != outputLength) {
      throw new IllegalArgumentException(
          "Input and Output array lengths don't match: " + inputLength + " vs " + outputLength);
    }

    // Prepare the output directory
    File outputDir = null;
    if (outputDirPath != null) {
      outputDir = new File(outputDirPath);
      try {
        prepareOutputDir(outputDir);
      } catch (IOException ex) {
        throw new OfficeException("Unable to prepare the output directory", ex);
      }
    }

    // For all the input/output filename pairs...
    for (int i = 0; i < inputFilenames.length; i++) {

      // Get the input and output files
      final String inputFilename = inputFilenames[i];
      final String inputFullPath = FilenameUtils.getFullPath(inputFilename);
      final File inputFile = new File(inputFilename);
      final String outputFilename = outputFilenames[i];
      final String outputFullPath = FilenameUtils.getFullPath(outputFilename);
      final File outputDirectory =
          StringUtils.isBlank(outputFullPath)
              ? (outputDir == null // NOSONAR
                  ? (StringUtils.isBlank(inputFullPath) ? null : new File(inputFullPath)) // NOSONAR
                  : outputDir)
              : new File(outputFullPath);
      final File outputFile =
          (outputDirectory == null
              ? new File(outputFilename)
              : new File(outputDirectory, FilenameUtils.getName(outputFilename)));

      // Validate that the file can be converted
      if (validateInputFile(inputFile) && validateOutputFile(inputFile, outputFile, overwrite)) {

        convert(filterChain, inputFile, outputFile);
      }
    }
  }

  private void prepareOutputDir(final File outputDir) throws IOException {

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
  }

  private void printInfo(final String info) {

    out.println(info);
    out.flush();
  }

  private boolean validateInputFile(final File inputFile) {

    if (inputFile.exists()) {
      if (inputFile.isDirectory()) {
        printInfo("Skipping file '" + inputFile + "' that exists but is a directory");
        return false;
      }

      if (!inputFile.canRead()) {
        printInfo("Skipping file '" + inputFile + "' that cannot be read");
        return false;
      }

    } else {
      printInfo("Skipping file '" + inputFile + "' that does not exist");
      return false;
    }
    return true;
  }

  private boolean validateOutputFile(
      final File inputFile, final File outputFile, final boolean overwrite) {

    if (outputFile.exists()) {

      if (outputFile.isDirectory()) {
        printInfo(
            "Skipping file '"
                + inputFile
                + "' because the output file '"
                + outputFile
                + "' already exists and is a directory");
        return false;
      }

      if (!overwrite) {
        printInfo(
            "Skipping file '"
                + inputFile
                + "' because the output file '"
                + outputFile
                + "' already exists and the overwrite switch is off");
        return false;
      }

      if (!outputFile.delete()) {
        printInfo(
            "Skipping file '"
                + inputFile
                + "' because the output file '"
                + outputFile
                + "' already exists and cannot be deleted");
        return false;
      }
    }

    return true;
  }
}
