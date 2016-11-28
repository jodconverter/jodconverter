//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2011 Mirko Nasato and contributors
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter. If not, see
// <http://www.gnu.org/licenses/>.
//
package org.artofsolving.jodconverter;

import java.io.File;

import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

@Test(groups = "functional")
public class StressTest {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(StressTest.class);

    private static final int MAX_CONVERSIONS = 1024;
    private static final int MAX_RUNNING_THREADS = 128;
    private static final int MAX_TASKS_PER_PROCESS = 10;

    private static final String INPUT_EXTENSION = "rtf";
    private static final String OUTPUT_EXTENSION = "pdf";

    public void runParallelConversions() throws Exception {
        
        // Configure the office manager in a way that maximizes possible race conditions.
        DefaultOfficeManagerBuilder configuration = new DefaultOfficeManagerBuilder();
        configuration.setPortNumbers(new int[]{2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009});
        configuration.setMaxTasksPerProcess(MAX_TASKS_PER_PROCESS);

        OfficeManager officeManager = configuration.build();
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        DocumentFormatRegistry formatRegistry = converter.getFormatRegistry();

        officeManager.start();
        try {
            File inputFile = new File("src/test/resources/documents/test." + INPUT_EXTENSION);

            Thread[] threads = new Thread[MAX_RUNNING_THREADS];

            boolean first = true;
            int t = 0;

            for (int i = 0; i < MAX_CONVERSIONS; i++) {
                DocumentFormat inputFormat = formatRegistry.getFormatByExtension(INPUT_EXTENSION);
                DocumentFormat outputFormat = formatRegistry.getFormatByExtension(OUTPUT_EXTENSION);

                File outputFile = File.createTempFile("test", "." + outputFormat.getExtension());
                outputFile.deleteOnExit();

                // Converts the first document without threads to ensure everything is OK. 
                if (first) {
                    converter.convert(inputFile, outputFile, outputFormat);
                    first = false;
                }

                logger.info("Creating thread {}...", t);
                Runner r = new Runner(inputFile, outputFile, inputFormat, outputFormat, converter);
                threads[t] = new Thread(r);
                threads[t++].start();

                if (t == MAX_RUNNING_THREADS) {
                    for (int j = 0; j < t; j++) {
                        threads[j].join();
                    }
                    t = 0;
                }
            }

            // Wait for remaining threads.
            for (int j = 0; j < t; j++) {
                threads[j].join();
            }

        } finally {
            officeManager.stop();
        }
    }

    public class Runner implements Runnable {

        public Runner(File inputFile, File outputFile, DocumentFormat inputFormat, DocumentFormat outputFormat, OfficeDocumentConverter converter) {
            super();
            this.inputFile = inputFile;
            this.outputFile = outputFile;
            this.inputFormat = inputFormat;
            this.outputFormat = outputFormat;
            this.converter = converter;
        }

        File inputFile, outputFile;

        DocumentFormat inputFormat, outputFormat;

        OfficeDocumentConverter converter;

        public void run() {
            try {
                logger.info("-- converting %s to %s... ", inputFormat.getExtension(), outputFormat.getExtension());
                converter.convert(inputFile, outputFile, outputFormat);
                logger.info("done.\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}