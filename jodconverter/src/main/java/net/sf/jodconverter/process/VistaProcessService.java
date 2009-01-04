package net.sf.jodconverter.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


/**
 * Windows specific ProcessService that relies on the tasklist/taskkill and wmic window commands.
 * These should work for Server 2003/2008, Vista and XP Pro.
 * @author Sean Greenhow
 *
 */
public class VistaProcessService extends BasicWindowsProcessService{
	
	public VistaProcessService(){
		setCommand("find",new String[] {"wmic.exe"});
	}
	
	public String findPID(String pattern) throws ProcessException {       
        BufferedReader br = null;
        try{
        	Process getPidProcess = Runtime.getRuntime().exec(getCommand("find"));
            // must close oStream see http://forums.sun.com/thread.jspa?messageID=4424722
            OutputStreamWriter oStream = new OutputStreamWriter(getPidProcess.getOutputStream());
            oStream.write("process get CommandLine,ProcessId");            
            oStream.flush();
            oStream.close(); 
            br = new BufferedReader(new InputStreamReader(getPidProcess.getInputStream()));
            String line=null;
            while ( (line = br.readLine()) != null ){
            	if ( line.matches(pattern)){
                    line = line.trim();
                    line.substring(line.lastIndexOf(" ")).trim();
                }
            }
        } catch (IOException ex){
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
		
	public boolean isPID(String pid) throws ProcessException {
		Process p = null;
		try {
			p = new ProcessBuilder("tasklist.exe",  "/FO", "LIST").start();
			InputStreamReader isr = new InputStreamReader(p.getInputStream());
	        BufferedReader br = new BufferedReader(isr);
	        String line = null;
	        boolean found = false;
	        String prefix = "PID:";
	        while( (line = br.readLine()) != null ){
	        	if(found){ // swallow any remaining output
	        		continue;
	        	}
	            if ( line.startsWith(prefix)){
	                if ( pid.equals(line.substring(prefix.length()).trim())){
	                    found = true;
	                    break;
	                }
	            }
	        }
	        return found;
		} catch (IOException ex){
			throw new ProcessException("Unable to detect pid ", ex);			
		}
	}
	
	public void kill(String pid, boolean force) throws ProcessException {
		try {
			if ( force ){
				new ProcessBuilder("taskkill.exe", "/T", "/F", "/PID", pid).start();
			} else{
				new ProcessBuilder("taskkill.exe", "/T", "/PID", pid).start();
			}
		} catch (IOException ioException) {
			throw new ProcessException("Could not execute kill command", ioException);
		}

	}
	

}
