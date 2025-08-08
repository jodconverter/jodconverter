This guide discusses migration from JODConverter version 4.4.4 to version 4.4.5

## New Local Converter Option.

There is a new option available when building a `LocalConverter`, named `loadDocumentMode`.

```java
final File inputFile = File("...")
final File outputFile = File("...")
final OfficeManager manager = ExternalOfficeManager
        .builder()
        .connectFailFast(true)
        .connectOnStart(true)
        .hostName("127.0.0.1")
        .portNumbers(8100)
        .build();
try {
    manager.start();
    LocalConverter.builder()
            .officeManager(manager)
            .loadDocumentMode(LoadDocumentMode.REMOTE)
            .build()
            .convert(inputFile)
            .to(outputFile)
            .execute();
} catch (OfficeException e) {
    e.printStackTrace();
} finally {
    OfficeUtils.stopQuietly(manager);
}
```

This new option has been introduced to support conversions using an OOo instance that doesn't share the same drives as the process running JODConverter. When `LoadDocumentMode.REMOTE` is set, local files will be converted to/from streams when loading and storing a document. This is usefull when using an `ExternalOfficeManager` to connect to an OOo instance running on another server or in a Docker container.