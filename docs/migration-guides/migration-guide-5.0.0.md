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

### Spring Boot 3

`jodconverter-spring-boot-starter` requires Spring Boot 3. It is built and tested with Spring Boot 3.5. Spring Boot 2
applications must stay on JODConverter 4.4. The migration of the application itself (`javax.*` to `jakarta.*`, and so
on) is covered by the
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

### Command line tool

The command line tool logs through Log4j 2. Its configuration file is now `conf/log4j2.xml` instead of
`conf/log4j.properties` (same default: errors only, on the standard output). On Linux and macOS, the tool now
actually loads that file: the start script did not expand the installation directory, so the previous versions
ignored their `conf/log4j.properties`.

## Dependencies

The published POMs and Gradle module metadata of `jodconverter-core`, `jodconverter-local` and `jodconverter-remote`
no longer import the Spring Boot BOM (`spring-boot-dependencies`). Each dependency has its own version instead. Only
`jodconverter-spring-boot-starter` uses the Spring Boot BOM.

An application that relied on JODConverter to bring Spring Boot's dependency management, without importing the
Spring Boot BOM itself, may now resolve different versions of the libraries that BOM used to manage.

The command line tool no longer depends on the Spring Framework: its distribution ships Gson and SnakeYAML Engine
instead of the Spring jars, for its configuration file.

## API changes

These changes only affect code that extends or calls these classes directly.

### jodconverter-spring removed

The `jodconverter-spring` module and its `JodConverterBean` are gone. It configured the local office manager only,
with setters, and lagged the Spring Boot starter on every feature. A Spring Framework application declares the manager
and the converter as beans instead; the builders do the rest:

```java
@Bean(initMethod = "start", destroyMethod = "stop")
public OfficeManager officeManager() {
  return LocalOfficeManager.builder()
      .portNumbers(2002)
      .taskExecutionTimeout(120_000L)
      .build();
}

@Bean
public DocumentConverter documentConverter(OfficeManager officeManager) {
  return LocalConverter.make(officeManager);
}
```

Each setter of `JodConverterBean` has the builder method of the same name on `LocalOfficeManager.Builder` (the
`portNumbers` setter took a comma-separated string; the builder takes `int...`). With XML configuration, use the
static `LocalOfficeManager.make()` and `LocalConverter.make(officeManager)` factory methods with `factory-method`,
`init-method="start"` and `destroy-method="stop"`. Spring Boot applications are not affected: the starter never
used this module.

### Process management

The office processes started by JODConverter are followed through their `ProcessHandle`: their pid is known as soon
as they start, and they are killed, with their descendants, through the handle. A `ProcessManager` is only asked to
find a process that JODConverter did not start (the `existingProcessAction` check), and its contract changes
accordingly: `canFindPid()`, `findPid(ProcessQuery)` with the `PID_UNKNOWN` and `PID_NOT_FOUND` sentinels, and
`kill(Process, long)` are replaced by `Optional<ProcessHandle> find(ProcessQuery)` and `kill(ProcessHandle)`, the
latter with a default implementation. `MacProcessManager` and `FreeBSDProcessManager` are gone: `UnixProcessManager`
reads the command lines through the JVM on every Unix system. `WindowsProcessManager` lists the processes with
PowerShell only (`wmic` is no longer used), and `AbstractProcessManager` stays for the managers that run a command to
list them. `UnixProcessManager.setRunAsArgs` (unused), `ExitCodeRetryable` and the four stream pumper classes of the
`process` package are removed.

### Remote module without Apache HttpClient

`jodconverter-remote` sends its requests with the HTTP client of the JDK (`java.net.http`) and no longer depends on
Apache HttpClient: the `httpclient`, `httpcore`, `httpmime` and `fluent-hc` artifacts are gone from its POM. The
client of a worker is built once, when the manager starts, with the SSL material loaded once; a task that times out
is aborted by cancelling its request, not by closing a client.

- `RemoteOfficeContext.getHttpClient()` returns a `java.net.http.HttpClient`, and the context has a `send(request,
    handler)` method that a custom task uses to send its requests, so that they are cancelled when the task is
    aborted. `RemoteOfficeConnection` takes that client and a `RequestConfig`.
- `RemoteOfficeManager.Builder` validates `urlConnection` in `build()` (an `IllegalArgumentException` for an invalid
    URL, instead of a failure at the first conversion), builds the URL of the `convert-to` service once, and
    recognizes the `cool` directory of Collabora Online as well as `lool`; `poolSize` must be at least 1; the
    `connectTimeout` and `socketTimeout` setters no longer document a system default for negative values, which they
    reject. `socketTimeout` is the timeout for the response of the server once a request is sent.
- The new `sslContext(SSLContext)` setter takes an SSL context built by the application, which takes precedence over
    `sslConfig(SslConfig)`. The `org.jodconverter.remote.ssl.SslContexts` utility builds an `SSLContext` and its
    `SSLParameters` from an `SslConfig`; a `classpath:` key store or trust store is read as a class path resource, so
    it can live inside a jar. The SSL material is loaded when the manager starts: a wrong key password or an unknown
    protocol, cipher suite or enabled protocol fails `start()`, with the cause as the root cause of the
    `OfficeException`, instead of failing every conversion. A host name mismatch is reported as an
    `SSLHandshakeException`, like any other handshake failure.

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

### Office manager pool

The pool of office processes was rewritten. The classes of the previous pool, `AbstractOfficeManagerPool`,
`AbstractOfficeManagerPoolEntry` and `SuspendableThreadPoolExecutor`, are removed. An office manager that extended
them now extends `AbstractOfficeWorkerPool` and gives it its workers, one per office process or connection, each
implementing `OfficeWorker` (start, restart, is-ready, execute, abort, stop). The three office managers of
JODConverter are built this way.

- `OfficeManager` has a new `submit(task)` method, with a default implementation that executes the task before
    returning: a custom office manager keeps compiling and behaving as before.
- `ConversionJob` has a new abstract `executeAsync()` method, and `AbstractConversionJob` asks its subclasses for
    `getOfficeManager()` and `createTask()` instead of a `doExecute()` implementation.

### Removed deprecated methods

`Lo.createInstanceMSF(...)` and `Lo.createInstanceMCF(...)` (local module), deprecated since 4.4.4, are removed; use the
`Lo.createInstance(...)` overloads, which take the same arguments.

### Office managers make temporary files

`OfficeManager` now extends `TemporaryFileMaker`, which every office manager of JODConverter already implemented, so a
converter no longer checks for it at runtime before converting a stream. A custom office manager must implement
`makeTemporaryFile(String extension)` (the no-argument overload has a default implementation).

### Immutable document formats

`DocumentFormat` is always immutable: `DocumentFormat.copy(format)`, `unmodifiableCopy(format)` and
`Builder.unmodifiable(boolean)` are gone; `DocumentFormat.builder(format)` gives a builder initialized from a format.
`getLoadProperties()` and `getStoreProperties()` return empty maps instead of `null` when the format has no property,
and `getStoreProperties(family)` still returns `null` for a family without properties. A format needs at least one
extension, and `equals`/`hashCode` compare every field. `DefaultDocumentFormatRegistry` keeps only `getInstance()`:
`getFormatByExtension`, `getFormatByMediaType` and `getOutputFormats` are called on that instance. A custom
`document-formats.json` that drops one of the formats named by the `DefaultDocumentFormatRegistry` constants now fails
when the class is loaded, instead of giving a silent placeholder format.

### Builder setters take primitives

The numeric and boolean setters of the office manager builders (`taskExecutionTimeout`, `taskQueueTimeout`,
`taskQueueCapacity`, `poolSize`, `processTimeout`, `processRetryInterval`, `afterStartProcessDelay`, `startFailFast`,
`keepAliveOnShutdown`, `maxTasksPerProcess`, `connectOnStart`, `connectTimeout`, `connectRetryInterval`,
`connectFailFast`, `maxTasksPerConnection`, `socketTimeout`) take `long`, `int` or `boolean` instead of nullable
wrappers that kept the default on `null`. A caller that passed a possibly null value keeps the default by not calling
the setter.

### Removed utilities

In `org.jodconverter.core.util`, `IOUtils` is removed (`InputStream.transferTo` and `readAllBytes`),
`FileUtils.copyFile`, `copyFileToDirectory` and `readFileToString` are removed (`Files.copy` and `Files.readString`),
`StringUtils.isEmpty`, `isNotEmpty`, `appendIfMissing` and `endsWithAny` are removed, `AssertUtils.notNull` is removed
(`Objects.requireNonNull`), and `OSUtils` keeps `IS_OS_FREE_BSD`, `IS_OS_MAC`, `IS_OS_UNIX` and `IS_OS_WINDOWS` only.

In `org.jodconverter.local.office.utils`, the `Calc` and `Draw` classes, `Write.isWeb` and `Info.getConfigUpdateAccess`
are removed: nothing in the project used them, and `LocalOfficeUtils.getDocumentFamily` tells the kind of a document.

### Document indexes updater filter

`TableOfContentUpdaterFilter` is renamed `DocumentIndexesUpdaterFilter`, since it updates every index of a text document
(table of contents, alphabetical index, table of figures, bibliography) and not only the table of contents. It now
refreshes the document and updates the indexes twice, so that the page numbers are right after a table of contents
grew. The old class remains as a deprecated subclass, to be removed in a later release, and the command line tool
accepts both `document-indexes-updater` and `table-of-content-updater` as the filter type.

### Internal classes

`NamedThreadFactory` (core), `CliConverter` (command line tool) and the `MIN_PROCESS_RETRY_INTERVAL` and
`MIN_AFTER_START_PROCESS_DELAY` constants of `LocalOfficeManager` are no longer public, and the constructors of
`AbstractOfficeTask` and `AbstractRemoteOfficeTask` are protected: they were never meant to be used outside the
project.

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

### No search for the pid of a started office process

A started office process no longer needs to be found in the list of the running processes: the `ps`, `wmic` or
PowerShell commands that ran after each start are gone, with the retries, the FreeBSD delay and the restart that
followed a pid that could not be found. `afterStartProcessDelay` is still honored. On Windows, `soffice.bin` is
found behind the `soffice.exe` launcher through the process tree, and killed with it.

### Office port used by another program

When the port of a local office process is already used by another program, `start()` now fails right away with a
`Port X on host 'Y' is already used by another program` error, instead of hanging until the process timeout. With
`startFailFast` set to `false`, the error is logged.

### Office manager pool

The tasks of an office manager wait in a single queue, and an office process only takes a task when it is ready: a
process that is starting or restarting is never given a task while another one is free, and a task submitted while a
process starts waits in the queue instead of failing with the execution timeout.

- `taskQueueTimeout` runs from the submission of a task until an office process takes it, so it includes the wait for a
    process to be ready; `taskExecutionTimeout` runs from the moment the process starts the task. Before, the wait for
    a process could count against the execution timeout.
- With `startFailFast` (or `connectFailFast`) set to `false`, an office process that cannot be started is retried with
    a growing delay instead of being logged once; set to `true`, a manager whose start fails is shut down for good.
- A task that exceeds the task execution timeout, or whose calling thread is interrupted, is now cancelled: its office
    process is killed and restarted. Before, the task could keep the process busy, and every following task waited
    behind it. The exception keeps its `TimeoutException` cause.
- After an unexpected loss of connection, an office process that is still running gets at most 2 seconds to exit by
    itself before it is killed and restarted, instead of the whole process timeout (2 minutes by default).
- When the manager is stopped, an idle office process is asked to terminate as before, but a process that is executing
    a task is killed, even with `keepAliveOnShutdown`.
- The restarts of an office process are visible: see `getStatus()` under the new features.

### Spring Boot starter

The `jodconverter.local.port-numbers` property no longer has a default value in the properties class, so that it
does not conflict with the new `jodconverter.local.pool-size` property. The effective default is unchanged: with no
port numbers, pipe names or pool size, the office process uses port 2002.

The timeouts and delays of the starter (`task-queue-timeout`, `task-execution-timeout`, `process-timeout`,
`process-retry-interval`, `after-start-process-delay`, `connect-timeout`, `connect-retry-interval`, `socket-timeout`)
are bound as `Duration`: a plain number is still a number of milliseconds, and a duration such as `30s` or `2m` is
accepted. `existing-process-action` and `load-document-mode` are bound to the `ExistingProcessAction` and
`LoadDocumentMode` enums, in any case and with hyphens or underscores (`connect-or-kill`).
`JodConverterLocalProperties`, `JodConverterExternalProperties`, `JodConverterRemoteProperties` and the new
`JodConverterDocumentFormatsProperties` are records, bound through their constructor: an application that injects
one reads a value with the accessor of the component (`properties.processTimeout()`), not a getter, and the setters are
gone. The properties shared by the three managers (`working-dir`, `task-queue-capacity`, `task-queue-timeout`,
`task-execution-timeout`) are declared by the `JodConverterPoolProperties` interface. `ExternalOfficeManager`'s
`DEFAULT_*` constants are public, like those of the other managers. The `jodconverter.remote.ssl.*` properties are bound
directly to an `SslConfig`: `JodConverterRemoteProperties.getSsl()` returns one, and the `SslProperties` copy of its
fields is gone (same property names, same defaults).

The document format registry is configured once for every converter of the starter, under
`jodconverter.document-formats`: `jodconverter.local.document-format-registry` becomes
`jodconverter.document-formats.registry`, and `jodconverter.local.format-options` becomes
`jodconverter.document-formats.options`. The `documentFormatRegistry` bean exists whether or not the local manager is
enabled, and the external and remote converters use it too (the remote converter used to ignore it). An application
that declares its own `DocumentFormatRegistry` bean, of any name, replaces it.

### Command line tool

The `-a` / `--application-context` option, which loaded a Spring XML context for the filter chain and the SSL
configuration, is replaced by `--config <file>`, a JSON or YAML file with a `filters` section and an `ssl` section.
The built-in filters are named by `type` with their own keys, and a custom filter by `class`, which needs a public
no-argument constructor:

```yaml
ssl:
  enabled: true
  trust-store: /path/to/truststore.p12
  trust-store-password: secret
filters:
  - type: pages-selector
    pages: [2]
  - type: graphic-inserter
    image: /path/to/image.jpg
    horizontal-position: 50
    vertical-position: 111
  - class: com.example.MyFilter
```

The keys of the `ssl` section are the properties of `SslConfig` in kebab case; see the
[command line tool](../getting-started/command-line-tool.md#configuration-file) page for the filters and their keys.
A filter chain that needed Spring to wire constructor arguments beyond these keys becomes a custom filter class.

An unknown option, like a missing file name, now exits with status 255 (invalid arguments) instead of 2 (error).

The `--help` output now uses the new commons-cli help formatter: the options are printed as a table, up to 120
columns wide.

## New features

- [`PdfOptions`](../getting-started/pdf-options.md): typed options to convert to PDF (PDF/A, PDF/UA, images, page
    range, passwords and permissions, watermark, digital signature...), given to a conversion with the new
    `with(...)` method: `converter.convert(source).to(target).with(PdfOptions.archive()).execute()`. The command line
    tool takes them with its new `--pdf-preset` and `--pdf-option` arguments, a converter can apply them to all its
    conversions with `defaultTargetOptions(...)`, and the Spring Boot starter sets them with the `jodconverter.pdf.*`
    properties. The builder also takes an option by its name and its text value, as written on the command line
    (`option("images.jpeg-quality", "80")`), to apply options read from a configuration file of your own.
- [`poolSize`](../configuration/local-manager.md): start a number of office processes on free ports, without choosing
    them (`jodconverter.local.pool-size` with Spring Boot).
- [`taskQueueCapacity`](../configuration/local-manager.md): bound the conversion queue, so that a task submitted while
    the queue is full fails at once (`jodconverter.local.task-queue-capacity`, and the same for the external and
    remote managers, with Spring Boot).
- [`officeExecutable`](../configuration/local-manager.md): start the office processes through a launcher, such as a
    snap or an AppImage (`jodconverter.local.office-executable` with Spring Boot).
- [External office manager in the Spring Boot starter](../configuration/external-manager.md#spring-boot): the
    `jodconverter.external.*` properties auto-configure an `ExternalOfficeManager` and its converter.
- [Markdown](../getting-started/supported-formats.md): the default registry knows the Markdown format (`md`,
    `markdown`), supported by LibreOffice 26.2 and later.
- [Asynchronous conversions](../getting-started/document-converters.md#lifecycle-and-threading):
    `converter.convert(source).to(target).executeAsync()` returns a `CompletableFuture<Void>` instead of blocking, and
    `OfficeManager.submit(task)` does the same for an `OfficeTask`.
- [`AbstractOfficeWorkerPool.getStatus()`](../getting-started/office-managers.md): a snapshot of the office processes
    (state, tasks executed, restarts, failed start attempts) and of the queue; with Spring Boot Actuator, the
    `jodconverter` health indicator of the starter reports it.
