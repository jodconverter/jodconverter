package org.artofsolving.jodconverter.spring;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringControllerTest {

    @Configuration("SpringControllerTestConfiguration")
    static class ContextConfiguration {

        // this bean will be injected into the SpringControllerTest class
        @Bean
        public JodConverterBean springJoDConverter() {
            JodConverterBean sjd = new JodConverterBean();
            // set properties, etc.
            return sjd;
        }
    }


    private File inputFileTXT = null;
    private File outputFileRTF = null;
    private File outputFileDOC = null;
    private File outputFilePDF = null;
    private File outputFileDOCX = null;

    @Autowired
    private JodConverterBean bean = null;

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

        bean.convert(inputFileTXT, outputFileRTF);
        assertTrue("RTF File not created.", outputFileRTF.exists());
    }

    @Test
    public void testTXTToDOC() throws Exception {

        bean.convert(inputFileTXT, outputFileDOC);
        assertTrue("DOC File not created.", outputFileDOC.exists());
    }

    @Test
    public void testTXTToDOCX() throws Exception {

        bean.convert(inputFileTXT, outputFileDOCX);
        assertTrue("DOCX File not created.", outputFileDOCX.exists());
    }

    @Test
    public void testTXTToPDF() throws Exception {

        bean.convert(inputFileTXT, outputFilePDF);
        assertTrue("PDF File not created.", outputFilePDF.exists());
    }
}
