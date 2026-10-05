# ExternalOfficeManager Configuration

The [ExternalOfficeManager](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/office/ExternalOfficeManager.java)
is the manager to use when you want to connect to an already started office process that is not managed
by **JODConverter**.

A `ExternalOfficeManager` is built using a builder:

```java
OfficeManager officeManager = ExternalOfficeManager.builder().build();
```

## Spring Boot

With the Spring Boot starter, set `jodconverter.external.enabled` to `true` to get an `ExternalOfficeManager` bean
named `externalOfficeManager`, and a `DocumentConverter` bean named `externalDocumentConverter` that converts through
it. Each property below matches the builder property of the same name; the values shown are the defaults:

```yml title="application.yml"
jodconverter:
  external:
    enabled: true
    host-name: 127.0.0.1
    port-numbers: 2002              # or pipe-names / websocket-urls
    working-dir:                    # defaults to java.io.tmpdir
    connect-on-start: true
    connect-timeout: 120000
    connect-retry-interval: 250
    connect-fail-fast: false
    max-tasks-per-connection: 1000
    task-queue-timeout: 30000
    task-execution-timeout: 120000
    apply-default-load-properties: true
    load-document-mode: auto        # remote: stream documents to an office process on another host
```

When the office processes run in another container or on another host, set `load-document-mode` to `remote`: the
documents are then sent through the connection instead of being read from (and written to) the local file system.
The local and external auto-configurations can be enabled together; inject the converter you need with
`@Qualifier("externalDocumentConverter")` or `@Qualifier("localDocumentConverter")`.

## Builder properties

Here are all the properties you can set through the builder:

!!! note

    **JODConverter** uses milliseconds for all time values.

#### 📁`workingDir`

This property is used to create a temporary directory where files will be created when conversions are done
using InputStream/OutputStream.

&#160;***Default***: The system temporary directory as specified by the `java.io.tmpdir` system property.

**NOTE** that
[some OS automatically clean up the `java.io.tmpdir` directory periodically](https://github.com/jodconverter/jodconverter/issues/220).
It is recommended to check your OS to see if you have to set this property to a directory that won't be deleted.

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .workingDir("C:\\jodconverter\\tmp")
        .build();
```

#### 🔠`hostName`

This property sets the host name that will be used in the `--accept` argument when connecting to an
office process.

&#160;***Default***: 127.0.0.1

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .hostName("localhost")
        .build();
```

#### 🔢`portNumbers` / 🔠`pipeNames` / 🔠`websocketUrls`

This property sets the port number(s), pipe name(s) and websocket urls that will be used in the `--accept` argument
when connecting to an office process.

If you want to know more about web socket, read the
[Pull Request](https://github.com/jodconverter/jodconverter/pull/355) where it has been introduced.

&#160;***Default***: TCP socket, on port 2002.

=== "Java"

```java hl_lines="7 8"
// This example will use 4 TCP ports and 4 pipes, which will
// cause JODConverter to connect to 8 office processes (a bit excessive!)
// when the OfficeManager will be started.
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .portNumbers(2002, 2003, 2004, 2005)
        .pipeNames("Pipe1", "Pipe2", "Pipe3", "Pipe4")
        .build();
```

#### ❎`connectOnStart`

This property controls whether a connection must be attempted when the manager starts. If `false`, a connection will
only be attempted the first time a conversion task is executed.

&#160;***Default***: true.

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .connectOnStart(false)
        .build();
```

#### ⌚`connectTimeout`

This property sets the timeout, in milliseconds, after which a connection attempt will fail.

&#160;***Default***: 120000 (2 minutes)

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .connectTimeout(60000)
        .build();
```

#### ⌚`connectRetryInterval`

This property sets the delay, in milliseconds, between each try when trying to connect to the external OOo process.

&#160;***Default***: 250 (0.25 seconds)

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .connectRetryInterval(1000)
        .build();
```

#### ❎`connectFailFast`

This property controls whether the manager will "fail fast" if the connection to the external process fails. If set to
`true`, a connection attempt will wait for the task to be completed, and will throw an exception the connection to the
external process fails. If set to `false`, the task of connecting to the external process will be submitted and will
return immediately, meaning a faster starting process. Only error logs will be produced if anything goes wrong.

&#160;***Default***: false.

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .connectFailFast(true)
        .build();
```

#### 🔢`maxTasksPerConnection`

This property sets the maximum number of tasks an office process can execute before reconnecting to it. 0 means an
infinite number of tasks (will never reconnect).

&#160;***Default***: 1000

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .maxTasksPerConnection(500)
        .build();
```

#### ⌚`taskQueueTimeout`

This property is used to set the maximum living time of a task in the conversion queue. The task will be removed from
the queue if the waiting time is longer than this timeout and an `OfficeException` will be thrown.

&#160;***Default***: 30000 (30 seconds)

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .taskQueueTimeout(60000)
        .build();
```

#### ⌚`taskExecutionTimeout`

This property sets the maximum time allowed to process a task. If the processing time of a task is longer than this
timeout, this task will be aborted and the next task is processed.

&#160;***Default***: 120000 (2 minutes)

```java hl_lines="4"
OfficeManager officeManager =
    ExternalOfficeManager
        .builder()
        .taskExecutionTimeout(60000)
        .build();
```

--8<-- "note.md"
