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
	
	File TXTinputFile=null;
	File RTFoutputFile=null;
	File DOCoutputFile=null;
	
	@Autowired
	private SpringJodConverter testClass=null;
	
	@Before
	public void setUp() throws Exception {
		TXTinputFile=new File("input.txt");
		if(!TXTinputFile.exists()) {
			boolean b = TXTinputFile.createNewFile();
			if(!b) throw new Exception ("Cannot create the input file.");
		}else {
			PrintWriter pw = new PrintWriter(TXTinputFile);
			pw.println("This is the first line of the input file.");
			pw.println("This is the second line of the input file.");
			pw.close();
		}
		RTFoutputFile=new File("output.rtf");
		DOCoutputFile=new File("output.doc");
	}

	@After
	public void tearDown() throws Exception {
		TXTinputFile.delete();
		RTFoutputFile.delete();
		DOCoutputFile.delete();
	}

	@Test
	public void testTXTToRTF() throws Exception {
		testClass.convert(TXTinputFile.toString(),RTFoutputFile.toString());
		if(!RTFoutputFile.exists()) throw new Exception("There is not RTF File!.");
	}
	
	@Test
	public void testTXTToDOC() throws Exception {
		testClass.convert(TXTinputFile.toString(),DOCoutputFile.toString());
		if(!DOCoutputFile.exists()) throw new Exception("There is not DOC File!.");
	}

}
