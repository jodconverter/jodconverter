package org.artofsolving.jodconverter.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.lang.XComponent;

/**
 * Base class of a FilterChain.
 */
public abstract class FilterChainBase implements FilterChain {

    private boolean readOnly;
    private List<Filter> filters;
    private int pos; // to maintain the current position in the filter chain.

    /**
     * Creates a FilterChain.
     */
    public FilterChainBase() {
        this(new Filter[] {});
    }

    /**
     * Creates a FilterChain that will contains the specified filters.
     * 
     * @param filters
     *            the filters to add to the chain.
     */
    public FilterChainBase(Filter... filters) {
        this(false, filters);

        if (filters != null) {
            for (Filter filter : filters) {
                addFilter(filter);
            }
        }
    }

    /**
     * Creates a FilterChain that will contains the specified filters.
     * 
     * @param readOnly
     *            {@code true} if the chain must be read-only (which means that no other filter can
     *            be added to the chain), {@code false} otherwise.
     * @param filters
     *            the filters to initially add to the chain.
     */
    public FilterChainBase(boolean readOnly, Filter... filters) {

        this.readOnly = readOnly;
        this.pos = 0;
        this.filters = new ArrayList<Filter>();

        if (filters != null) {
            for (Filter filter : filters) {
                this.filters.add(filter);
            }
        }

        if (readOnly) {
            this.filters = Collections.unmodifiableList(this.filters);
        }
    }

    /**
     * Adds a filter to the chain.
     * 
     * @param filter
     *            the filter to add at the end of the chain.
     */
    protected void addFilter(Filter filter) {

        if (readOnly) {
            throw new UnsupportedOperationException();
        }
        filters.add(filter);
    }

    /**
     * Resets the position in the filter chain to 0, making the chain reusable.
     */
    public void reset() {

        pos = 0;
    }

    @Override
    public void doFilter(OfficeContext context, XComponent document) throws OfficeException {

        // Call the next filter if there is one
        if (pos < filters.size()) {
            Filter filter = filters.get(pos++);
            filter.doFilter(context, document, this);
        }
    }
}
