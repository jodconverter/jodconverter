# Samples

The sample applications live in their own repository,
[jodconverter/jodconverter-samples](https://github.com/jodconverter/jodconverter-samples), where they are built and
tested with each change, against the latest release of **JODConverter**. Each one has a README that says how to run
it and what it shows.

| Sample                                                                                                              | What it shows                                                                                                                                                                                                                                                  |
| ------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [basic-webapp](https://github.com/jodconverter/jodconverter-samples/tree/main/samples/basic-webapp)                 | A plain Jakarta Servlet application, without a framework: a `LocalOfficeManager` started and stopped with the application, and a servlet that converts the uploaded documents.                                                                                 |
| [spring-boot-webapp](https://github.com/jodconverter/jodconverter-samples/tree/main/samples/spring-boot-webapp)     | A web page with the [Spring Boot starter](../getting-started/modules.md#jodconverter-spring-boot-starter): conversions with filters, PDF options and passwords.                                                                                                |
| [spring-boot-rest](https://github.com/jodconverter/jodconverter-samples/tree/main/samples/spring-boot-rest)         | A REST API with the Spring Boot starter: conversions with load and store properties, a description of the office, previews of the first page and [images of the slides](../getting-started/page-images.md), and the health and meters of the office processes. |
| [spring-boot-remote](https://github.com/jodconverter/jodconverter-samples/tree/main/samples/spring-boot-remote)     | Conversions sent to a Collabora Online server with the [remote module](../getting-started/libreoffice-online.md): the application has no office of its own.                                                                                                    |
| [spring-boot-attached](https://github.com/jodconverter/jodconverter-samples/tree/main/samples/spring-boot-attached) | Conversions with a LibreOffice that runs in a container, which the application does not start: the [AttachedOfficeManager](../configuration/attached-manager.md), with the documents sent through the connection.                                              |

The samples need Java 17 or later. All of them but the remote one need LibreOffice, installed on the machine or, for
the attached sample, in its container; the remote and attached samples need Docker for their conversion server or
office. To run one, from the root of the repository:

```shell
./gradlew :samples:spring-boot-rest:bootRun
```

--8<-- "note.md"
