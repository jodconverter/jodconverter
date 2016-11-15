package org.artofsolving.jodconverter.step;

import org.artofsolving.jodconverter.office.OfficeContext;

import com.sun.star.lang.XComponent;

/**
 * Represents a step where a document is transformed.
 */
public interface TransformerStep {

    /**
     * Transform the specified document.
     * 
     * @param context
     *            the office context.
     * @param document
     *            the document.
     */
    void transform(OfficeContext context, XComponent document);
}
