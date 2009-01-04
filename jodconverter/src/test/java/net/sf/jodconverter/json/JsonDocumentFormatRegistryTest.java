package org.artofsolving.jodconverter.json;

import static org.testng.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.DocumentFamily;
import org.artofsolving.jodconverter.DocumentFormat;
import org.artofsolving.jodconverter.DocumentFormatRegistry;
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
