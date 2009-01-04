package org.artofsolving.process;

/**
 * Use this when an old windows version is used and no access to third party executables.
 * Basically this will do very little! 
 * 
 * @author Sean Greenhow
 *
 */
public class BasicWindowsProcessService extends AbstractProcessService{
	

	public String findPID(String pattern) throws ProcessException{
		throw new UnsupportedOperationException("Cannot find PID ");
	}

	public String getPID(Process process) 
	throws ProcessException {
		throw new UnsupportedOperationException("Obtaining PID from windows Process is not supported");
	}
	
	public boolean isPID(String pid) throws ProcessException{
		throw new UnsupportedOperationException("Cannot test if valid PID");
	}

	public void kill(Process process){
		process.destroy();
	}
	
	public void kill(String pid, boolean force) throws ProcessException{
		throw new UnsupportedOperationException("Cannot kill process");
	}

}
