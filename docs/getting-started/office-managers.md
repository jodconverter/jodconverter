# Office Managers

This page explains what an Office Manager is in the **JODConverter** ecosystem and how it fits into conversions.

## What is an Office Manager?

In **JODConverter**, an Office Manager is the component responsible for starting, supervising, and using office
processes (LibreOffice or OpenOffice) to execute OfficeTasks such as document conversions. It provides a simple
lifecycle and a thread-safe execution API:

- **start()**: boots the underlying office process(es) and gets them ready to accept tasks.
- **execute(OfficeTask)**: submits a task and blocks until it completes.
- **stop()**: shuts down the manager and its office process(es). After `stop()`, a manager cannot be restarted.

See the interface definition:
[org.jodconverter.core.office.OfficeManager](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-core/src/main/java/org/jodconverter/core/office/OfficeManager.java).

## Why do you need one?

Converters (e.g., `LocalConverter` or `RemoteConverter`) do not start OOo themselves. They delegate
to an Office Manager which maintains the connection(s) to office processes, enforces timeouts, queues tasks, and
recovers from failures.

Without an Office Manager, there’s no running office backend to perform conversions.

## Manager types

**JODConverter** ships with different Office Manager implementations depending on how and where OOo runs:

- **LocalOfficeManager**: Starts and manages one or more local office processes on the same machine as your application.
  Best for typical server-side use when OOo is installed locally.
  See [LocalOfficeManager](../configuration/local-manager.md) for all configuration options.
- **ExternalOfficeManager**: Connects to an already running local office process you start externally (you manage the
  process lifecycle). Useful when the process must be controlled outside the JVM.
  See [ExternalOfficeManager](../configuration/external-manager.md) for all configuration options.
- **RemoteOfficeManager**: Connects to a remote LibreOffice Online (LOOL/Collabora Online) server through
  HTTP/WebSocket.
  The office process lifecycle is remote; the manager controls the connection pool.
  See [RemoteOfficeManager](../configuration/remote-manager.md) for all configuration options.

Internally, pool-capable managers derive from an abstract pool (`AbstractOfficeManagerPool`) that can manage multiple
office processes simultaneously (via multiple ports or pipes) for concurrency and resilience.

## Typical usage patterns

**1)** Install a global (default) manager.

- Installing a manager in the `InstalledOfficeManagerHolder` lets converter builders use it automatically when you
  don't specify any office manager.

Java example:

```java
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

public class Example {
  public static void main(String[] args) throws OfficeException {
    // Build and install a LocalOfficeManager as the global default
    LocalOfficeManager officeManager = LocalOfficeManager.builder().install();
    try {
      officeManager.start();

      // Now LocalConverter.make() will use the installed manager by default
      LocalConverter
          .make()
          .convert(new java.io.File("in.docx"))
          .to(new java.io.File("out.pdf"))
          .execute();

    } finally {
      OfficeUtils.stopQuietly(officeManager);
    }
  }
}
```

**2)** Pass a manager explicitly to a converter.

```java
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

OfficeManager officeManager = LocalOfficeManager.builder().build();
try {
  officeManager.start();

  LocalConverter
      .builder()
      .officeManager(officeManager)
      .build()
      .convert(new java.io.File("in.docx"))
      .to(new java.io.File("out.pdf"))
      .execute();

} finally {
  OfficeUtils.stopQuietly(officeManager);
}
```

**3)** Connect to an existing local process (`ExternalOfficeManager`).

If you start LibreOffice manually with a known accept string, use `ExternalOfficeManager`. See
configuration/external-manager.md for details.

**4)** Use a remote LibreOffice Online server (`RemoteOfficeManager`).

When targeting LO Online/Collabora Online, create a `RemoteOfficeManager` and pass it to a RemoteConverter.
See [RemoteOfficeManager](../configuration/remote-manager.md).

## Lifecycle and threading

- Start before use: Always call `start()` before invoking `execute()` through a converter.
- Stop on shutdown: Call `stop()` when your application shuts down. OfficeUtils.stopQuietly(...) is provided for
  convenience in finally blocks.
- Not restartable: Once stopped, a given manager instance cannot be restarted; create a new instance if needed.
- Thread-safe queueing: Managers queue and dispatch tasks to their office processes. Timeouts like
  `taskExecutionTimeout` and `taskQueueTimeout` protect against slow or stuck tasks
  ([see LocalOfficeManager configuration page](../configuration/local-manager.md) for more details).

## Best practices

- One manager per application: Create one `OfficeManager` and reuse it across conversions. Avoid creating a manager per
  request.
- Start early: Start the manager during application boot (e.g., in a servlet context listener or Spring Boot lifecycle)
  and stop it with a shutdown hook.
- Tune for throughput: For concurrent workloads, configure multiple ports/pipes so the manager can spawn multiple office
  processes.
- Mitigate leaks: Keep `maxTasksPerProcess` at a reasonable value so office processes are recycled periodically.
- Use the holder wisely: install() sets the `InstalledOfficeManagerHolder`; this is convenient for libraries and reduces
  boilerplate.

## Related APIs

- `OfficeManager` (core): lifecycle and execute contract.
- `InstalledOfficeManagerHolder` (core): global singleton used by converter builders when no manager is provided.
- `LocalOfficeManager` (local): starts and manages local OOo processes.
- `ExternalOfficeManager` (local): connects to an already running local office process you manage externally.
- `RemoteOfficeManager` (remote): connects to LibreOffice Online / Collabora Online.

For in-depth configuration of each manager type, refer to:

- [LocalOfficeManager](../configuration/local-manager.md)
- [ExternalOfficeManager](../configuration/external-manager.md)
- [RemoteOfficeManager](../configuration/remote-manager.md)

--8<-- "note.md"