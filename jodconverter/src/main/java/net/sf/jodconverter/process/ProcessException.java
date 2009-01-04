package org.artofsolving.process;

/**
 * Runtime exception thrown by process classes. 
 * @author Sean Greenhow
 *
 */
public class ProcessException extends RuntimeException {

	private static final long serialVersionUID = -6411764423048984826L;

	public ProcessException(String m){
		super(m);
	}
	
	public ProcessException(String m, Exception ex){
		super(m,ex);
	}
}
