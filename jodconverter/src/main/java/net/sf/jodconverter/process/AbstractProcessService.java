package net.sf.jodconverter.process;

import java.util.HashMap;
import java.util.Map;

/**
 * Helpful common class.
 * 
 * @author Sean Greenhow
 *
 */
public abstract class AbstractProcessService implements ProcessService {

	
	private Map<String,String[]> cmds = new HashMap<String,String[]>();
	
	protected String[] getCommand(String key){
		return cmds.get(key);
	}
	
	protected void setCommand(String key, String[]cmd){
		cmds.put(key, cmd);
	}
	
	
}
