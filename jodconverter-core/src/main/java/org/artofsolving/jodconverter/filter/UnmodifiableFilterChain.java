package org.artofsolving.jodconverter.filter;

/**
 * Unmodifiable implementation of FilterChain.
 */
public class UnmodifiableFilterChain extends FilterChainBase {

    /**
     * Creates an unmodifiable FilterChain that will contains the specified filters.
     * 
     * @param filters
     *            the filters to add to the chain.
     */
    public UnmodifiableFilterChain(Filter... filters) {
        super(true, filters);
    }
}
