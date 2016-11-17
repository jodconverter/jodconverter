package org.artofsolving.jodconverter.spring;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/*
 * 17/11/2016 Jose Luis López López
 * The purpose of this component is to provide to the Spring Container a Bean that encapsulates
 * the functionality already present in the JODConverter-CORE library. The target of this component is
 * to provide the functionality of the PocessPoolOfficeManager.
 * 
 * The Controller shall launch the OO processes.
 * The Controller shall stop the OO processes when it´s time to shutdown the application
 * 
 */

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SpringJodConverter implements InitializingBean, DisposableBean{

	private static final String DEFAULT_OFFICE_PORT = null;
	private static final String DEFAULT_OFFICE_HOME = null;
	private static final String DEFAULT_OFFICE_PROFILE = null;
	
	private OfficeManager officeManager=null;
	private OfficeDocumentConverter documentConverter=null;
	
	public SpringJodConverter()
	{
		DefaultOfficeManagerBuilder configuration = new DefaultOfficeManagerBuilder();
		String officePortParam = DEFAULT_OFFICE_PORT;
		if (officePortParam != null) {
		    configuration.setPortNumber(Integer.parseInt(officePortParam));
		}
		String officeHomeParam = DEFAULT_OFFICE_HOME;
		if (officeHomeParam != null) {
		    configuration.setOfficeHome(new File(officeHomeParam));
		}
		String officeProfileParam = DEFAULT_OFFICE_PROFILE;
		if (officeProfileParam != null) {
		    configuration.setTemplateProfileDir(new File(officeProfileParam));
		}
		officeManager = configuration.build();
		documentConverter = new OfficeDocumentConverter(officeManager);
	}

	@Override
	public void destroy() throws Exception {
		officeManager.stop();
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		officeManager.start();
	}
	public void convert(String inputFile,String outputFile) throws Exception {
		String outputExtension=FilenameUtils.getExtension(outputFile);
		String inputExtension=FilenameUtils.getExtension(inputFile);
		
        DocumentFormat outputFormat = documentConverter.getFormatRegistry().getFormatByExtension(outputExtension);
    	long startTime = System.currentTimeMillis();
    	documentConverter.convert(new File(inputFile), new File(outputFile));
    	long conversionTime = System.currentTimeMillis() - startTime;
	}
}
