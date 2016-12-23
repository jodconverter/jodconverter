package org.artofsolving.jodconverter;

import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;

public class ExportAsPDFOfficeTask extends AbstractOfficeTask{

	public ExportAsPDFOfficeTask(XComponent doc) {
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
	     	   
	     	   //Export the document as URL, and PDF
		     	// Exporting to PDF consists of giving the proper
		     	// filter name in the property "FilterName"
		     	// With only this, the document will be exported
		     	// using the existing PDF export settings
		     	// (the one used the last time, or the default if the first time)
	     	   PropertyValue[] aMediaDescriptor = new PropertyValue[3];
	     	   aMediaDescriptor[0] = new PropertyValue();
	     	   aMediaDescriptor[0].Name = "FilterName";
	     	   aMediaDescriptor[0].Value = "writer_pdf_Export";
	     	   aMediaDescriptor[1] = new PropertyValue();
	     	   aMediaDescriptor[1].Name = "Overwrite";
	     	   aMediaDescriptor[1].Value = new Boolean(true);


	     	  //Prepare PDF/A-1 FilterData settings and Embed fonts.
	     	   PropertyValue[] aFilterData = new PropertyValue[2];
	     	    aFilterData[0] = new PropertyValue();
	     	    aFilterData[0].Name = "SelectPdfVersion";
	     	    aFilterData[0].Value = 1;
	     	   aFilterData[1] = new PropertyValue();
	     	    aFilterData[1].Name = "EmbedStandardFonts";
	     	    aFilterData[1].Value = new Boolean(true);

	     	 
	     	    aMediaDescriptor[2] = new PropertyValue();
	     	    aMediaDescriptor[2].Name = "FilterData";
	     	    aMediaDescriptor[2].Value = aFilterData;
	     	   
	     	   
	     	   XModel model= (XModel)
		                UnoRuntime.queryInterface(
			                    XModel.class, document);;
			   String url=model.getURL();
			   
			   url=FilenameUtils.getPath(url)+FilenameUtils.getBaseName(url)+".pdf";
			   
	     	   store.storeToURL(url, aMediaDescriptor);;
	     	   model=null;
	     	   
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
