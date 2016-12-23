package org.artofsolving.jodconverter;


import java.util.Map;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.container.XIndexAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;

/**************************************************************
 * 

 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 *************************************************************/



//***************************************************************************
// comment: Step 1: get the Desktop object from the office
//          Step 2: open an empty text document
//          Step 3: enter a example text
//          Step 4: replace some english spelled words with US spelled
//***************************************************************************


import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import com.sun.star.util.XSearchDescriptor;
import com.sun.star.util.XSearchable;

public class TextReplaceOfficeTask extends AbstractOfficeTask {
	private XComponent doc;
	private String mark;
	private String replacement;

	public TextReplaceOfficeTask(XComponent doc,String mark, String replacement) {
		super(doc);
		this.mark=mark;
		this.replacement=replacement;
	}
    
	public void execute(OfficeContext context) throws OfficeException {
		//NEEDED PARAMETERS MUST BE PRESENT USING THE CONSTRUCTOR AND MUST BE VALID.
		if(document == null) {
			completed=true;
			throw new OfficeException("The specified file does not exist.");
		}
		
		if(mark==null || mark.equals("")) {
			completed=true;
			throw new OfficeException("The mark is not a valid string");
		}
		
		if(replacement==null || replacement.equals("")) {
			completed=true;
			throw new OfficeException("The replacement is not a valid string");
		}
		
		//TEXT REPLACEMENT TASK.
		try {
	           XReplaceDescriptor xReplaceDescr = null;

	           XReplaceable xReplaceable = null;    	  
	     	   
	     	   XTextDocument doc = (XTextDocument)
		                UnoRuntime.queryInterface(
			                    XTextDocument.class, document);
	     	   
	     	   XSearchable xSearch =(XSearchable) UnoRuntime.queryInterface(XSearchable.class,doc);
	     	   
	     	   XSearchDescriptor xSd= xSearch.createSearchDescriptor();
	     	   xSd.setPropertyValue("SearchWords", true);
	     	  xSd.setPropertyValue("SearchStyles", false);
	     	   xSd.setSearchString("MARK");
	     	   
	     	   XIndexAccess xind= xSearch.findAll(xSd);
	     	   
				xReplaceable = (XReplaceable)
	                UnoRuntime.queryInterface(
	                    XReplaceable.class, doc);
	            
	           	xReplaceDescr = (XReplaceDescriptor)
                xReplaceable.createReplaceDescriptor();
            	xReplaceDescr.setReplaceString(replacement);
                xReplaceDescr.setSearchString(mark);

                
                // Replace all words
                xReplaceable.replaceAll( xReplaceDescr );

	        }
	        catch( Exception e) {
	            e.printStackTrace(System.err);
	        }
		
        completed=true;
	}

    
}
