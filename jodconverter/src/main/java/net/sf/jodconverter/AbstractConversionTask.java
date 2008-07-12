package net.sf.jodconverter;

import static net.sf.jodconverter.office.UnoUtils.*;

import java.io.File;
import java.util.Map;

import net.sf.jodconverter.office.OfficeContext;
import net.sf.jodconverter.office.OfficeException;
import net.sf.jodconverter.office.OfficeTask;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XRefreshable;

public abstract class AbstractConversionTask implements OfficeTask {

    private final File inputFile;
    private final File outputFile;

    public AbstractConversionTask(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    protected abstract Map<String,?> getLoadProperties(File inputFile);

    protected abstract Map<String,?> getStoreProperties(File outputFile, XComponent document);

    public void execute(OfficeContext context) throws OfficeException {
        XComponentLoader loader = cast(XComponentLoader.class, context.getService(SERVICE_DESKTOP));
        XComponent document = null;
        try {
            Map<String,?> loadProperties = getLoadProperties(inputFile);
            document = loader.loadComponentFromURL(toUrl(inputFile), "_blank", 0, toUnoProperties(loadProperties));
            XRefreshable refreshable = cast(XRefreshable.class, document);
            if (refreshable != null) {
                refreshable.refresh();
            }
            Map<String,?> storeProperties = getStoreProperties(outputFile, document);
            cast(XStorable.class, document).storeToURL(toUrl(outputFile), toUnoProperties(storeProperties));
        } catch (IOException ioException) {
            throw new OfficeException("conversion failed", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new OfficeException("conversion failed", illegalArgumentException);
        } finally {
            try {
                cast(XCloseable.class, document).close(true);
            } catch (CloseVetoException closeVetoException) {
                // whoever raised the veto should close the document
            }
        }
    }

}
