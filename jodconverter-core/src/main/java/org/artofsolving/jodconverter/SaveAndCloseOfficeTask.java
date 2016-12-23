package org.artofsolving.jodconverter;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;

public class SaveAndCloseOfficeTask extends AbstractOfficeTask{

	public SaveAndCloseOfficeTask(XComponent doc) {
		super(doc);
		document=doc;
	}

	@Override
	public void execute(OfficeContext context) throws OfficeException {
		if(document == null) {
			completed=true;
			throw new OfficeException("The specified file does not exist.");
		}
		
		try {
				//Get the TextDocumentInterface
	     	   XTextDocument doc = (XTextDocument)
		                UnoRuntime.queryInterface(
			                    XTextDocument.class, document);
	     	   
	     	   //Get the Storable Interface
	     	   XStorable store =(XStorable) UnoRuntime.queryInterface(XStorable.class,doc);
	     	   
	     	   //Save the document
	     	   store.store();
	     	   
	     	   //Close the Document
	     	   doc.dispose();
	     	   doc=null;
	     	   document=null;

	        }
	        catch( Exception e) {
	            e.printStackTrace(System.err);
	        }
		
     completed=true;
		
	}

}
