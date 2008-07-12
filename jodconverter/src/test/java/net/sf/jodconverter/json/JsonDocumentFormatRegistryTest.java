package net.sf.jodconverter.json;

import static org.testng.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import net.sf.jodconverter.DocumentFamily;
import net.sf.jodconverter.DocumentFormat;
import net.sf.jodconverter.DocumentFormatRegistry;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.testng.annotations.Test;

@Test
public class JsonDocumentFormatRegistryTest {

    public void readJsonRegistry() throws JSONException, IOException {
        InputStream input = getClass().getResourceAsStream("/document-formats.js");
        DocumentFormatRegistry registry = null;
        try {
            registry = new JsonDocumentFormatRegistry(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
        DocumentFormat odt = registry.getFormatByExtension("odt");
        assertNotNull(odt);
        assertNotNull(odt.getStoreProperties(DocumentFamily.TEXT));
    }

}
