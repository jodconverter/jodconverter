package org.artofsolving.jodconverter.filter;

/**
 * Default implementation of FilterChain.
 */
public class DefaultFilterChain extends FilterChainBase {

    /**
     * Creates a FilterChain that will contains the specified filters.
     * 
     * @param filters
     *            the filters to add to the chain.
     */
    public DefaultFilterChain(Filter... filters) {
        super(filters);
    }
    
    // Change method visibility
    @Override
    public void addFilter(Filter filter) {
        super.addFilter(filter);
    }
}
