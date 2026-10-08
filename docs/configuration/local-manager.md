# LocalOfficeManager Configuration

The [LocalOfficeManager](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/office/LocalOfficeManager.java)
is the most common office manager used by the **JODConverter** users. It is the manager you must use when the OOo
installation lives on the same server as your application, hence the **`Local`** prefix.

When using a `LocalOfficeManager`, there are a number of settings that can be configured. Some of the default settings
used by **JODConverter** have been chosen because they have a greater chance of working out of the box, but they are
not necessarily the optimal ones.

A `LocalOfficeManager` is built using a builder:

```java
OfficeManager officeManager = LocalOfficeManager.builder().build();
```

Here are all the properties you can set through the builder:

!!! note

    **JODConverter** uses milliseconds for all time values.

#### 📁`officeHome`

This property sets the office home directory of the office installation that will be used to perform document
conversions.

&#160;***Default***: Auto-detected, starting with LibreOffice (over OpenOffice) and the most recent version. On
Linux, the directories named `libreoffice*` under `/usr/lib64`, `/usr/lib`, `/usr/local/lib64`, `/usr/local/lib` and
`/opt` are considered, so the packages of The Document Foundation (`/opt/libreoffice25.8`) are found whatever their
version; on Windows, the `LibreOffice*` directories of the program files.

=== "Java"

    ```java hl_lines="6"
    // This example will force JODConverter to use the OpenOffice 4
    // installation that can be found using the specified path.
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .officeHome("C:\\Program Files (x86)\\OpenOffice 4")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        office-home: C:/Program Files (x86)/OpenOffice 4
    ```

    ```conf title="application.properties"
    jodconverter.local.office-home = C:/Program Files (x86)/OpenOffice 4
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -i "C:/Program Files (x86)/OpenOffice 4" infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --office-home "C:/Program Files (x86)/OpenOffice 4" infile outfile
    ```

#### 📁`officeExecutable`

This property sets the program that starts the office processes, instead of the executable found in the office home
(`program/soffice.bin`). Use it when the office program must be started through a launcher, for example a snap or an
AppImage. When it is set, `officeHome` is not required.

&#160;***Default***: The executable of the office home.

!!! note "Snap"

    A snap must be started through its launcher (`/snap/bin/libreoffice`): its `soffice.bin` can't find its libraries
    outside of the snap sandbox. A snap also has its own private `/tmp` directory and can't read hidden folders directly
    in your home directory, so set the [workingDir](#workingdir) to a non-hidden folder in your home directory, for
    example `/home/me/jodconverter`, so that the office processes can read the files to convert. This setup has not been
    tested by the **JODConverter** team; please report how it works for you.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .officeExecutable("/snap/bin/libreoffice")
            .workingDir("/home/me/jodconverter")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        office-executable: /snap/bin/libreoffice
        working-dir: /home/me/jodconverter
    ```

    ```conf title="application.properties"
    jodconverter.local.office-executable = /snap/bin/libreoffice
    jodconverter.local.working-dir = /home/me/jodconverter
    ```

#### 📁`workingDir`

This property sets the directory where temporary office profile directories will be created. An office profile directory
is created per office process launched. This property will also be used to create a temporary directory where files will
be created when conversions are done using InputStream/OutputStream.

&#160;***Default***: The system temporary directory as specified by the `java.io.tmpdir` system property.

**NOTE** that
[some OS automatically clean up the `java.io.tmpdir` directory periodically](https://github.com/jodconverter/jodconverter/issues/220).
It is recommended to check your OS to see if you have to set this property to a directory that won't be deleted.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .workingDir("C:\\jodconverter\\tmp")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        working-dir: "C:/jodconverter/tmp"
    ```

    ```conf title="application.properties"
    jodconverter.local.working-dir = "C:/jodconverter/tmp"
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -w "C:/jodconverter/tmp" infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --working-dir "C:/jodconverter/tmp" infile outfile
    ```

#### 📁`templateProfileDir`

A `LocalOfficeManager` creates temporary profile directories for its OOo processes, to avoid interfering with e.g.,
another OOo instance being used by the user. Using this property, you can provide a template profile directory
containing customized settings. The `OfficeManager` will copy such a template directory to the temporary profile,
so OOo will use the same settings while still keeping the OOo instances separate.

A profile can be customized in OOo by selecting the Tools > Options menu item. Settings that may be worth customizing
for automated conversions include e.g.

- Load/Save > General: you may e.g., want to disable "Save URLs relative to Internet" for security reasons.
- Load/Save > Microsoft Office: these options affect conversions of embedded documents, e.g., an Excel table contained
    in a Word document. If not enabled, the embedded table will likely be lost when converting the Word document to
    another format.

&#160;***Default***: By default, this temporary profile will be a new one, created by OOo with its own default settings,
and relies on the [-nofirststartwizard](https://wiki.openoffice.org/wiki/Framework/Article/Command_Line_Arguments)
command line option.

!!! note

    The `-nofirststartwizard` switch is ignored by LibreOffice, see the
    [Command Line Parameters](https://help.libreoffice.org/latest/km/text/shared/guide/start_parameters.html)
    documentation.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .templateProfileDir("C:\\jodconverter\\templateProfileDir")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        template-profile-dir: "C:/jodconverter/templateProfileDir"
    ```

    ```conf title="application.properties"
    jodconverter.local.template-profile-dir = "C:/jodconverter/templateProfileDir"
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -u C:\jodconverter\templateProfileDir infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --user-profile C:\jodconverter\templateProfileDir infile outfile
    ```

#### 🔠`hostName`

This property sets the host name that will be used in the `--accept` argument when starting an office process. Most of
the time, the default will work. But if it doesn't work (unable to connect to the started process), using `localhost`
instead of the default value may work.

&#160;***Default***: 127.0.0.1

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .hostName("localhost")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        host-name: localhost
    ```

    ```conf title="application.properties"
    jodconverter.local.host-name = localhost
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -n localhost infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --host-name localhost infile outfile
    ```

#### 🔢`portNumbers` / 🔠`pipeNames`

OOo inter-process communication can use either TCP sockets and/or named pipes. Named pipes have the advantage of not
taking up TCP ports (with their potential security implications), and they are marginally faster. However, they require
a native library to be loaded by the JVM, and this means having to set the `java.library.path` system property. That's
why it's not the default. The path that needs to be added to `java.library.path` is different depending on the platform,
but it should be the directory in the OOo installation containing libjpipe (or jpipe.dll).

- On Linux it's e.g.: java -Djava.library.path=/opt/openoffice.org/ure/lib
- On Windows it's e.g.: java "-Djava.library.path=C:\\Program Files (x86)\\OpenOffice 4\\program"

&#160;***Default***: TCP socket, on port 2002.

=== "Java"

    ```java hl_lines="7 8"
    // This example will use 4 TCP ports and 4 pipes, which will
    // cause JODConverter to start 8 office processes (a bit excessive!)
    // when the OfficeManager will be started.
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .portNumbers(2002, 2003, 2004, 2005)
            .pipeNames("Pipe1", "Pipe2", "Pipe3", "Pipe4")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        port-numbers: 2002, 2003, 2004, 2005
        pipe-names: Pipe1, Pipe2, Pipe3, Pipe4
    ```

    ```conf title="application.properties"
    jodconverter.local.port-numbers = 2002, 2003, 2004, 2005
    jodconverter.local.pipe-names = Pipe1, Pipe2, Pipe3, Pipe4
    ```

=== "Command Line"

    !!! note

        `pipeNames` can't be used with the command line tool, and only 1 port can be configured.

    ```shell title="short option"
    jodconverter-cli -p 2003 infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --port 2003 infile outfile
    ```

#### 🔢`poolSize`

The number of office processes to start, using free TCP ports picked when the manager is built. Use it when you only
care about how many conversions can run at the same time, not about which ports are used. It replaces `portNumbers` and
`pipeNames`, and can't be combined with them.

Two applications started at the same instant may pick the same free port. The office process started last then fails
to start, with an error saying that the port is already used by another program. If that can happen in your setup,
configure distinct port numbers or pipe names instead.

&#160;***Default***: not set (the processes come from `portNumbers` and `pipeNames`).

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .poolSize(4)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        pool-size: 4
    ```

    ```conf title="application.properties"
    jodconverter.local.pool-size = 4
    ```

#### 🔠`processManager`

A process manager finds an office process that already runs with the connection string **JODConverter** wants to
use (see [`existingProcessAction`](#existingprocessaction)), and kills office processes. The processes that
**JODConverter** starts itself are followed through their `ProcessHandle`, without the process manager.

&#160;***Default***: By default, **JODConverter** will try to find the best process manager according to the OS on
which **JODConverter** is running: on Linux, macOS and the other Unix systems, the running processes are read by the
JVM; on Windows, they are listed with a PowerShell query. But any process manager implementing the
[`ProcessManager`](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/process/ProcessManager.java)
interface (`find(ProcessQuery)`, and optionally `kill(ProcessHandle)`) can be used if found on the classpath.

=== "Java"

    ```java hl_lines="6"
    // This example will create an instance of the com.example.foo.CustomProcessManager
    // class that will be used by the created OfficeManager.
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .processManager("com.example.foo.CustomProcessManager")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        process-manager-class: com.example.foo.CustomProcessManager
    ```

    ```conf title="application.properties"
    jodconverter.local.process-manager-class = com.example.foo.CustomProcessManager
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -m com.example.foo.CustomProcessManager infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --process-manager com.example.foo.CustomProcessManager infile outfile
    ```

#### 🔠`runAsArgs`

This property specifies the sudo arguments that will be used with unix commands when **JODConverter** chooses
a unix process manager.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .runAsArgs("sudo")
            .build();
    ```

=== "Spring Boot"

    `runAsArgs` can't be set with the Spring Boot module.

=== "Command Line"

    `runAsArgs` can't be set with the command line tool.

#### ⌚`processTimeout`

This property sets the timeout, in milliseconds, when trying to execute an office process call (start/terminate).

&#160;***Default***: 120000 (2 minutes)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .processTimeout(60000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        process-timeout: 60000
    ```

    ```conf title="application.properties"
    jodconverter.local.process-timeout = 60000
    ```

=== "Command Line"

    `processTimeout` can't be set with the command line tool, it will always be 120000.

#### ⌚`processRetryInterval`

This property sets the delay, in milliseconds, between each try when trying to execute an office process call (
start/terminate).

&#160;***Default***: 250 (0.25 seconds)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .processRetryInterval(1000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        process-retry-interval: 1000
    ```

    ```conf title="application.properties"
    jodconverter.local.process-retry-interval = 1000
    ```

=== "Command Line"

    `processRetryInterval` can't be set with the command line tool, it will always be 250.

#### ⌚`afterStartProcessDelay`

This property specifies the delay, in milliseconds, after an attempt to start an office process before doing anything
else. It is required on some OS to avoid an attempt to connect to the started process that will hang for more than 5
minutes before throwing a timeout exception, we do not know why.

&#160;***Default***: 0 (no delay). On FreeBSD, which is a known OS needing this, it defaults to 2000 (2 seconds).

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .afterStartProcessDelay(5000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        after-start-process-delay: 5000
    ```

    ```conf title="application.properties"
    jodconverter.local.after-start-process-delay = 5000
    ```

=== "Command Line"

    `afterStartProcessDelay` can't be set with the command line tool, it will always be 0.

#### 🔠`existingProcessAction`

This property specifies the action that must be taken when trying to start an office process with a connection
string and that there already is a process running with the same connection string. Available options are:

- **FAIL**: Indicates that the office manager must fail when trying to start an office process and there already is a
    process running with the same connection string. If that is the case, an exception is thrown.
- **KILL**: Indicates that the manager must kill the existing office process when starting a new office process, and
    there already is a process running with the same connection string.
- **CONNECT**: Indicates that the manager must connect to the existing office process when starting a new office
    process, and there already is a process running with the same connection string.
- **CONNECT_OR_KILL**: Indicates that the manager must first try to connect to the existing office process when starting
    a new office process, and there already is a process running with the same connection string. If the connection fails,
    then the manager must kill the existing office process.

&#160;***Default***: ExistingProcessAction.KILL.

See [here](https://github.com/jodconverter/jodconverter/issues/72) to understand why such a property exists
and to learn more about a use case where this properly is useful.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .existingProcessAction(ExistingProcessAction.CONNECT_OR_KILL)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        existing-process-action: connect_or_kill
    ```

    ```conf title="application.properties"
    jodconverter.local.existing-process-action = connect_or_kill
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -x connect_or_kill infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --existing-process-action connect_or_kill infile outfile
    ```

#### ❎`keepAliveOnShutdown`

This property controls whether the manager will keep the office process alive on shutdown. If set to `true`, the stop
task will only disconnect from the office process, which will stay alive. If set to `false`, the office process will be
stopped gracefully (or killed if it could not be stopped gracefully).

See [here](https://github.com/jodconverter/jodconverter/issues/72) to understand why such a property exists
and to learn more about a use case where this properly is useful.

&#160;***Default***: false.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .keepAliveOnShutdown(true)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        keep-alive-on-shutdown: true
    ```

    ```conf title="application.properties"
    jodconverter.local.keep-alive-on-shutdown = true
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -k outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --keep-alive-on-shutdown infile outfile
    ```

#### ❎`startFailFast`

This property controls whether the manager will "fail fast" if an office process cannot be started or the connection
to the started process fails. If set to `true`, `start()` waits for all the office processes to be started and
connected, and throws an exception if one of them cannot be; the manager cannot be used after that. If set to `false`,
`start()` returns immediately: the tasks wait in the queue for an office process to be ready (see `taskQueueTimeout`),
and a process that cannot be started is retried, with a growing delay between the attempts (1, 2, 5, 10, then every 30
seconds). Only logs are produced if anything goes wrong. In both cases, a `stop()` from another thread while `start()`
waits aborts the starts instead of waiting for them, and that `start()` then fails.

&#160;***Default***: false.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .startFailFast(true)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        start-fail-fast: true
    ```

    ```conf title="application.properties"
    jodconverter.local.start-fail-fast = true
    ```

=== "Command Line"

    `startFailFast` can't be set with the command line tool, it will always be false. It would not be possible to support
    this option with the command line tool.

#### 🔢`maxTasksPerProcess`

This property sets the maximum number of tasks an office process can execute before restarting. 0 means an infinite
number of tasks (will never restart). It is not recommended to set this property to 0 since some OOo installation
is known to have memory leaks when converting documents.

&#160;***Default***: 200

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .maxTasksPerProcess(50)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        max-tasks-per-process: 50
    ```

    ```conf title="application.properties"
    jodconverter.local.max-tasks-per-process = 50
    ```

=== "Command Line"

    `maxTasksPerProcess` can't be set with the command line tool, it will always be 200.

#### 🔢`taskQueueCapacity`

This property sets the maximum number of tasks waiting in the conversion queue. A task submitted while the queue is
full fails at once with an `OfficeException`, instead of waiting for the queue timeout; a web application can thus
answer right away that it is overloaded. 0 means no limit.

&#160;***Default***: 0 (no limit)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .taskQueueCapacity(100)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        task-queue-capacity: 100
    ```

    ```conf title="application.properties"
    jodconverter.local.task-queue-capacity = 100
    ```

=== "Command Line"

    `taskQueueCapacity` can't be set with the command line tool, it will always be 0.

#### 🔢`taskRetries`

This property sets how many times a task is executed again when the office process executing it is lost (it
crashed, it was killed, its connection dropped). Instead of failing, the task goes back to the head of the queue and
is executed by the process once it is restarted, or by another one; the queue timeout and the execution timeout
start again for the new attempt.

Only the tasks that can be executed again from the start are retried: a conversion from a file to a file, and an
export of [page images](../getting-started/page-images.md) from a file. A conversion from or to a stream is not,
since a stream is read, or written, only once. A task that exceeds the execution timeout, and the conversion of a
password-protected document without its password, are not retried either: the task itself is the reason.

A document that makes the office crash is converted, and makes it crash, that many more times before its
conversion fails; keep the number low.

&#160;***Default***: 0 (a task whose office process is lost fails)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .taskRetries(1)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        task-retries: 1
    ```

    ```conf title="application.properties"
    jodconverter.local.task-retries = 1
    ```

=== "Command Line"

    `taskRetries` can't be set with the command line tool, it will always be 0.

#### ⌚`taskQueueTimeout`

This property sets the maximum time a task waits in the conversion queue, from its submission until an office process
takes it. Waiting for a process to start or restart is part of it. When it expires, the task is removed from the queue
without having been executed and fails with an `OfficeException`.

&#160;***Default***: 30000 (30 seconds)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .taskQueueTimeout(60000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        task-queue-timeout: 60000
    ```

    ```conf title="application.properties"
    jodconverter.local.task-queue-timeout = 60000
    ```

=== "Command Line"

    `taskQueueTimeout` can't be set with the command line tool, it will always be 30000.

#### ⌚`taskExecutionTimeout`

This property sets the maximum time allowed to execute a task, counted from the moment an office process starts it,
not from its submission. When it expires, the task fails with an `OfficeException`, the office process is killed and
restarted, and the next task is processed by another process in the meantime.

&#160;***Default***: 120000 (2 minutes)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        LocalOfficeManager
            .builder()
            .taskExecutionTimeout(60000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        task-execution-timeout: 60000
    ```

    ```conf title="application.properties"
    jodconverter.local.task-execution-timeout = 60000
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -t 60000 infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --timeout 60000 infile outfile
    ```

--8<-- "note.md"
