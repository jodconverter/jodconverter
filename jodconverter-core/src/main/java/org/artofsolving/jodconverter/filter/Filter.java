package org.artofsolving.jodconverter.filter;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.lang.XComponent;

/**
 * Represents a step where a document is transformed.
 */
public interface Filter {

    /**
     * The <code>doFilter</code> method of the Filter is called each time a document is passed
     * through the chain due to a conversion request. The FilterChain passed in to this method
     * allows the Filter to pass on the document to the next entity in the chain.
     * <p>
     * A typical implementation of this method would <strong>either</strong> invoke the next filter
     * in the chain using the FilterChain object (<code>chain.doFilter()</code>),
     * <strong>or</strong> not pass on the document to the next filter in the filter chain to block
     * the conversion processing.
     *
     * @param context
     *            the OfficeContext in use to pass along the chain.
     * @param document
     *            the XComponent being converted to pass along the chain.
     *
     * @throws OfficeException
     *             if an error processing the filter.
     */
    public void doFilter(OfficeContext context, XComponent document, FilterChain chain) throws OfficeException;
}
