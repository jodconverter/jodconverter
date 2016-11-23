package org.artofsolving.jodconverter.sample.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.uno.RuntimeException;

public class WebappContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {

        try {
            WebappContext.init(event.getServletContext());
        } catch (OfficeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            WebappContext.destroy(event.getServletContext());
        } catch (OfficeException e) {
            throw new RuntimeException(e);
        }
    }

}
