# Modules

This page provides an overview of the JODConverter project modules and when to use each one. If you’re new here, start
with the Java Library page and come back for details.

- Java Library: [Java Library](java-library/index.md)
- Command Line Tool: [Command Line Tool](command-line-tool.md)

______________________________________________________________________

## jodconverter-cli

The `jodconverter-cli` module provides a standalone command-line tool for converting documents using LibreOffice or
Apache OpenOffice. It enables quick and easy file conversions without writing any Java code, making it ideal for
automation, scripting, and server-side integrations.

See the dedicated page: [Command Line Tool](command-line-tool.md)

## jodconverter-core

[Dependencies](https://central.sonatype.com/artifact/org.jodconverter/jodconverter-core/5.0.1/dependencies)

The `jodconverter-core` module provides the core abstractions, used by JODConverter module implementations,
such as `jodconverter-local` or `jodconverter-remote`. It abstracts the complexity of working with office managers,
document formats, and conversion pipelines.

## jodconverter-local

[Dependencies](https://central.sonatype.com/artifact/org.jodconverter/jodconverter-local/5.0.1/dependencies)

The `jodconverter-local` module builds on top of the `jodconverter-core` module and provides a ready-to-use
implementation that connects to a locally installed instance of LibreOffice or Apache OpenOffice to perform document
conversions.

This module handles the lifecycle of the office process, manages one or more office instances, and provides a
convenient API to convert documents using the local desktop installation of an office suite—without requiring
the user to manually start or manage OOo in headless mode.

See also: [Java Library](java-library/index.md)

## jodconverter-local-oo

[Dependencies](https://central.sonatype.com/artifact/org.jodconverter/jodconverter-local-oo/5.0.1/dependencies)

The `jodconverter-local-oo` module provides a variant of `jodconverter-local` that is packaged with dependencies
targeting Apache OpenOffice. It contains no additional code, but includes the OpenOffice UNO libraries instead of
the LibreOffice ones.

Use this module when your application needs to convert documents using an Apache OpenOffice installation.

Note: Internally, both `jodconverter-local-oo` and `jodconverter-local-lo` delegate to the same codebase (
`jodconverter-local`). The only difference lies in the dependencies declared in their build files. Using this module is
in fact the exact same thing as using the `jodconverter-local` module. By default, JODConverter is built using the
OpenOffice libraries. See https://github.com/jodconverter/jodconverter/issues/113 to know why.

## jodconverter-local-lo

[Dependencies](https://central.sonatype.com/artifact/org.jodconverter/jodconverter-local-lo/5.0.1/dependencies)

The `jodconverter-local-lo` module is a variant of `jodconverter-local` that packages the project with LibreOffice UNO
libraries instead of the default Apache OpenOffice ones. Like `jodconverter-local-oo`, it contains no additional Java
code—only a different set of dependencies defined in its build.gradle.kts.

Use this module if your application is intended to run with LibreOffice, which is generally recommended due to its more
active development and broader format support.

## jodconverter-remote

[Dependencies](https://central.sonatype.com/artifact/org.jodconverter/jodconverter-remote/5.0.1/dependencies)

The `jodconverter-remote` module is a Java client library designed to perform document conversions by connecting to a
remote document conversion REST API, such as those exposed by LibreOffice Online or Collabora Online.

Instead of managing a local or UNO-based office process, this module sends documents and conversion requests over
HTTP(S) to a remote server that handles the conversion, making it ideal for cloud-native or containerized environments.

## Spring Framework without Spring Boot

There is no module for the Spring Framework alone (the `jodconverter-spring` module of 4.x is gone in 5.0): the
builders of the office managers and converters are all a Spring application needs. Declare the manager as a bean
started and stopped with the context, and a converter on top of it:

```java
@Configuration
public class JodConverterConfiguration {

  @Bean(initMethod = "start", destroyMethod = "stop")
  public OfficeManager officeManager() {
    return LocalOfficeManager.builder().portNumbers(2002, 2003).build();
  }

  @Bean
  public DocumentConverter documentConverter(OfficeManager officeManager) {
    return LocalConverter.make(officeManager);
  }
}
```

With XML configuration, `LocalOfficeManager.make()` and `LocalConverter.make(officeManager)` are static factory
methods: `<bean id="officeManager" class="org.jodconverter.local.office.LocalOfficeManager" factory-method="make" init-method="start" destroy-method="stop"/>`.

## jodconverter-spring-boot-starter

[Dependencies](https://central.sonatype.com/artifact/org.jodconverter/jodconverter-spring-boot-starter/5.0.1/dependencies)

The `jodconverter-spring-boot-starter` module provides a convenient Spring Boot starter that simplifies integrating
JODConverter’s document conversion capabilities into Spring Boot applications.

It auto-configures and manages all necessary beans, including the office manager and document converter, based on
sensible defaults and externalized configuration properties, allowing developers to quickly enable document conversion
with minimal setup.

______________________________________________________________________

## Choosing a module

- Prefer jodconverter-local-lo when targeting LibreOffice (recommended for broader support).
- Use jodconverter-local-oo if you must target Apache OpenOffice.
- Use jodconverter-remote if you rely on a remote conversion service (LO Online/Collabora).
- Use jodconverter-cli for one-off or scripted conversions without Java coding.
- Add jodconverter-spring-boot-starter for Spring Boot apps.

--8<-- "note.md"
