# LocalConverter Configuration

`LocalConverter` is the high-level document converter implementation that talks to local OOo processes through an
`OfficeManager`. It offers a fluent API to:

- Accept a source (File or InputStream) and optionally specify its format.
- Choose a target (File or OutputStream) and optionally specify its format and save options.
- Optionally apply filters to the opened document prior to saving (e.g., refresh fields).
- Execute the conversion via a running `OfficeManager`.

It must be used with an `OfficeManager` that manages local office processes, typically:

- `LocalOfficeManager` (starts and manages local processes), or
- `ExternalOfficeManager` (connects to an already running local process that you started).

See class:
[org.jodconverter.local.LocalConverter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/LocalConverter.java).

A `LocalConverter` is built using a builder:

```java
LocalConverter converter = LocalConverter.builder().build();
```

Here are all the properties you can set through the builder:

#### `officeManager`

This property specifies the `OfficeManager` the converter will use to execute office tasks.

#### `formatRegistry`

This property specifies the `DocumentFormatRegistry` which contains the document formats that will be supported by this
converter.

#### `applyDefaultLoadProperties`

This property specifies that this converter will apply the default load properties when loading a source
document.

&nbsp;***Default***: true.

Default load properties are:

- **Hidden**: true
- **ReadOnly**: true
- **UpdateDocMode**: UpdateDocMode.NO_UPDATE

When building the load properties map that will be used to load a source document, the load properties of the input [
`DocumentFormat`](../getting-started/document-format-registry.md//#what-is-a-document-format), if any, are put in the
map first. Then, the default load properties, if required, are added to the map. Finally, any properties specified in
the `loadProperty(String, Object)` or `loadProperties(Map)` are put in the map.

#### `useUnsafeQuietUpdate`

This property specifies whether this converter will use the unsafe `UpdateDocMode.QUIET_UPDATE` as default for the
`UpdateDocMode` load property, which was the default until **JODConverter** version 4.4.4.

See this article for more details about the security issue:
<a href="https://buer.haus/2019/10/18/a-tale-of-exploitation-in-spreadsheet-file-conversions/">A Tale of Exploitation in
Spreadsheet File Conversions</a>

#### loadDocumentMode(LoadDocumentMode)

This property specifies how a document is loaded/stored when converting a document, whether it is loaded assuming the
office process has access to the file on disk or not. If not, the conversion process will use stream adapters

&nbsp;***Default***: LoadDocumentMode.AUTO

#### loadProperty(String, Object) / loadProperties(Map<String, Object>)

This property specifies a property, for this converter, that will be applied when a document is loaded during a
conversion task, regardless of the input format of the document.

When building the load properties map that will be used to load a source document, the load properties of the input [
`DocumentFormat`](../getting-started/document-format-registry.md//#what-is-a-document-format), if any, are put in the
map first. Then, the default load properties, if required, are added to the map. Finally, any properties specified in
the `loadProperty(String, Object)` or `loadProperties(Map)` are put in the map.

Any property set here will override the property with the same name from the input document format or the default load
properties.

#### storeProperty(String, Object) / storeProperties(Map<String, Object>)

This property specifies a property, for this converter, that will be applied when a document is stored during a
conversion task, regardless of the output format of the document.

Custom properties are applied after the store properties of the target **DocumentFormat**, so any property set here will
override the property with the same name from the document format.

#### filterChain(Filter... filters) / filterChain(FilterChain)

This property specifies the filters to apply when converting a document. Filter may be used to modify the document
before the conversion (after it has been loaded). Filters are applied in the same order they appear as arguments.

Notes:

- Load and store property keys/values are UNO properties understood by LibreOffice. Valid keys and values vary by format
  and LO version.
- For PDF export, common options are provided via the PDF export filter (e.g., SelectPdfVersion, ExportBookmarks, etc.).

--8<-- "note.md"