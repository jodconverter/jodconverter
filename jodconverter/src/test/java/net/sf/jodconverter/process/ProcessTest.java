package org.artofsolving.process;

import static org.testng.Assert.assertTrue;

import org.artofsolving.process.ProcessService;
import org.artofsolving.process.ProcessServiceFactory;
import org.testng.annotations.Test;

/**
 * Test cases for ProcessService.
 * 
 * @author Sean Greenhow
 *
 */
@Test(groups="functional")
public class ProcessTest {

	public static void main(String[]args){	
		try{
			ProcessTest pt = new ProcessTest();
			pt.findProcessTest();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void findProcessTest() {
		
		ProcessService ps = ProcessServiceFactory.getProcessService();
		try {
			String pid = ps.findPID(".*soffice.*port=8100.*");
			boolean isPid = ps.isPID(pid);
		    assertTrue(isPid);
		    //ps.kill(pid, true);
		} catch (UnsupportedOperationException ex){
			// ok if BasicWindowsProcessService
			if ( !(ps instanceof BasicWindowsProcessService)){
				throw ex;
			}
		}
		
	}
}
