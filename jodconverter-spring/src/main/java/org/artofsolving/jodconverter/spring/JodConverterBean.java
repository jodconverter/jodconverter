package org.artofsolving.jodconverter.spring;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.OfficeDocumentUtils;
import org.artofsolving.jodconverter.TextReplaceOfficeTask;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.sun.star.lang.XComponent;

/**
 * The purpose of this class is to provide to the Spring Container a Bean that encapsulates the
 * functionality already present in the JODConverter-CORE library. The target of this bean is to
 * provide the functionality of the PocessPoolOfficeManager.
 * 
 * The Controller shall launch the OO processes. The Controller shall stop the OO processes when
 * it´s time to shutdown the application
 * 
 * Also, The controller shall allow the user to open a document. Then, the user shall be able to apply a number of
 * document tasks over it, such as TextReplace, etc, without the need to open and close the document each time.
 *  
 * @author Jose Luis López López
 */
public class JodConverterBean implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(JodConverterBean.class);

    //These are the variables that are used for the DefaultConversionTask
    private String officeHome;
    private String portNumbers;
    private String workingDir;
    private String templateProfileDir;
    private Long retryTimeout;
    private Long retryInterval;
    private Boolean killExistingProcess;
    private Long taskQueueTimeout;
    private Long taskExecutionTimeout;
    private Integer maxTasksPerProcess;

    private OfficeManager officeManager = null;
    private OfficeDocumentConverter documentConverter = null;
    
    //These are the variables that are used to execute DocumentTasks. If the document is null, then the tasks will 
    //fail with OfficeException.
    private XComponent document=null;

    @Override
    public void afterPropertiesSet() throws Exception {

        DefaultOfficeManagerBuilder builder = new DefaultOfficeManagerBuilder();

        if (!StringUtils.isBlank(officeHome)) {
            builder.setOfficeHome(officeHome);
        }

        if (!StringUtils.isBlank(workingDir)) {
            builder.setWorkingDir(new File(workingDir));
        }

        if (!StringUtils.isBlank(portNumbers)) {
            Set<Integer> ports = buildPortNumbers(this.portNumbers);
            if (!ports.isEmpty()) {
                builder.setPortNumbers(ArrayUtils.toPrimitive(ports.toArray(new Integer[]{})));
            }
        }

        if (!StringUtils.isBlank(templateProfileDir)) {
            builder.setTemplateProfileDir(new File(templateProfileDir));
        }

        if (retryTimeout != null) {
            builder.setRetryTimeout(retryTimeout);
        }

        if (retryInterval != null) {
            builder.setRetryInterval(retryInterval);
        }

        if (killExistingProcess != null) {
            builder.setKillExistingProcess(killExistingProcess);
        }

        if (taskQueueTimeout != null) {
            builder.setTaskQueueTimeout(taskQueueTimeout);
        }

        if (taskExecutionTimeout != null) {
            builder.setTaskExecutionTimeout(taskExecutionTimeout);
        }

        if (maxTasksPerProcess != null) {
            builder.setMaxTasksPerProcess(maxTasksPerProcess);
        }

        // Starts the manager
        officeManager = builder.build();
        documentConverter = new OfficeDocumentConverter(officeManager);
        officeManager.start();
        
        
    }

    /**
     * Prints the available formats provided by the JODConverter module.
     */
    public void availableFormats() {

        DefaultDocumentFormatRegistry ref = DefaultDocumentFormatRegistry.getInstance();
        Set<DocumentFormat> group = ref.getOutputFormats(DocumentFamily.TEXT);
        Iterator<DocumentFormat> it = group.iterator();
        logger.info("Supported Text Document Formats are:");
        while (it.hasNext()) {
            DocumentFormat df = it.next();
            logger.info(df.getName());
        }

        group = ref.getOutputFormats(DocumentFamily.SPREADSHEET);
        it = group.iterator();
        logger.info("Supported SpreadSheet Document Formats are:");
        while (it.hasNext()) {
            DocumentFormat df = it.next();
            logger.info(df.getName());
        }

        group = ref.getOutputFormats(DocumentFamily.PRESENTATION);
        it = group.iterator();
        logger.info("Supported Presentation Document Formats are:");
        while (it.hasNext()) {
            DocumentFormat df = it.next();
            logger.info(df.getName());
        }

        group = ref.getOutputFormats(DocumentFamily.DRAWING);
        it = group.iterator();
        logger.info("Supported Drawing Document Formats are:");
        while (it.hasNext()) {
            DocumentFormat df = it.next();
            logger.info(df.getName());
        }
    }

    // Create a set of port numbers from a string
    private Set<Integer> buildPortNumbers(String str) {

        Set<Integer> iports = new HashSet<Integer>();

        if (StringUtils.isBlank(str)) {
            return iports;
        }

        String[] portNumbers = StringUtils.split(str, ", ");
        if (portNumbers.length == 0) {
            return iports;
        }

        for (String portNumber : portNumbers) {
            if (!StringUtils.isBlank(portNumber)) {
                iports.add(Integer.parseInt(StringUtils.trim(portNumber)));
            }
        }
        return iports;
    }

    /**
     * Converts an input file into a file of another format. The file extensions are taken to know
     * the conversion formats.
     * 
     * @param inputFile
     *            The file whose content is to be converted.
     * @param outputFile
     *            The output file (target) of the conversion.
     * @throws Exception
     *             Thrown if an error occurs while converting the file.
     */
    public void convert(String inputFile, String outputFile) throws Exception {

        convert(new File(inputFile), new File(outputFile));
    }

    /**
     * Converts an input file into a file of another format. The file extensions are taken to know
     * the conversion formats.
     * 
     * @param inputFile
     *            The file whose content is to be converted.
     * @param outputFile
     *            The output file (target) of the conversion.
     * @throws Exception
     *             Thrown if an error occurs while converting the file.
     */
    public void convert(File inputFile, File outputFile) throws Exception {

        documentConverter.convert(inputFile, outputFile);
    }

    @Override
    public void destroy() throws Exception {

        if (officeManager != null) {
            officeManager.stop();
        }
    }

    /**
     * Gets whether we must kill existing office process when an office process already exists for
     * the same connection string. If not set, it defaults to true.
     * 
     * @return {@code true} to kill existing process, {@code false} otherwise.
     */
    public Boolean getKillExistingProcess() {
        return killExistingProcess;
    }

    /**
     * Gets the maximum number of tasks an office process can execute before restarting. Default is
     * 200.
     * 
     * @return the maximum value.
     */
    public Integer getMaxTasksPerProcess() {
        return maxTasksPerProcess;
    }

    /**
     * Gets the office home directory.
     * 
     * @return the office home directory.
     */
    public String getOfficeHome() {
        return officeHome;
    }

    /**
     * Gets the list of ports, separated by commas, used by each JODConverter processing thread. The
     * number of office instances is equal to the number of ports, since 1 office will be launched
     * for each port number.
     * 
     * @return the port numbers to use.
     */
    public String getPortNumbers() {
        return portNumbers;
    }

    /**
     * Get the retry interval (milliseconds).Used for waiting between office process call tries
     * (start/terminate). Default is 250.
     * 
     * @return the retry interval, in milliseconds.
     */
    public Long getRetryInterval() {
        return retryInterval;
    }

    /**
     * Set the retry timeout (milliseconds).Used for retrying office process calls
     * (start/terminate). If not set, it defaults to 2 minutes.
     * 
     * @return the retry timeout, in milliseconds.
     */
    public Long getRetryTimeout() {
        return retryTimeout;
    }

    /**
     * Gets the maximum time allowed to process a task. If the processing time of a task is longer
     * than this timeout, this task will be aborted and the next task is processed. Default is
     * 120000 (2 minutes).
     * 
     * @return the timeout value.
     */
    public Long getTaskExecutionTimeout() {
        return taskExecutionTimeout;
    }

    /**
     * Gets the maximum living time of a task in the conversion queue. The task will be removed from
     * the queue if the waiting time is longer than this timeout. Default is 30000 (30 seconds).
     * 
     * @return timeout value.
     */
    public Long getTaskQueueTimeout() {
        return taskQueueTimeout;
    }

    /**
     * Gets the directory to copy to the temporary office profile directories to be created.
     * 
     * @return the template profile directory.
     */
    public String getTemplateProfileDir() {
        return templateProfileDir;
    }

    /**
     * Gets the directory where temporary office profiles will be created.
     * <p>
     * Defaults to the system temporary directory as specified by the <code>java.io.tmpdir</code>
     * system property.
     * 
     * @return the working directory.
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Gets whether we must kill existing office process when an office process already exists for
     * the same connection string. If not set, it defaults to true.
     * 
     * @return {@code true} to kill existing process, {@code false} otherwise.
     */
    public boolean isKillExistingProcess() {
        return killExistingProcess;
    }

    /**
     * Sets whether we must kill existing office process when an office process already exists for
     * the same connection string.
     * 
     * @param killExistingProcess
     *            {@code true} to kill existing process, {@code false} otherwise.
     */
    public void setKillExistingProcess(Boolean killExistingProcess) {
        this.killExistingProcess = killExistingProcess;
    }

    /**
     * Sets the maximum number of tasks an office process can execute before restarting.
     * 
     * @param maxTasksPerProcess
     *            the new value to set.
     */
    public void setMaxTasksPerProcess(Integer maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
    }

    /**
     * Sets the office home directory.
     * 
     * @param officeHome
     *            the new home directory to set.
     */
    public void setOfficeHome(String officeHome) {
        this.officeHome = officeHome;
    }

    /**
     * Sets the list of ports, separated by commas, used by each JODConverter processing thread. The
     * number of office instances is equal to the number of ports, since 1 office will be launched
     * for each port number.
     * 
     * @param portNumbers
     *            the port numbers to use.
     */
    public void setPortNumbers(String portNumbers) {
        this.portNumbers = portNumbers;
    }

    /**
     * Set the retry interval (milliseconds).Used for waiting between office process call tries
     * (start/terminate).
     * 
     * @param retryInterval
     *            the retry interval, in milliseconds.
     */
    public void setRetryInterval(Long retryInterval) {
        this.retryInterval = retryInterval;
    }

    /**
     * Set the retry timeout (milliseconds). Used for retrying office process calls
     * (start/terminate).
     * 
     * @param retryTimeout
     *            the retry timeout, in milliseconds.
     */
    public void setRetryTimeout(Long retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    /**
     * Sets the maximum time allowed to process a task. If the processing time of a task is longer
     * than this timeout, this task will be aborted and the next task is processed. Default is
     * 120000 (2 minutes).
     * 
     * @param taskExecutionTimeout
     *            the new timeout value.
     */
    public void setTaskExecutionTimeout(Long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

    /**
     * Sets the maximum living time of a task in the conversion queue. The task will be removed from
     * the queue if the waiting time is longer than this timeout.
     * 
     * @param taskQueueTimeout
     *            the new timeout value.
     */
    public void setTaskQueueTimeout(Long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

    /**
     * Sets the directory to copy to the temporary office profile directories to be created.
     * 
     * @param templateProfileDir
     *            the new template profile directory.
     */
    public void setTemplateProfileDir(String templateProfileDir) {
        this.templateProfileDir = templateProfileDir;
    }

    /**
     * Sets the directory where temporary office profiles will be created.
     * 
     * @param workingDir
     *            the new working directory.
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }
    
    
    /**
     * Opens a document and store a reference to it, so that subsequent calls to Document Tasks will be performed
     * over this document, until the close operation is invoked or an error occurs.
     * 
     * @param inputFile
     *            The path to the source file that will be opened and stored internally.

     * @throws OfficeException
     *             Thrown if an error occurs while performing the text replace operation or the document is null
     */
    public void loadDocument(String inputFile) throws OfficeException {
    	if(document==null) {
    		document=OfficeDocumentUtils.loadDocument(inputFile, this.officeManager.getContext());
    	}else {
    		//TODO: Close current document and apply changes or not depending on some parameter.
    		document.dispose();
    		document=null;
    		document=OfficeDocumentUtils.loadDocument(inputFile, this.officeManager.getContext());
    	}
    }
    
    
    
    /**
     * Requests the controller to perform the Document Task of "TextReplace"
     * 
     * @param document
     *            The previously opened document. You should call the operation "loadDocument(String inputFile) before
     *            requesting any of the Document Tasks.
     * @param mark
     *            The String to search, that will be replaced
     * @param replacement
     * 			  The String to replace the mark with
     * @throws OfficeException
     *             Thrown if an error occurs while performing the text replace operation or the document is null
     */
    public void searchAndReplace(String mark, String replacement) throws OfficeException {
    	if(document==null) {
    		throw new OfficeException("There is no document to apply the Document Task. Please load a document.");
    	}

    	TextReplaceOfficeTask task=new TextReplaceOfficeTask(document, mark, replacement);
		officeManager.execute(task);
    }
}
