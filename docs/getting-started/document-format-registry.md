# Document Format Registry

This page explains the concept of the document format registry used by **JODConverter**’s Java library. If you are new
to **JODConverter**, start with the Java Library overview, then come back here for details about how formats are defined
and found during a conversion.

### What is a Document Format?

A `DocumentFormat` describes a file type that OOo can read or write. It includes:

- Name, primary extension and alternate extensions (e.g., pdf, docx, odt, xlsx, html(htm), png…).
- Media type (MIME type).
- Optional load properties applied when opening a document of that format.
- Optional store properties per document family (TEXT, SPREADSHEET, PRESENTATION, DRAWING) used when exporting.

### What is the Registry?

The `DocumentFormatRegistry` is a lookup service that maps extensions and media types to `DocumentFormat` descriptors,
and lists the available output formats for a document family.

**JODConverter** ships with a comprehensive default registry covering common office, text, spreadsheet, presentation,
drawing and image formats.

#### Core Types

- `DocumentFormat`: The immutable description of a format (`org.jodconverter.core.document.DocumentFormat`).
- `DocumentFormatRegistry`: Interface to look up formats (`org.jodconverter.core.document.DocumentFormatRegistry`).
- `DefaultDocumentFormatRegistry`: Static convenience access to the default registry and well-known constants (e.g.,
  `DefaultDocumentFormatRegistry.PDF`).
- `SimpleDocumentFormatRegistry`: A mutable in-memory registry you can build programmatically.
- `JsonDocumentFormatRegistry`: A registry that can be loaded from JSON.

### Default Registry

The default document format registry is backed by a bundled JSON file: `/document-formats.json`.

If a `/custom-document-formats.json` is present on the application classpath, it is automatically loaded and merged on
top of the defaults (overrides existing formats or adds new ones).

#### Typical Usage

- Let **JODConverter** pick formats automatically by file extension: If you pass File or stream with an explicit target
  format, converters will use the registry to resolve the correct configuration.
- Query formats yourself: Access by extension or media type

```java
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;

DocumentFormat pdf = DefaultDocumentFormatRegistry.getFormatByExtension("pdf");
DocumentFormat docx = DefaultDocumentFormatRegistry.getFormatByMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
```

- List available output formats for a given family

```java
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;

var outputForText = DefaultDocumentFormatRegistry.getOutputFormats(DocumentFamily.TEXT);
```

### Custom Registry

Both `LocalConverter` and `RemoteConverter` accept a format registry in their builder.

```java
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.document.JsonDocumentFormatRegistry;
import org.jodconverter.local.LocalConverter;

// Example: load additional/overridden formats from a JSON string or stream
DocumentFormatRegistry custom = JsonDocumentFormatRegistry.create(jsonInputStream);

LocalConverter converter = LocalConverter.builder()
    .formatRegistry(custom) // use your registry
    .build();
```

### Programmatic Customization

Build a registry in code when you only need a few tweaks:

```java
import org.jodconverter.core.document.*;

SimpleDocumentFormatRegistry reg = new SimpleDocumentFormatRegistry();

DocumentFormat myPdf = DocumentFormat.builder()
    .name("PDF-A1")
    .extension("pdf")
    .mediaType("application/pdf")
    // Customize store properties for TEXT family (export as PDF/A-1)
    .storeProperty(DocumentFamily.TEXT, "FilterName", "writer_pdf_Export")
    .storeProperty(DocumentFamily.TEXT, "SelectPdfVersion", 1)
    .unmodifiable(true)
    .build();

reg.addFormat(myPdf);
```

### Overriding the Default Registry

If you prefer to change the global default used by `DefaultDocumentFormatRegistry` constants, set the instance:

```java
import org.jodconverter.core.document.*;

DefaultDocumentFormatRegistryInstanceHolder.setInstance(registry);
```

### Load/Store Property Precedence

Load properties used when opening a document are determined as follows:

1. Input `DocumentFormat` load properties (from the registry), then
2. Converter default load properties (Hidden=true, ReadOnly=true, UpdateDocMode=NO_UPDATE unless configured otherwise),
   then
3. Explicit per-converter loadProperty/loadProperties you set in the builder

Store properties used when saving the output are determined as follows:

1. Target `DocumentFormat` store properties (from the registry), then
2. Explicit per-converter storeProperty/storeProperties you set in the builder

### Where to put custom JSON

- Place `custom-document-formats.json` on your application’s runtime classpath (e.g., in `src/main/resources`) to have
  it automatically merged with the defaults
- Or load any JSON at runtime and pass a `JsonDocumentFormatRegistry` instance to your converter

### Troubleshooting

If a format cannot be resolved, ensure the extension is correct and present in the registry.

For advanced PDF export options (bookmarks, PDF/A, tagged PDF, etc.), set the corresponding store properties on the
target `DocumentFormat` or via converter.storeProperty.

### Related Pages

- [Getting Started: Supported Formats](../getting-started/supported-formats.md)
- [Getting Started: Document Converters](../getting-started/document-converters.md)
