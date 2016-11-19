package org.artofsolving.jodconverter.spring;

import java.io.File;
import java.io.PrintWriter;

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

        // this bean will be injected into the OrderServiceTest class
        @Bean
        public SpringJodConverter springJoDConverter() {
            SpringJodConverter sjd = new SpringJodConverter();
            // set properties, etc.
            return sjd;
        }
    }

    File TXTinputFile = null;
    File RTFoutputFile = null;
    File DOCoutputFile = null;
    File PDFoutputFile = null;
    File DOCXoutputFile = null;

    @Autowired
    private SpringJodConverter testClass = null;

    @Before
    public void setUp() throws Exception {
        TXTinputFile = new File("input.txt");
        if (!TXTinputFile.exists()) {
            boolean b = TXTinputFile.createNewFile();
            if (!b)
                throw new Exception("Cannot create the input file.");
        } else {
            PrintWriter pw = new PrintWriter(TXTinputFile);
            pw.println("This is the first line of the input file.");
            pw.println("This is the second line of the input file.");
            pw.close();
        }
        RTFoutputFile = new File("output.rtf");
        DOCoutputFile = new File("output.doc");
        PDFoutputFile = new File("output.pdf");
        DOCXoutputFile = new File("output.DOCX");
    }

    @After
    public void tearDown() throws Exception {
        TXTinputFile.delete();
        RTFoutputFile.delete();
        DOCoutputFile.delete();
        PDFoutputFile.delete();
        DOCXoutputFile.delete();
    }

    @Test
    public void testAvailableFormats() throws Exception {
        testClass.availableFormats();
    }

    @Test
    public void testTXTToRTF() throws Exception {
        testClass.convert(TXTinputFile.toString(), RTFoutputFile.toString());
        if (!RTFoutputFile.exists())
            throw new Exception("There is not a RTF File!.");
    }

    @Test
    public void testTXTToDOC() throws Exception {
        testClass.convert(TXTinputFile.toString(), DOCoutputFile.toString());
        if (!DOCoutputFile.exists())
            throw new Exception("There is not a DOC File!.");
    }

    @Test
    public void testTXTToDOCX() throws Exception {
        testClass.convert(TXTinputFile.toString(), DOCXoutputFile.toString());
        if (!DOCXoutputFile.exists())
            throw new Exception("There is not a DOCX File!.");
    }

    @Test
    public void testTXTToPDF() throws Exception {
        testClass.convert(TXTinputFile.toString(), PDFoutputFile.toString());
        if (!PDFoutputFile.exists())
            throw new Exception("There is not a PDF File!.");
    }

}
