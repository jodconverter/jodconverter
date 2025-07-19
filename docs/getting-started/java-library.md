# Java Library

By default, **JODConverter** is built using the OpenOffice libraries.
See [here](https://github.com/jodconverter/jodconverter/issues/113) to know why. But you can now decide whether
you want to use **JODConverter** with the LibreOffice libraries or the OpenOffice libraries.

## Maven dependencies

### LibreOffice

=== "Gradle"

    ```groovy title="Groovy"
    implementation 'org.jodconverter:jodconverter-local-lo:4.4.10'
    ```
    
    ```kotlin title="Kotlin"
    implementation("org.jodconverter:jodconverter-local-lo:4.4.10")
    ```

=== "Maven"

    ```xml
    
    <dependency>
        <groupId>org.jodconverter</groupId>
        <artifactId>jodconverter-local-lo</artifactId>
        <version>4.4.10</version>
    </dependency>
    ```

### OpenOffice

=== "Gradle"

    ```groovy title="Groovy"
    implementation 'org.jodconverter:jodconverter-local:4.4.10'
    ```

    ```kotlin title="Kotlin"
    implementation("org.jodconverter:jodconverter-local:4.4.10")
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>org.jodconverter</groupId>
        <artifactId>jodconverter-local</artifactId>
        <version>4.4.10</version>
    </dependency>
    ```

or

=== "Gradle"

    ```groovy title="Groovy"
    implementation 'org.jodconverter:jodconverter-local-oo:4.4.10'
    ```

    ```kotlin title="Kotlin"
    implementation("org.jodconverter:jodconverter-local-oo:4.4.10")
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>org.jodconverter</groupId>
        <artifactId>jodconverter-local-oo</artifactId>
        <version>4.4.10</version>
    </dependency>
    ```

## Usage

Using **JODConverter** in your own Java application is very straightforward. The following example shows the skeleton
code required to perform a one-off conversion from a Word document to PDF:

```java
File inputFile = new File("document.doc");
File outputFile = new File("document.pdf");

// Create an office manager using the default configuration.
// The default port is 2002. Note that when an office manager
// is installed, it will be the one used by default when
// a converter is created.
LocalOfficeManager officeManager = LocalOfficeManager.install(); 
try {

    // Start an office process and connect to the started instance (on port 2002).
    officeManager.start();

    // Convert
    JodConverter
             .convert(inputFile)
             .to(outputFile)
             .execute();
} finally {
    // Stop the office process
    OfficeUtils.stopQuietly(officeManager);
}
```

To convert from/to other formats, change the file names and the formats will be determined based on file
extensions; e.g., to convert an Excel file to OpenDocument Spreadsheet file:

```java
File inputFile = new File("spreadsheet.xls");
File outputFile = new File("spreadsheet.ods");
JodConverter
         .convert(inputFile)
         .to(outputFile)
         .execute();
```

If you are working with streams instead of files, no problems! You have to inform JODConverter what the input
and output document formats are; e.g.: to convert an Excel stream to OpenDocument Spreadsheet stream:

```java
InputStream inputStream = ...
OutputStream outputStream = ...
JodConverter
         .convert(inputStream)
         .as(DefaultDocumentFormatRegistry.XLS)
         .to(outputStream)
         .as(DefaultDocumentFormatRegistry.ODS)
         .execute();
```

Simple, isn't it? Yet this example actually shows almost everything you need to know for most applications.

Almost because establishing a new connection each time you need to do a conversion, while perfectly acceptable, is not
the best idea from a performance point of view. If you're integrating JODConverter in a web application, for example,
you may want to initialize a single OfficeManager instance when the app is started and stop it when the app is stopped.

There are many different ways to do this depending on which web framework (if any) you're using, so I'm not going to
explain it here. For plain Servlet API you can use a context listener, for Spring you can use the jodconverter-spring
or jodconverter-spring-boot-starter module, and so on.