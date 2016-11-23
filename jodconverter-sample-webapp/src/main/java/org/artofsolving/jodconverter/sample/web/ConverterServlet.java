package org.artofsolving.jodconverter.sample.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConverterServlet extends HttpServlet {
    private static final long serialVersionUID = -591469426224201748L;
    
    private static final Logger logger = LoggerFactory.getLogger(ConverterServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(request)) {
        	response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only multipart requests are allowed");
        	return;
        }

		WebappContext webappContext = WebappContext.get(getServletContext());
		ServletFileUpload fileUpload = webappContext.getFileUpload();
		OfficeDocumentConverter converter = webappContext.getDocumentConverter();

		String outputExtension = FilenameUtils.getExtension(request.getRequestURI());
		        
		FileItem uploadedFile;
		try {
			uploadedFile = getUploadedFile(fileUpload, request);
		} catch (FileUploadException fileUploadException) {
		    throw new ServletException(fileUploadException);
		}
		if (uploadedFile == null) {
			throw new NullPointerException("Uploaded file is null");
		}
        String inputExtension = FilenameUtils.getExtension(uploadedFile.getName());

        String baseName = FilenameUtils.getBaseName(uploadedFile.getName());
        File inputFile = File.createTempFile(baseName, "." + inputExtension);
        writeUploadedFile(uploadedFile, inputFile);
        File outputFile = File.createTempFile(baseName, "." + outputExtension);
        try {
            DocumentFormat outputFormat = converter.getFormatRegistry().getFormatByExtension(outputExtension);
        	long startTime = System.currentTimeMillis();
        	converter.convert(inputFile, outputFile);
        	long conversionTime = System.currentTimeMillis() - startTime;
        	logger.info(String.format("Successful conversion: %s [%db] to %s in %dms", inputExtension, inputFile.length(), outputExtension, conversionTime));
        	response.setContentType(outputFormat.getMediaType());
            response.setHeader("Content-Disposition", "attachment; filename="+ baseName + "." + outputExtension);
            sendFile(outputFile, response);
        } catch (Exception exception) {
            logger.error(String.format("Failed conversion: %s [%db] to %s; %s; input file: %s", inputExtension, inputFile.length(), outputExtension, exception, inputFile.getName()));
        	throw new ServletException("Conversion failed", exception);
        } finally {
            FileUtils.deleteQuietly(outputFile);
            FileUtils.deleteQuietly(inputFile);
        }
	}

	private void sendFile(File file, HttpServletResponse response) throws IOException {
		response.setContentLength((int) file.length());
        InputStream inputStream = null;
        try {
        	inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, response.getOutputStream());
        } finally {
        	IOUtils.closeQuietly(inputStream);
        }
	}

	private void writeUploadedFile(FileItem uploadedFile, File destinationFile) throws ServletException {
        try {
			uploadedFile.write(destinationFile);
		} catch (Exception exception) {
			throw new ServletException("Error writing uploaded file", exception);
		}
		uploadedFile.delete();
	}

	private FileItem getUploadedFile(ServletFileUpload fileUpload, HttpServletRequest request) throws FileUploadException {
	    
		List<FileItem> fileItems = fileUpload.parseRequest(request);
		for (FileItem fileItem : fileItems) {
			if (!fileItem.isFormField()) {
				return fileItem;
			}
		}
		return null;
	}

}
