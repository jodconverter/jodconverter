# Usage as a Java Library

> Whenever OpenOffice.org (OOo for short) is mentioned, this can generally be interpreted to include any office suite derived from OOo such as [Apache OpenOffice](https://www.openoffice.org) and [LibreOffice](https://www.libreoffice.org).

Using **JODConverter** in your own Java application is very straightforward. The following example shows the skeleton code required to perform a one off conversion from a Word document to PDF:

```java
File inputFile = new File("document.doc");
File outputFile = new File("document.pdf");

// Create an office manager using the default configuration.
// The default port is 2002.
final DefaultOfficeManager officeManager = DefaultOfficeManager.install(); 
try {

    // Start an office process and connect to the started instance (on port 2002).
    officeManager.start();

    // Convert
    JODConverter
             .convert(inputFile)
             .to(outputFile)
             .execute();
} finally {
    // Stop the office process
    OfficeUtils.stopQuietly(officeManager);
}
```

To convert from/to other formats, simply change the file names and the formats will be determined based on file extensions; e.g. to convert an Excel file to OpenDocument Spreadsheet file:


```java
File inputFile = new File("spreadsheet.xls");
File outputFile = new File("spreadsheet.ods");
JODConverter
         .convert(inputFile)
         .to(outputFile)
         .execute();
```

If you are working with streams instead of file, no problems! You just have to inform JODConverter what are the input and output document formats; e.g: to convert an Excel stream to OpenDocument Spreadsheet stream:

```java
InputStream inputStream = ...
OutputStream outputStream = ...
JODConverter
         .convert(inputStream)
         .as(DefaultDocumentFormatRegistry.XLS)
         .to(outputStream)
         .as(DefaultDocumentFormatRegistry.ODS)
         .execute();
```

Simple, isn't it? Yet this example actually shows almost everything you need to know for most applications.

Almost because establishing a new connection each time you need to do a conversion, while perfectly acceptable, is not the best idea from a performance point of view. If you're integrating JODConverter in a web application for example you may want to initialize a single OfficeManager instance when the app is started and stop it when the app is stopped.

There are many different ways to do this depending on which web framework (if any) you're using so I'm not going to explain it here. For plain Servlet API you can use a context listener, for Spring you can use the jodconverter-spring or jodconverter-spring-boot-starter module, and so on.