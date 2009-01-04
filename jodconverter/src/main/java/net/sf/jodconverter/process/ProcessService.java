package net.sf.jodconverter.process;

/**
 * Abstraction to allows Java to deal with native processes via the a Process Identifier or 'PID'. A
 * String is used to represent such a value so that the interface can be platform neutral. 
 * Not all platforms can support all method, as a result, UnsupportedOperationException
 * can be thrown by some methods. A method might fail with ProcessException.This could be 
 * again due to a support issue. For instance not all window versions support the 
 * tasklist/taskkill commands used by the VistaProcessService.
 * 
 * @author Sean Greenhow
 *
 */
public interface ProcessService {
	
	/**
	 * Attempts to search for the PID of the process matching the pattern. For instance
	 * to find an Open Office instance running on the port 8100 the following 
	 * pattern could be provided <i>".*soffice.*port=8100.*"</i>.
	 * @param pattern
	 * @return the PID, or null
	 * @throws ProcessException
	 * @exception UnsupportedOperationException If service does not support call
	 */
	public String findPID(String pattern) throws ProcessException;

	/**
	 * Return the pid for the supplied Process instance. 
	 * 
	 * @param process The process to return a PID for
	 * @return The PID
	 * @exception ProcessException If call failed
	 * @exception UnsupportedOperationException If service does not support call
	 */
	public String getPID(Process process) 
	throws ProcessException;
	
	/**
	 * Tests to see if the supplied value is a PID.
	 * @param pid Value to test
	 * @return true if a process is running with the the PID
	 * @exception ProcessException If call failed
	 * @exception UnsupportedOperationException If service does not support call
	 */
	public boolean isPID(String pid)
	throws ProcessException;
	
	/**
	 * Kill the running process.
	 * @param pid The process to kill
	 * @param force Use force (such as signal -9)
	 * @exception ProcessException If call failed
	 * @exception UnsupportedOperationException If service does not support call
	 */
	public void kill(String pid, boolean force)
	throws ProcessException;
	
}
