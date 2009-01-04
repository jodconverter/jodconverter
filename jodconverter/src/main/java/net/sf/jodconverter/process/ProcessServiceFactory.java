package net.sf.jodconverter.process;


/**
 * Provide a singleton for a ProcessService instance.
 * @author Sean Greenhow
 *
 */
public class ProcessServiceFactory {

	private static ProcessService processService;

	/**
	 * Return installed ProcessService. If none is installed the method tries to install an appropriate 
	 * one. Use setProcessService to override factory supplied choice.
	 * @return ProcessService or null if unable to find appropriate one
	 */
	public static ProcessService getProcessService(){
		if ( processService == null){
			String osname = System.getProperty("os.name");
			/**
			 * Note wmic (used by VistaProcessService) in the usual mdros way is only available for:
			 		Windows XP professional
			 		Windows Server 2003/2008
			 		Vista 
				Similar for tasklist! Could use 'Process Explorer' from SysInternals as an alternative.
			 */
			if ( osname.startsWith("Windows")){
				if ( osname.endsWith("2003") ||
						osname.endsWith("2008") ||
						osname.endsWith("XP") ||     // if pro, otherwise most methods will fail
						osname.endsWith("Vista") ||
						osname.endsWith("Windows NT (unknown)")// vista return for JVM pre 1.5u8 bug #6220825
				){ 
					processService = new VistaProcessService();
					try{ 
						processService.isPID("1");
					} catch (UnsupportedOperationException ex){
						processService = new BasicWindowsProcessService();
					}
				}else{
					processService = new BasicWindowsProcessService();
				}
			} else{ // otherwise assume its unix
				processService = new UnixProcessService();
			}
		}
		return processService;
	}

	/**
	 * Set the singleton to the supplied instance. This is required if getProcessService cannot assign a appropriate 
	 * instance. For example a fall back implementation for windows could be written that supports a third party 
	 * command line executable such as the Beyond Logic process viewer/killer see
	 * http://www.beyondlogic.org/consulting/processutil/processutil.htm.
	 * @param service The instance to set, or null
	 */
	public static void setProcessService(ProcessService service){
		ProcessServiceFactory.processService = service;
	}

}
