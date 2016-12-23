package org.artofsolving.jodconverter.office;

import static org.junit.Assert.*;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.artofsolving.jodconverter.AbstractOfficeTask;
import org.artofsolving.jodconverter.OfficeDocumentUtils;
import org.artofsolving.jodconverter.SaveAndCloseOfficeTask;
import org.artofsolving.jodconverter.TextReplaceOfficeTask;
import org.artofsolving.jodconverter.office.ExternalOfficeManager;
import org.artofsolving.jodconverter.office.OfficeProcess;
import org.artofsolving.jodconverter.office.OfficeUtils;
import org.artofsolving.jodconverter.office.UnoUrlUtils;
import org.artofsolving.jodconverter.process.PureJavaProcessManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.helper.UnoUrl;

public class TestSaveAndCloseTask {
	OfficeProcess officeProcess=null;
	UnoUrl unoUrl = null;
	ExternalOfficeManager manager=null;
	AbstractOfficeTask task=null; 
	
	String source="C:\\tmp\\source.odt";
	
	@Before
	public void setUp() throws Exception {
        unoUrl = UnoUrlUtils.socket(2002);
        officeProcess = new OfficeProcess(OfficeUtils.getDefaultOfficeHome(), unoUrl, null, null, new File(System.getProperty("java.io.tmpdir")), new PureJavaProcessManager(), true);
        officeProcess.start();
        Thread.sleep(2000);
        Integer exitCode = officeProcess.getExitCode();
        if (exitCode != null && exitCode.equals(Integer.valueOf(81))) {
            officeProcess.start(true);
            Thread.sleep(2000);
        }

        manager = new ExternalOfficeManager(unoUrl, true);
        manager.start();   
        
	}

	@After
	public void tearDown() throws Exception {
        manager.stop();
        Process process = (Process) FieldUtils.readDeclaredField(officeProcess, "process", true);
        process.destroy();
	}

	@Test
	public void testExecute() throws Exception{
		
		OfficeContext context=manager.getContext();
		XComponent document = OfficeDocumentUtils.loadDocument(source, context);		
		task=new TextReplaceOfficeTask(document, "{$MARK}", "REPLACED TEXT");
		manager.execute(task);
		assertTrue(task.isCompleted());
		task=null;
		task=new SaveAndCloseOfficeTask(document);
		manager.execute(task);
		assertTrue(task.isCompleted());

		Thread.sleep(2000);
        


	}

}
