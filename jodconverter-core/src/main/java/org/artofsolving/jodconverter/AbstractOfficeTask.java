package org.artofsolving.jodconverter;

import java.util.HashMap;
import java.util.Map;

import org.artofsolving.jodconverter.office.OfficeException;

/*
 * Jose Luis López López
 * 
 * The purpose of this class is to enable the framework to execute different tasks provided by the API over an
 * already opened document. The document can be opened usinf the OfficeDocumentUtils loadDocument() static method
 * and passed to any TASK that is extension of this class.  
 */

import org.artofsolving.jodconverter.office.OfficeTask;

import com.sun.star.document.UpdateDocMode;
import com.sun.star.lang.XComponent;

public abstract class AbstractOfficeTask implements OfficeTask {

	protected XComponent document;
	protected boolean completed=false;

		
	public AbstractOfficeTask(XComponent doc ) {
		document=doc;
	}
	

	public boolean isCompleted() {
		return completed;
	}

}