---
class: hide-toc
---

# Frequently Asked Questions

## General Questions

??? question "What is JODConverter?"

    JODConverter (Java OpenDocument Converter) is a library that converts documents using LibreOffice or OpenOffice.

??? question "Is JODConverter free to use?"

    Yes. JODConverter is open-source and released under the Apache 2.0 License.

??? question "When will the next JODConverter version be released?"

    The project is maintained entirely on a voluntary basis in the developer's free time, so unfortunately there are no
    scheduled release dates.

## Installation & Setup

??? question "Do I really need to install OpenOffice.org to use JODConverter?"

    Yes. In fact, JODConverter simply automates OOo operations; all actual conversions are performed by OOo. Trying
    to use JODConverter without OOo would be like trying to use, say, the MySQL JDBC driver without a MySQL database
    server. If it can't connect to a server, the driver is useless.

??? question "Do I ***really really*** need to install OpenOffice.org? There's no way to install only a few libraries/DLLs instead?"

    Yes you do really need a complete OOo installation. At most you can omit Base and a few optional component when
    installing. (Splitting OOo conversion filters into independent components is one of the goals of an OOo sub-project
    called ODF Toolkit, but don't hold your breath.)

??? question "How well does it convert format X to format Y?"

    Different people have different requirements/expectations. Why don't you just find out for yourself? Since
    JODConverter simply automates OOo conversions, you don't even need to install JODConverter to do some tests,
    you just need OOo. Manually open a document in format X with OOo, and save it as (or export it to) format Y.
    Voilà! No tricks, no gimmicks.

??? question "Is there an option to set X (image quality, layout mode, hidden text, etc)?"

    Guess what? It depends on OOo. Start up Writer, Calc, or Impress, save/export a document in the format you want to
    convert it to, and see which options OOo provides. Once you've found out which options you want to set you can ask
    on the discussion how to automate the same operation.

??? question "Is it possible to execute multiple conversions at a time?"

    By default, JODConverter will start a single office instance, listening for conversion request on port 2002. In
    order to process more than 1 conversion at the time, you must start multiple office instances.
    
    This behavior can be achieved using the portNumbers configuration:
    
    ```java
    // This example will use 4 TCP ports, which will cause
    // JODConverter to start 4 office processes when the
    // OfficeManager will be started.
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .portNumbers(2002, 2003, 2004, 2005)
            .build();
    ```
    
    The example above shows how to start an office manager that would be able to process 4 conversions at the time.
    Note that the more office process you start, the more RAM will be consumed by LibreOffice or Apache OpenOffice.

??? question "How could I set password protection when converting a file to PDF?"

    If you want to set password protection when converting to PDF, you must set 2 filter properties, `EncryptFile` and
    `DocumentOpenPassword`.
    
    Here's how this could be done:
    
    ```java
    File inputFile = new File("document.doc");
    File outputFile = new File("document.pdf");
    
    Map<String, Object> filterData = new HashMap<>();
    filterData.put("EncryptFile",true);
    filterData.put("DocumentOpenPassword","test");
    
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put("FilterData",filterData);
    
    LocalConverter
        .builder()
        .storeProperties(customProperties)
        .build()
        .convert(inputFile)
        .to(outputFile)
        .execute();
    ```
    
    OR
    
    ```java
    Map<String, Object> filterData = new HashMap<>();
    filterData.put("EncryptFile",true);
    filterData.put("DocumentOpenPassword","test");
    
    DocumentFormat format =
        DocumentFormat
            .builder()
            .from(DefaultDocumentFormatRegistry.PDF)
            .storeProperty(DocumentFamily.TEXT, "FilterData", filterData)
            .build();
    
    JodConverter
        .convert(source)
        .to(target)
        .as(format)
        .execute();
    ```

??? question "How could I specify the password of a password-protected file (input file) to convert?"

    If you want to be able to convert a password-protected file, you must set the `Password` load property.
    
    Here's how this could be done:
    
    ```java
    final File in = new File("path_to_password_protected_file");
    final File out = new File("path_to_output_file");
    
    final OfficeManager manager = LocalOfficeManager.builder().startFailFast(true).build();
    try{
        manager.start();
        Map<String, Object> loadProperties = new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
        loadProperties.put("Password","myPassword");
      
        LocalConverter
            .builder()
            .officeManager(manager)
            .loadProperties(loadProperties)
            .build()
            .convert(in)
            .to(out)
            .execute();
        
    } catch(Exception e) {
        e.printStackTrace();
    } finally {
        OfficeUtils.stopQuietly(manager);
    }
    ```

## Troubleshooting

??? question "When converting from format X to Y, something in the output is not quite right. What happened?"

    As mentioned, JODConverter simply automates OOo conversions. 90% of the problems reported on the JODConverter
    issues page are actually OOo problems. Please try performing a manual conversion using OOo alone. If you still
    get the same incorrect result, then it's clearly an OOo issue. (Note that the reverse is not true. If you get
    different results that doesn't mean it's clearly a JODConverter issue: it may still be an OOo issue that
    affects only its headless mode.)

??? question "So if it is an OpenOffice.org issue and not a JODConverter one, where can I ask for more help?"

    The OpenOffice.org Forums is probably the best place to start. If you are sure it is a bug then you can report
    it as such using the OOo issue tracker.

??? question "When converting to HTML using the webapp, images are missing. Why is that?"

    HTML is a bit special. Converting to most other formats results in a single output file, but when converting to
    HTML OOo generates multiple files: one HTML file plus various image files (OOo puts them in the same directory as
    the HTML one). The sample webapp has no special support for HTML output (in fact its form page doesn't list HTML as
    an option), it just does what works for other formats i.e. returns a single file: the HTML one. Hence images are
    lost. The webapp does not even attempt to provide a solution for this problem, because the exact solution depends
    on your particular requirements. In some cases you may want to package HTML and images into a ZIP file in order
    to return a single file. In other cases you may want to copy HTML and images to a public path on your web server to
    access them directly. In all cases you should think about security implications. It's up to you.

--8<-- "note.md"
