package net.sf.jodconverter.sample.web;

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import net.sf.jodconverter.OfficeDocumentConverter;
import net.sf.jodconverter.office.ManagedProcessOfficeManager;
import net.sf.jodconverter.office.OfficeManager;
import net.sf.jodconverter.office.OfficeUtils;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class WebappContext {

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

		String officeHomeParam = servletContext.getInitParameter(PARAMETER_OFFICE_HOME);
		File officeHome = officeHomeParam != null ? new File(officeHomeParam) : OfficeUtils.getDefaultOfficeHome();
		String officeProfileParam = servletContext.getInitParameter(PARAMETER_OFFICE_PROFILE);
		File officeProfile = officeProfileParam != null ? new File(officeProfileParam) : OfficeUtils.getDefaultProfileDir();
		
		ManagedProcessOfficeManager officeManager = new ManagedProcessOfficeManager(officeHome, officeProfile);
		officeManager.setMaxTasksPerProcess(50);
		officeManager.setTaskExecutionTimeout(30000L);
		this.officeManager = officeManager;
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
