package org.artofsolving.jodconverter.filter;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

/**
 * This filter is used to refresh a document.
 */
public class RefreshFilter implements Filter {
    
    public static final RefreshFilter INSTANCE = new RefreshFilter();

    @Override
    public void doFilter(OfficeContext context, XComponent document, FilterChain chain) throws OfficeException {
        
        XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, document);
        if (refreshable != null) {
            refreshable.refresh();
        }
        chain.doFilter(context, document);
    }
}
