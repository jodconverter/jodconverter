# Document Converters

This page explains what a Document Converter is in the **JODConverter** ecosystem and how it is used to perform
conversions.

## What is a Document Converter?

In **JODConverter**, a Document Converter is the high-level component that orchestrates document conversions. It exposes
a fluent API to:

- Accept a source document (File or InputStream), optionally with an explicit format.
- Target a destination (File or OutputStream), optionally with a desired format and save options.
- Execute the conversion via an Office Manager.

See the interface definition: org.jodconverter.core.DocumentConverter.
[org.jodconverter.core.DocumentConverter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-core/src/main/java/org/jodconverter/core/DocumentConverter.java).

Converters do not start or manage OOo processes by themselves. They delegate actual execution to an
Office Manager, which handles lifecycle, queuing, timeouts, and resilience. A converter focuses on I/O selection, format
resolution, and applying conversion options/filters.

## Why do you need one?

- Simplified conversion API: Compose conversions with a readable, fluent builder.
- Format resolution: Uses a `DocumentFormatRegistry` to infer formats by extension/MIME type, and allows you to override
    them explicitly.
- Options and filters: Provide document-family-specific options (e.g., PDF export options) and filter chains to adjust
    content before saving.
- Integration: Works with a provided `OfficeManager` or the globally installed manager.

Without a Document Converter, you would have to craft and execute low-level OfficeTasks yourself.

## Converter types

**JODConverter** provides different converter implementations depending on where/how the office backend is running:

- **LocalConverter**: Uses a **LocalOfficeManager** to communicate with local OOo processes.
    See [LocalConverter](../configuration/local-converter.md) for all configuration options and examples.
- **RemoteConverter**: Uses a **RemoteOfficeManager** to communicate with LibreOffice Online / Collabora Online.
    See [RemoteConverter](../configuration/remote-converter.md) for configuration and examples.

Internally, concrete converters extend an abstract base that wires the format registry, job pipeline, and office task
execution.

## Typical usage patterns

**1)** Use the globally installed OfficeManager and the simple `make()` builders.

If an OfficeManager is installed in the `InstalledOfficeManagerHolder`, converter builders can use it implicitly.

Java example:

```java
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

public class Example {
  public static void main(String[] args) throws OfficeException {
    // Install a LocalOfficeManager as the global default
    LocalOfficeManager officeManager = LocalOfficeManager.install();
    try {
      officeManager.start();

      // LocalConverter.make() will use the installed manager automatically
      LocalConverter
          .make()
          .convert(new java.io.File("in.docx"))
          .to(new java.io.File("out.pdf"))
          .execute();

    } finally {
      OfficeUtils.stopQuietly(officeManager);
    }
  }
}
```

**2)** Pass a manager explicitly to a converter.

```java
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

OfficeManager officeManager = LocalOfficeManager.builder().build();
try {
  officeManager.start();

  LocalConverter
      .builder()
      .officeManager(officeManager)
      .build()
      .convert(new java.io.File("in.docx"))
      .to(new java.io.File("out.pdf"))
      .execute();

} finally {
  OfficeUtils.stopQuietly(officeManager);
}
```

**3)** Working with streams and explicit formats.

```java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.local.LocalConverter;

try (FileInputStream in = new FileInputStream("in.html");
     FileOutputStream out = new FileOutputStream("out.odt")) {

  LocalConverter
      .make()
      .convert(in) // could also be convert(in, true) to auto-close
      .as(DefaultDocumentFormatRegistry.HTML)
      .to(out)
      .as(DefaultDocumentFormatRegistry.ODT)
      .execute();
}
```

**4)** Converting to PDF with [PDF options](pdf-options.md).

```java
import java.io.File;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.core.pdf.PdfVersion;

converter
    .convert(new File("in.odt"))
    .to(new File("out.pdf"))
    .with(PdfOptions.builder().version(PdfVersion.PDF_A_2B).tagged(true).build())
    .execute();
```

**5)** Applying filters and PDF options (`LocalConverter`).

```java
import java.io.File;
import org.jodconverter.core.pdf.PdfOptions;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.filter.text.DocumentIndexesUpdaterFilter;

LocalConverter
    .builder()
    .filterChain(new DocumentIndexesUpdaterFilter())
    .build()
    .convert(new File("in.odt"))
    .to(new File("out.pdf"))
    .with(PdfOptions.archive())
    .execute();
```

Notes:

- [Filters](using-filters.md) modify the loaded document (update its indexes, remove pages, add text) before it is
    stored; without a filter chain, the document is only refreshed.
- [PDF options](pdf-options.md) choose how the PDF is exported; any other store property of an office export filter
    can be given with `storeProperty(name, value)`.

## Lifecycle and threading

- Requires an `OfficeManager`: A converter relies on a running `OfficeManager`. Start the manager before executing
    conversions and stop it on shutdown.

- Thread-safe: Converters can be reused across threads; job execution is queued through the `OfficeManager`.

- Asynchronous execution: `executeAsync()` submits the conversion and returns a `CompletableFuture<Void>` at once,
    instead of blocking like `execute()`. The future completes when the conversion is done, or exceptionally with an
    `OfficeException` when it fails; cancelling it abandons the conversion. Several conversions can thus run in
    parallel, one per office process of the manager, and be joined later:

    ```java
    final var futures =
        sources.stream()
            .map(source -> converter.convert(source).to(targetOf(source)).executeAsync())
            .toList();
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    ```

    The actions chained to the future (`thenRun`, `whenComplete`...) may run on a thread of the office manager, so
    they must not block. `OfficeManager.submit(task)` is the same for an `OfficeTask`.

- Format registry: `getFormatRegistry()` returns the formats supported by the converter. `LocalConverter` typically uses
    `DefaultDocumentFormatRegistry`.

## Best practices

- Reuse a single converter instance when possible, backed by a single `OfficeManager`. Avoid per-request instantiation.
- Prefer File I/O for very large documents to minimize memory pressure; streams are convenient but may incur buffering.
- Specify formats explicitly when converting from streams without file extensions.
- Tune OfficeManager timeouts and process counts for your workload (see the Office Managers page and LocalOfficeManager
    configuration).

## Related APIs

- [Merging documents](using-filters.md#merging-documents): `LocalConverter.merge(...)`, several text documents into
    one output.

- [Slides to images](page-images.md): `LocalConverter.exportPages(...)`, one image per slide or draw page.

- `DocumentConverter` (core): high-level conversion contract.
- `DefaultDocumentFormatRegistry` (core): common formats and MIME mappings.
- Conversion job API (core.job): fluent pipeline (convert (...).to (...).execute ()).
- `LocalConverter` (local): converter for local office processes.
- `RemoteConverter` (remote): converter for LibreOffice Online / Collabora Online.
- `InstalledOfficeManagerHolder` (core): global singleton used when no manager is provided explicitly.

For detailed configuration of each converter type, refer to:

- [LocalConverter](../configuration/local-converter.md)
- [RemoteConverter](../configuration/remote-converter.md)

--8<-- "note.md"
