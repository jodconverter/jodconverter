This guide discusses migration from JODConverter version 4.4.11 to version 5.0.0

!!! note

    JODConverter 5.0.0 is not released yet. This guide follows the `develop` branch and is completed as the
    changes are merged.

## Background

JODConverter 5.0 is a major version: it moves to Java 17, Spring Boot 3 and SLF4J 2, and it is the occasion to fix
a few long-standing issues whose fix changes a behavior. Most applications only have to update their Java, Spring
Boot and logging setup; the API changes only affect code that extends JODConverter.

## Requirements

### Java 17

JODConverter 5.0 requires Java 17 or later, at build time and at runtime. Applications that must stay on Java 8 or
11 must stay on JODConverter 4.4.

### Spring Boot 3 and Spring Framework 6

- `jodconverter-spring-boot-starter` requires Spring Boot 3. It is built and tested with Spring Boot 3.5.
- `jodconverter-spring` requires Spring Framework 6. It is built and tested with Spring Framework 6.2.

Spring Boot 2 and Spring Framework 5 applications must stay on JODConverter 4.4. The migration of the application
itself (`javax.*` to `jakarta.*`, and so on) is covered by the
[Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide).

The auto-configurations of the starter are registered in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; the `META-INF/spring.factories`
file, which Spring Boot 3 ignores, is gone.

## Logging

### SLF4J 2

JODConverter logs through SLF4J 2 (`slf4j-api` 2.0) instead of SLF4J 1.7. An application that logs through an SLF4J
1.7 binding (`slf4j-log4j12`, `slf4j-reload4j`, `log4j-slf4j-impl`, Logback 1.2...) needs the SLF4J 2 version of its
provider, for example:

| Backend  | SLF4J 1.7 binding                           | SLF4J 2 provider                                |
| -------- | ------------------------------------------- | ----------------------------------------------- |
| Log4j 2  | `org.apache.logging.log4j:log4j-slf4j-impl` | `org.apache.logging.log4j:log4j-slf4j2-impl`    |
| Logback  | `ch.qos.logback:logback-classic` 1.2.x      | `ch.qos.logback:logback-classic` 1.3.x or later |
| reload4j | `org.slf4j:slf4j-reload4j` 1.7.x            | `org.slf4j:slf4j-reload4j` 2.0.x                |

Otherwise, SLF4J prints `No SLF4J providers were found` and JODConverter logs nothing. Spring Boot 3 applications
are not affected: Spring Boot already uses SLF4J 2.

### No logging backend from jodconverter-spring

`jodconverter-spring` declared the SLF4J log4j 1 binding as a runtime dependency, so every application using it got
log4j 1 on its classpath. It now only depends on `slf4j-api`, like the other modules: an application that relied on
that binding without declaring it must now add a logging backend of its own.

### Command line tool

The command line tool logs through Log4j 2. Its configuration file is now `conf/log4j2.xml` instead of
`conf/log4j.properties` (same default: errors only, on the standard output). On Linux and macOS, the tool now
actually loads that file: the start script did not expand the installation directory, so the previous versions
ignored their `conf/log4j.properties`.

## Dependencies

The published POMs and Gradle module metadata of `jodconverter-core`, `jodconverter-local` and `jodconverter-remote`
no longer import the Spring Boot BOM (`spring-boot-dependencies`), and `jodconverter-spring` no longer imports it
either. Each dependency has its own version instead. Only `jodconverter-spring-boot-starter` uses the Spring Boot
BOM.

An application that relied on JODConverter to bring Spring Boot's dependency management, without importing the
Spring Boot BOM itself, may now resolve different versions of the libraries that BOM used to manage.

## API changes

These changes only affect code that extends or calls these classes directly.

### Records

`ProcessQuery` (local module) and `RequestConfig` (remote module) are now records, and their accessors follow the
record naming:

| Class                                          | 4.4                                                     | 5.0                                            |
| ---------------------------------------------- | ------------------------------------------------------- | ---------------------------------------------- |
| `org.jodconverter.local.process.ProcessQuery`  | `getCommand()`, `getArgument()`                         | `command()`, `argument()`                      |
| `org.jodconverter.remote.office.RequestConfig` | `getUrl()`, `getConnectTimeout()`, `getSocketTimeout()` | `url()`, `connectTimeout()`, `socketTimeout()` |

A custom `ProcessManager` that reads the query in `findPid` must be updated:

```java
@Override
public long findPid(final ProcessQuery query) throws IOException {
  // 4.4: query.getCommand() and query.getArgument()
  final String command = query.command();
  final String argument = query.argument();
  ...
}
```

### Unmodifiable collections

- `LocalConverter.DEFAULT_LOAD_PROPERTIES` is still unmodifiable, but its iteration order is no longer fixed, and
    looking up a `null` key throws a `NullPointerException`.
- The property maps of a `DocumentFormat` (load and store properties) reject `null` keys and values.
- `LocalOfficeManager.Builder.runAsArgs(...)` no longer accepts `null` elements.

### FileUtils.readFileToString

`org.jodconverter.core.util.FileUtils.readFileToString` now throws a `MalformedInputException` when the file contains
bytes that are not valid in the given charset, instead of replacing them.

## Behavior changes

### Numeric properties in JSON document format registries

Before 5.0, every number of a property map in a JSON document format registry was read as a `Double`, and
LibreOffice silently ignores a property of an integer type given as a double. So numeric options in a custom
`document-formats.json`, such as `SelectPdfVersion`, `Quality` or `MaxImageResolution`, had no effect.

They are now applied: a whole number is read as an `Integer` (or a `Long` when it does not fit), and a number with a
fraction or an exponent as a `Double`. Check that the numeric options of a custom registry give the expected
result, since they were ignored until now. The bundled registry has no numeric property.

### Remote conversions send the source format's load properties

The remote converter sent the load properties of the **target** format (the `l…` request parameters). It now sends
the load properties of the **source** format, as the local converter does. This only matters for servers that
support custom properties, such as the JODConverter sample REST service; LibreOffice Online and Collabora ignore
them.

### Office temporary files

The office processes started by a `LocalOfficeManager` now write their temporary files in a `tmp` folder of their
instance profile directory (`<workingDir>/.jodconverter_<connection>/tmp`) instead of the system temporary
directory, so they are deleted with the profile even when a process is killed. JODConverter sets `TMPDIR` (Linux,
macOS) and `TMP`/`TEMP` (Windows) for that.

An application that set these variables to move the office temporary files elsewhere (a tmpfs, for example) should
set the `workingDir` of the office manager to that place instead.

### Office port used by another program

When the port of a local office process is already used by another program, `start()` now fails right away with a
`Port X on host 'Y' is already used by another program` error, instead of hanging until the process timeout. With
`startFailFast` set to `false`, the error is logged.

### Lost connections and timed-out tasks

- After an unexpected loss of connection, an office process that is still running gets at most 2 seconds to exit by
    itself before it is killed and restarted, instead of the whole process timeout (2 minutes by default).
- A task that exceeds the task execution timeout, or whose calling thread is interrupted, is now cancelled. Before,
    it could keep the office process busy, and every following task waited behind it.
- An interrupted calling thread no longer makes the pool lose an office manager.

### Spring Boot starter

The `jodconverter.local.port-numbers` property no longer has a default value in the properties class, so that it
does not conflict with the new `jodconverter.local.pool-size` property. The effective default is unchanged: with no
port numbers, pipe names or pool size, the office process uses port 2002.

### Command line tool

The `--help` output now uses the new commons-cli help formatter: the options are printed as a table, up to 120
columns wide.

## New features

- [`PdfOptions`](../getting-started/pdf-options.md): typed options to convert to PDF (PDF/A, PDF/UA, images, page
    range, passwords and permissions, watermark, digital signature...), given to a conversion with the new
    `with(...)` method: `converter.convert(source).to(target).with(PdfOptions.archive()).execute()`. The command line
    tool takes them with its new `--pdf-preset` and `--pdf-option` arguments, a converter can apply them to all its
    conversions with `defaultTargetOptions(...)`, and the Spring Boot starter sets them with the `jodconverter.pdf.*`
    properties.
- [`poolSize`](../configuration/local-manager.md): start a number of office processes on free ports, without choosing
    them (`jodconverter.local.pool-size` with Spring Boot).
- [`officeExecutable`](../configuration/local-manager.md): start the office processes through a launcher, such as a
    snap or an AppImage (`jodconverter.local.office-executable` with Spring Boot).
- [External office manager in the Spring Boot starter](../configuration/external-manager.md#spring-boot): the
    `jodconverter.external.*` properties auto-configure an `ExternalOfficeManager` and its converter.
- [Markdown](../getting-started/supported-formats.md): the default registry knows the Markdown format (`md`,
    `markdown`), supported by LibreOffice 26.2 and later.
- `AbstractOfficeWorkerPool.getTempDir()`: the directory where an office manager creates the temporary files used by
    conversions.
