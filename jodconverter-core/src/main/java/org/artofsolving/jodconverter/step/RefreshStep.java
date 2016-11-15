package org.artofsolving.jodconverter.step;

import org.artofsolving.jodconverter.office.OfficeContext;

import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XRefreshable;

/**
 * This steps is used to refresh a document.
 */
public class RefreshStep implements TransformerStep {

    @Override
    public void transform(OfficeContext context, XComponent document) {
        
        XRefreshable refreshable = UnoRuntime.queryInterface(XRefreshable.class, document);
        if (refreshable != null) {
            refreshable.refresh();
        }
    }
}
