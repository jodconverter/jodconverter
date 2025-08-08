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

#### &#8986;`poolSize`

This property sets the size of the pool. Setting this property controls how many conversions can be done concurrently. 

&nbsp;***Default***: 1

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


#### &#128193;`workingDir`

This property is used to create a temporary directory where files will be created when conversions are done
using InputStream/OutputStream.

&nbsp;***Default***: The system temporary directory as specified by the `java.io.tmpdir` system property.

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

#### &#128193;`urlConnection`

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

#### &#8986;`connectTimeout`

This property sets the timeout in milliseconds until a connection is established. A timeout value of zero is
interpreted as an infinite timeout. A negative value is interpreted as undefined (system default).

&nbsp;***Default***: 60000 (1 minute)

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

#### &#8986;`socketTimeout`

This property sets the socket timeout `SO_TIMEOUT` in milliseconds, which is the timeout for waiting for data or,
to put differently, a maximum period inactivity between two consecutive data packets. A timeout value of zero is
interpreted as an infinite timeout. A negative value is interpreted as undefined (system default).

&nbsp;***Default***: 120000 (2 minutes)

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

#### &#128274;`sslConfig`

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

    When JODConverter remote is used as a Command Line Tool, you must provide the SSL configuration through an
    application context configuration file, which is the **`-a`** or **`--application-context`**. Here's an example of
    an SSL configuration file.
    
    ```xml title="ssl.xml"
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    
        <!-- Configure the SSL to secure communication with a Libre Office Online server. -->
        <bean class="org.jodconverter.ssl.SslConfig">
            <!-- Indicates whether SSL support is enabled or not. -->
            <property name="enabled" value="true"/>
            <!-- Comma separated values of the supported SSL ciphers. Defaults to the JVM default values. -->
            <property name="ciphers" value="ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"/>
            <!-- Comma separated values of the enabled SSL protocols. Defaults to the JVM default values. -->
            <property name="enabledProtocols" value="enabledProtocols"/>
            <!-- The alias that identifies the key in the key store. -->
            <property name="keyAlias" value="keyalias"/>
            <!-- The password used to access the key in the key store. -->
            <property name="keyPassword" value="keypassword"/>
            <!-- The path to the key store. -->
            <property name="keyStore" value="/path/to/the/keystore.jks"/>
            <!-- The password used to load the key store. -->
            <property name="keyStorePassword" value="keystorepassword"/>
            <!-- The type of key store. -->
            <property name="keyStoreType" value="JKS"/>
            <!-- The provider for the key store. -->
            <property name="keyStoreProvider" value="BC"/>
            <!-- The path to the trust store. -->
            <property name="trustStore" value="/path/to/the/truststore.p12"/>
            <!-- The password used to load the trust store . -->
            <property name="trustStorePassword" value="truststorepassword"/>
            <!-- The type of trust store. -->
            <property name="trustStoreType" value="PKCS12"/>
            <!-- The provider for the trust store. -->
            <property name="trustStoreProvider" value="SUN"/>
            <!-- The SSL protocol to use. Default to TLS. -->
            <property name="protocol" value="TLS"/>
            <!-- Indicates whether hostname should be verify during SSL handshake. Defaults to true. -->
            <property name="verifyHostname" value="true"/>
        </bean>
    </beans>
    ```

    then

    ```shell title="short option"
    jodconverter-cli -c "https://localhost:8001" -a ssl.xml timeout infile outfile
    ```
    or
    ```shell title="long option"
    jodconverter-cli --connection-url "https://localhost:8001" --application-context ssl.xml timeout infile outfile
    ```

#### &#128290;`maxTasksPerConnection`

This property sets the maximum number of tasks an office process can execute before reconnecting to it. 0 means an
infinite number of tasks (will never reconnect).

&nbsp;***Default***: 1000

=== "Java"

    ```java hl_lines="4"
    OfficeManager officeManager =
        RemoteOfficeManager
            .builder()
            .maxTasksPerConnection(500)
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      remote:
        max-tasks-per-connection: 500
    ```
    
    ```conf title="application.properties"
    jodconverter.remote.max-tasks-per-connection = 500
    ```

=== "Command Line"

    `maxTasksPerConnection` can't be set with the command line tool, it will always be 200.

#### &#8986;`taskQueueTimeout`

This property is used to set the maximum living time of a task in the conversion queue. The task will be removed from
the queue if the waiting time is longer than this timeout and an `OfficeException` will be thrown.

&nbsp;***Default***: 30000 (30 seconds)

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

#### &#8986;`taskExecutionTimeout`

This property sets the maximum time allowed to process a task. If the processing time of a task is longer than this
timeout, this task will be aborted and the next task is processed.

&nbsp;***Default***: 120000 (2 minutes)

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