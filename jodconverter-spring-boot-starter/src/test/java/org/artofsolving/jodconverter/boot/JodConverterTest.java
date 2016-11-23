package org.artofsolving.jodconverter.boot;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JodConverterTest {

    private File inputFileTXT = null;
    private File outputFileRTF = null;
    private File outputFileDOC = null;
    private File outputFilePDF = null;
    private File outputFileDOCX = null;

    @Autowired
    private OfficeDocumentConverter converter = null;

    @Before
    public void setUp() throws Exception {

        inputFileTXT = File.createTempFile("JodConverterTest", ".txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(inputFileTXT);
            pw.println("This is the first line of the input file.");
            pw.println("This is the second line of the input file.");
        } catch (Exception e) {
            IOUtils.closeQuietly(pw);
        }

        File parent = inputFileTXT.getParentFile();
        String basename = FilenameUtils.getBaseName(inputFileTXT.getName());
        outputFileRTF = new File(parent, basename + ".rtf");
        outputFileDOC = new File(parent, basename + ".doc");
        outputFilePDF = new File(parent, basename + ".pdf");
        outputFileDOCX = new File(parent, basename + ".docx");
    }

    @After
    public void tearDown() throws Exception {

        FileUtils.deleteQuietly(inputFileTXT);
        FileUtils.deleteQuietly(outputFileRTF);
        FileUtils.deleteQuietly(outputFileDOC);
        FileUtils.deleteQuietly(outputFilePDF);
        FileUtils.deleteQuietly(outputFileDOCX);
    }

    @Test
    public void testTXTToRTF() throws Exception {

        converter.convert(inputFileTXT, outputFileRTF);
        assertTrue("RTF File not created.", outputFileRTF.exists());
    }

    @Test
    public void testTXTToDOC() throws Exception {

        converter.convert(inputFileTXT, outputFileDOC);
        assertTrue("DOC File not created.", outputFileDOC.exists());
    }

    @Test
    public void testTXTToDOCX() throws Exception {

        converter.convert(inputFileTXT, outputFileDOCX);
        assertTrue("DOCX File not created.", outputFileDOCX.exists());
    }

    @Test
    public void testTXTToPDF() throws Exception {

        converter.convert(inputFileTXT, outputFilePDF);
        assertTrue("PDF File not created.", outputFilePDF.exists());
    }

}
