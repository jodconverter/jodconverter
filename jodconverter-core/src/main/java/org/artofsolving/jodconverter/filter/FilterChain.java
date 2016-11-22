package org.artofsolving.jodconverter.filter;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.lang.XComponent;

/**
 * A FilterChain is an object that is responsible to managed an invocation chain of filters. Filters
 * use the FilterChain to invoke the next filter in the chain, or if the calling filter is the last
 * filter in the chain, to end the invocation chain.
 */
public interface FilterChain {

    /**
     * Causes the next filter in the chain to be invoked, or if the calling filter is the last
     * filter in the chain, do nothing.
     *
     * @param context
     *            the OfficeContext in use to pass along the chain.
     * @param document
     *            the XComponent being converted to pass along the chain.
     *
     * @throws OfficeException
     *             if an error processing the filter.
     */
    void doFilter(OfficeContext context, XComponent document) throws OfficeException;
}
