package net.sf.jodconverter.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sf.jodconverter.util.ReflectionUtils;
import net.sf.jodconverter.util.UnixProcessUtils;

/**
 * Process service for unix.
 * 
 * @author Sean Greenhow
 *
 */
public class UnixProcessService extends AbstractProcessService {
	
	public UnixProcessService(){
		setCommand("find", new String[] {"/bin/bash", "-c",  "ps -eo \"%p %a\""});
		setCommand("kill", new String[] {"kill", ""});// extra arg for pid
		setCommand("force-kill", new String[] {"kill",UnixProcessUtils.SIGNAL_KILL,""}); // extra arg for pid
	}
	
	public String findPID(String pattern) throws ProcessException {	    
        BufferedReader br = null;
        try{
        	Process getPidProcess = Runtime.getRuntime().exec(getCommand("find"));
            br = new BufferedReader(new InputStreamReader(getPidProcess.getInputStream()));
            String line=null;
            while ( (line = br.readLine()) != null ){           	
                if ( line.matches(pattern)){
                    line = line.trim();
                    return line.substring(0, line.indexOf(" ")).trim();
                }
            }	  
        } catch (IOException ex){
        	ex.printStackTrace();
			throw new ProcessException("Unable to find PID ", ex);
        } finally{
        	if ( br != null ){
            	try {
					br.close();
				} catch (IOException e) {
					throw new ProcessException("Unable to close stream ", e);
				}
            }
        }
        return null;
    }

	public String getPID(Process process) 
	throws ProcessException {
		if ( process == null){
			throw new ProcessException("Unable to retrieve pid from a null process ");
		}
		try {
			return ""+ ReflectionUtils.getPrivateField(process, "pid");
		} catch (RuntimeException ex){
			throw ex;
		} catch (Exception ex){
			throw new ProcessException("Unable to retrieve pid from " + process.getClass().getName(), ex);
		}
	}

	public boolean isPID(String pid) throws ProcessException {
		Process p = null;
		try {
			p = new ProcessBuilder("/bin/bash", "-c","ps -e|grep '^[ ]*" + pid + "'").start();
			InputStreamReader isr = new InputStreamReader(p.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			String s= br.readLine();
			boolean ret= s != null ? s.trim().startsWith(pid) : false;
			while(br.readLine() != null); //swallow any remaining input
			return ret;
		} catch (IOException ex){
			throw new ProcessException("Unable to detect pid ", ex);			
		}
	}

	public void kill(Process process){
		process.destroy();
	}

	public void kill(String pid, boolean force) throws ProcessException {
		try {
			String[] cmd = getCommand(force ? "force-kill" : "kill").clone();
			if ( force ){
				cmd[2] = ""+pid; // 1 is signal
				new ProcessBuilder(cmd).start();
			} else{
				cmd[1] = ""+pid;
				new ProcessBuilder(cmd).start();
			}
		} catch (IOException ioException) {
			throw new ProcessException("Could not execute kill command", ioException);
		}

	}


}
