//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import static org.artofsolving.jodconverter.office.OfficeUtils.*;

import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeTask;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.util.XCloseable;

public class MockOfficeTask implements OfficeTask {

    private long delayTime = 0L;

    private boolean completed = false;

    public MockOfficeTask() {
        // default
    }

    public MockOfficeTask(long delayTime) {
        this.delayTime = delayTime;
    }

    public void execute(OfficeContext context) throws OfficeException {
        XComponentLoader loader = cast(XComponentLoader.class, context.getService(SERVICE_DESKTOP));
        assert loader != null : "desktop object is null";
        try {
            PropertyValue[] arguments = new PropertyValue[] { property("Hidden", true) };
            XComponent document = loader.loadComponentFromURL("private:factory/swriter", "_blank", 0, arguments);
            if (delayTime > 0) {
                Thread.sleep(delayTime);
            }
            cast(XCloseable.class, document).close(true);
            completed = true;
        } catch (Exception exception) {
            throw new OfficeException("failed to create document", exception);
        }
    }

    public boolean isCompleted() {
        return completed;
    }

}
