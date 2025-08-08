# RemoteConverter Cponfiguration

`RemoteConverter` is the high-level document converter implementation that sends conversion jobs to a remote LibreOffice Online (LOOL/Collabora Online) server via a RemoteOfficeManager. It exposes the same fluent conversion API:

- Accept source documents (File or InputStream) with optional explicit formats.
- Write to target destinations (File or OutputStream) with optional explicit formats.
- Execute the conversion through a `RemoteOfficeManager` which manages connectivity, pooling, and timeouts.

See class:
[org.jodconverter.remote.RemoteConverter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-remote/src/main/java/org/jodconverter/remote/RemoteConverter.java).

A `RemoteConverter` is built using a builder:

```java
RemoteConverter converter = RemoteConverter.builder().build();
```

Here are all the properties you can set through the builder:

#### `officeManager`

This property specifies the `OfficeManager` the converter will use to execute office tasks.

#### `formatRegistry`

This property specifies the `DocumentFormatRegistry` which contains the document formats that will be supported by this
converter.

All other remote-related settings are configured on RemoteOfficeManager itself, such as:

- urlConnection(String) — Base URL of the LibreOffice Online server.
- sslConfig(SslConfig) — SSL/TLS configuration when using HTTPS.
- connectTimeout(long) — Timeout for establishing HTTP connections.
- socketTimeout(long) — Read timeout for HTTP sockets.
- poolSize(int) — Number of concurrent remote connections.
- workingDir(File) — Used for temporary files.
- taskExecutionTimeout(long) / taskQueueTimeout(long) — Protection against slow/stuck jobs.

See: [RemoteOfficeManager](../configuration/remote-manager.md) for full details about `RemoteOfficeManager` options.

--8<-- "note.md"