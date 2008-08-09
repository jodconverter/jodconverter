package org.artofsolving.jodconverter;

import static org.testng.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.office.ManagedProcessOfficeManager;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.artofsolving.jodconverter.test.TestUtils;
import org.testng.annotations.Test;

@Test(groups="functional")
public class OfficeDocumentConverterFunctionalTest {

    private static final File OFFICE_HOME = TestUtils.getOfficeHome();
    private static final File OFFICE_PROFILE = TestUtils.getOfficeProfile();
    private static final String CONNECT_STRING = "socket,host=127.0.0.1,port=8100";

    public void runAllPossibleConversions() throws IOException {
        OfficeManager officeManager = new ManagedProcessOfficeManager(OFFICE_HOME, OFFICE_PROFILE, CONNECT_STRING);
        OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        DocumentFormatRegistry formatRegistry = converter.getFormatRegistry();
        
        officeManager.start();
        try {
            File dir = new File("src/test/resources/documents");
            File[] files = dir.listFiles(new FilenameFilter() {
            	public boolean accept(File dir, String name) {
            		return !name.startsWith(".");
            	}
            });
			for (File inputFile : files) {
                String inputExtension = FilenameUtils.getExtension(inputFile.getName());
                DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
                assertNotNull(inputFormat, "unknown input format: " + inputExtension);
                Set<DocumentFormat> outputFormats = formatRegistry.getOutputFormats(inputFormat.getInputFamily());
                for (DocumentFormat outputFormat : outputFormats) {
                    File outputFile = File.createTempFile("test", "." + outputFormat.getExtension());
                    outputFile.deleteOnExit();
                    System.out.printf("-- converting %s to %s... ", inputFormat.getExtension(), outputFormat.getExtension());
                    converter.convert(inputFile, outputFile, outputFormat);
                    System.out.printf("done.\n");
                    assertTrue(outputFile.isFile() && outputFile.length() > 0);
                    //TODO use file detection to make sure outputFile is in the expected format
                }
            }
        } finally {
            officeManager.stop();
        }
    }

}
