package org.artofsolving.jodconverter.sample.web;

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

public class WebappContext {

    public static final String PARAMETER_OFFICE_PORT = "office.port";
	public static final String PARAMETER_OFFICE_HOME = "office.home";
	public static final String PARAMETER_OFFICE_PROFILE = "office.profile";
	public static final String PARAMETER_FILEUPLOAD_FILE_SIZE_MAX = "fileupload.fileSizeMax";

	private final Logger logger = Logger.getLogger(getClass().getName());

	private static final String KEY = WebappContext.class.getName();

	private final ServletFileUpload fileUpload;

	private final OfficeManager officeManager;
	private final OfficeDocumentConverter documentConverter;

	public WebappContext(ServletContext servletContext) {
		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
		String fileSizeMax = servletContext.getInitParameter(PARAMETER_FILEUPLOAD_FILE_SIZE_MAX);
		fileUpload = new ServletFileUpload(fileItemFactory);
		if (fileSizeMax != null) {
			fileUpload.setFileSizeMax(Integer.parseInt(fileSizeMax));
			logger.info("max file upload size set to " + fileSizeMax);
		} else {
			logger.warning("max file upload size not set");
		}

		DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
		String officePortParam = servletContext.getInitParameter(PARAMETER_OFFICE_PORT);
		if (officePortParam != null) {
		    configuration.setPortNumber(Integer.parseInt(officePortParam));
		}
		String officeHomeParam = servletContext.getInitParameter(PARAMETER_OFFICE_HOME);
		if (officeHomeParam != null) {
		    configuration.setOfficeHome(new File(officeHomeParam));
		}
		String officeProfileParam = servletContext.getInitParameter(PARAMETER_OFFICE_PROFILE);
		if (officeProfileParam != null) {
		    configuration.setTemplateProfileDir(new File(officeProfileParam));
		}

		officeManager = configuration.buildOfficeManager();
		documentConverter = new OfficeDocumentConverter(officeManager);
	}

	protected static void init(ServletContext servletContext) {
		WebappContext instance = new WebappContext(servletContext);
		servletContext.setAttribute(KEY, instance);
		instance.officeManager.start();
	}

	protected static void destroy(ServletContext servletContext) {
		WebappContext instance = get(servletContext);
		instance.officeManager.stop();
	}

	public static WebappContext get(ServletContext servletContext) {
		return (WebappContext) servletContext.getAttribute(KEY);
	}

	public ServletFileUpload getFileUpload() {
		return fileUpload;
	}

	public OfficeManager getOfficeManager() {
        return officeManager;
    }

	public OfficeDocumentConverter getDocumentConverter() {
        return documentConverter;
    }

}
