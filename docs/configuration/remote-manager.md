# RemoteOfficeManager Configuration

The [RemoteOfficeManager](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-remote/src/main/java/org/jodconverter/remote/office/RemoteOfficeManager.java)
is the manager to use when you want to send conversion requests to a server supporting document conversions through a
REST API (like Collabora Online) and want to use the familiar JODConverter API in your applications.

A `RemoteOfficeManager` is built using a builder:

```java
OfficeManager officeManager = RemoteOfficeManager.builder().urlConnection("http://path/to/myLibreOfficeOnlineServer").build();
```

Here are all the properties you can set through the builder:

!!! note

    **JODConverter** uses milliseconds for all time values.

#### ⌚`poolSize`

This property sets the size of the pool. Setting this property controls how many conversions can be done concurrently.

&#160;***Default***: 1

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .poolSize(1)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        pool-size: 1
    ```

    ```conf title="application.properties"
    jodconverter.remote.pool-size = 1
    ```

=== "Command Line"

    `poolSize` can't be set with the command line tool, it will always be 1.

#### 📁`workingDir`

This property is used to create a temporary directory where files will be created when conversions are done
using InputStream/OutputStream.

&#160;***Default***: The system temporary directory as specified by the `java.io.tmpdir` system property.

**NOTE** that
[some OS automatically clean up the `java.io.tmpdir` directory periodically](https://github.com/jodconverter/jodconverter/issues/220).
It is recommended to check your OS to see if you have to set this property to a directory that won't be deleted.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .workingDir("C:\\jodconverter\\tmp")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        working-dir: "C:/jodconverter/tmp"
    ```

    ```conf title="application.properties"
    jodconverter.remote.working-dir = "C:/jodconverter/tmp"
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -c "https://localhost:8001" -w "C:/jodconverter/tmp" timeout infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --connection-url "https://localhost:8001" --wirking-dir "C:/jodconverter/tmp" timeout infile outfile
    ```

#### 📁`urlConnection`

This property sets the URL of the remote server.

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .urlConnection("https://localhost:8001")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        url: "https://localhost:8001"
    ```

    ```conf title="application.properties"
    jodconverter.remote.url = "https://localhost:8001"
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -c "https://localhost:8001" infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --connection-url "https://localhost:8001" infile outfile
    ```

#### ⌚`connectTimeout`

This property sets the timeout in milliseconds until a connection is established. A timeout value of zero is
interpreted as an infinite timeout. A negative value is interpreted as undefined (system default).

&#160;***Default***: 60000 (1 minute)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .connectTimeout(120000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        connect-timeout: 120000
    ```

    ```conf title="application.properties"
    jodconverter.remote.connect-timeout = 120000
    ```

=== "Command Line"

    `connectTimeout` can't be set with the command line tool, it will always be 60000.

#### ⌚`socketTimeout`

This property sets the socket timeout `SO_TIMEOUT` in milliseconds, which is the timeout for waiting for data or,
to put differently, a maximum period inactivity between two consecutive data packets. A timeout value of zero is
interpreted as an infinite timeout. A negative value is interpreted as undefined (system default).

&#160;***Default***: 120000 (2 minutes)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
            RemoteOfficeManager
                    .builder()
                    .socketTimeout(60000)
                    .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        socket-timeout: 60000
    ```

    ```conf title="application.properties"
    jodconverter.remote.socket-timeout = 60000
    ```

=== "Command Line"

    `socketTimeout` can't be set with the command line tool, it will always be 120000.

#### 🔒`sslConfig`

This property controls the SSL configuration to secure communication with the remote server

=== "Java"

    ```java hl_lines="1 2 3 4 9"
    final SslConfig sslConfig = new SslConfig();
    sslConfig.setEnabled(true);
    sslConfig.setTrustStore("Path to the TrustStore");
    sslConfig.setTrustStorePassword("Password of the TrustStore");

    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .sslConfig(sslConfig)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        ssl:
          enabled: true
          ciphers: TLS_RSA_WITH_AES_128_CBC_SHA
          enabled-protocols: TLSv1.1, TLSv1.2
          key-alias: clientkeypair
          key-password: clientkeystore
          key-store: classpath:clientkeystore.jks
          key-store-password: clientkeystore
          key-store-type: jks
          key-store-provider: SUN
          trust-store: classpath:clienttruststore.jks
          trust-store-password: clienttruststore
          trust-store-type: jks
          trust-store-provider: SUN
          protocol: TLS
          verify-hostname: true
    ```

    ```conf title="application.properties"
    jodconverter.remote.ssl.enabled = true
    jodconverter.remote.ssl.ciphers = TLS_RSA_WITH_AES_128_CBC_SHA
    jodconverter.remote.ssl.enabled-protocols = TLSv1.1, TLSv1.2
    jodconverter.remote.ssl.key-alias = clientkeypair
    jodconverter.remote.ssl.key-password = clientkeystore
    jodconverter.remote.ssl.key-store = classpath:clientkeystore.jks
    jodconverter.remote.ssl.key-store-password = clientkeystore
    jodconverter.remote.ssl.key-store-type = jks
    jodconverter.remote.ssl.key-store-provider = SUN
    jodconverter.remote.ssl.trust-store = classpath:clienttruststore.jks
    jodconverter.remote.ssl.trust-store-password = clienttruststore
    jodconverter.remote.ssl.trust-store-type = jks
    jodconverter.remote.ssl.trust-store-provider = SUN
    jodconverter.remote.ssl.protocol = TLS
    jodconverter.remote.ssl.verify-hostname = true
    ```

=== "Command Line"

    When JODConverter remote is used as a Command Line Tool, you provide the SSL configuration through the
    configuration file of the **`--config`** option, JSON or YAML, in its `ssl` section. Here is an example of an SSL
    configuration file.

    ```yaml title="ssl.yml"
    ssl:
      # Whether SSL support is enabled. Defaults to false.
      enabled: true
      # The supported SSL ciphers; a list, or comma-separated. Defaults to the JVM default values.
      ciphers: [ECDHE_RSA_WITH_AES_256_CBC_SHA384, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA]
      # The enabled SSL protocols; a list, or comma-separated. Defaults to the JVM default values.
      enabled-protocols: [TLSv1.2, TLSv1.3]
      # The alias that identifies the key in the key store.
      key-alias: keyalias
      # The password used to access the key in the key store.
      key-password: keypassword
      # The path to the key store.
      key-store: /path/to/the/keystore.jks
      # The password used to load the key store.
      key-store-password: keystorepassword
      # The type of key store.
      key-store-type: JKS
      # The provider for the key store.
      key-store-provider: BC
      # The path to the trust store.
      trust-store: /path/to/the/truststore.p12
      # The password used to load the trust store.
      trust-store-password: truststorepassword
      # The type of trust store.
      trust-store-type: PKCS12
      # The provider for the trust store.
      trust-store-provider: SUN
      # The SSL protocol to use. Defaults to TLS.
      protocol: TLS
      # Whether every certificate is trusted, without a trust store. Defaults to false.
      trust-all: false
      # Whether the host name is verified during the SSL handshake. Defaults to true.
      verify-hostname: true
    ```

    then

    ```shell title="short option"
    jodconverter-cli -c "https://localhost:8001" --config ssl.yml infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --connection-url "https://localhost:8001" --config ssl.yml infile outfile
    ```

#### 🔢`taskQueueCapacity`

This property sets the maximum number of tasks waiting in the conversion queue. A task submitted while the queue is
full fails at once with an `OfficeException`, instead of waiting for the queue timeout; a web application can thus
answer right away that it is overloaded. 0 means no limit.

&#160;***Default***: 0 (no limit)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .taskQueueCapacity(100)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        task-queue-capacity: 100
    ```

    ```conf title="application.properties"
    jodconverter.remote.task-queue-capacity = 100
    ```

=== "Command Line"

    `taskQueueCapacity` can't be set with the command line tool, it will always be 0.

#### ⌚`taskQueueTimeout`

This property sets the maximum time a task waits in the conversion queue, from its submission until a worker of the
pool takes it. When it expires, the task is removed from the queue without having been executed and fails with an
`OfficeException`.

&#160;***Default***: 30000 (30 seconds)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .taskQueueTimeout(60000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        task-queue-timeout: 60000
    ```

    ```conf title="application.properties"
    jodconverter.remote.task-queue-timeout = 60000
    ```

=== "Command Line"

    `taskQueueTimeout` can't be set with the command line tool, it will always be 30000.

#### ⌚`taskExecutionTimeout`

This property sets the maximum time allowed to execute a task, counted from the moment a worker of the pool starts it.
When it expires, the task fails with an `OfficeException` and its request to the server is aborted.

&#160;***Default***: 120000 (2 minutes)

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .taskExecutionTimeout(60000)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        task-execution-timeout: 60000
    ```

    ```conf title="application.properties"
    jodconverter.remote.task-execution-timeout = 60000
    ```

=== "Command Line"

    ```shell title="short option"
    jodconverter-cli -c "https://localhost:8001" -t 60000 infile outfile
    ```

    or

    ```shell title="long option"
    jodconverter-cli --connection-url "https://localhost:8001" --timeout 60000 infile outfile
    ```

--8<-- "note.md"
