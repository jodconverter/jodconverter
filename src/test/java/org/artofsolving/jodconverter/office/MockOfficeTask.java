//
// JODConverter - Java OpenDocument Converter
// Copyright 2009 Art of Solving Ltd
// Copyright 2004-2009 Mirko Nasato
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
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
