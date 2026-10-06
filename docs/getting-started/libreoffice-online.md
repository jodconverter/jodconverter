# LibreOffice Remote

This module integrates the Collabora Online / LibreOffice Online conversion functionality into **JODConverter**.

Collabora Online and LibreOffice Online have built-in functionality to process conversions on a remote server. With this
module, you can use the familiar JODConverter API in your applications to connect to the Online instance, which makes
the conversion setup trivial, particularly when you are already running a Collabora Online or LibreOffice Online server
for another need.

You can also use the pre-built CODE (Collabora Online Development Edition) Docker container just for the conversions, to
avoid the LibreOffice installation on your server.

### Maven Setup

```xml

<dependencies>
    <dependency>
        <groupId>org.jodconverter</groupId>
        <artifactId>jodconverter-remote</artifactId>
        <version>4.9.0</version>
    </dependency>
</dependencies>
```

### Gradle Setup

=== "Groovy"

    ```groovy
    implementation "org.jodconverter:jodconverter-remote:4.4.11"
    ```

=== "Kotlin"

    ```kotlin
    implementation("org.jodconverter:jodconverter-remote:4.4.11")
    ```

## Using the module

To convert documents using the remote module, you have to specify the address of a running Collabora Online or
LibreOffice Online server.

### With Command Line Tool

When a connection url is specified with the **-c** or **--connection-url** option, the tool will use the remote module.

### Java Library

```java
final RemoteOfficeManager officeManager = RemoteOfficeManager.make("http://path/to/myLibreOfficeOnlineServer");
```

See [Java Library](java-library/index.md) for more.

### SSL Support

When JODConverter remote is used as a Java Library, you must provide the SSL configuration while building the
RemoteOfficeManager:

```java
final SslConfig sslConfig = new SslConfig();
sslConfig.setEnabled(true);
sslConfig.setTrustStore("Path to the TrustStore");
sslConfig.setTrustStorePassword("Password of the TrustStore");

final OfficeManager manager =
    RemoteOfficeManager.builder()
        .urlConnection("http://path/to/myLibreOfficeOnlineServer")
        .sslConfig(sslConfig)
        .build();
```

When JODConverter remote is used as a Command Line Tool, you provide the SSL configuration through the configuration
file of the **--config** option, JSON or YAML, in its `ssl` section. Here is an example of an SSL configuration file,
with every key; see the [command line tool](command-line-tool.md#ssl-options) page.

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

```shell
jodconverter-cli -c "https://localhost:8001/lool/convert-to/" --config ssl.yml infile outfile
```

## Using the Collabora Online / LibreOffice Online without JODConverter

It is possible to use the Online converting functionality directly, without the JODConverter API:

### LibreOffice Online API

- API: HTTP POST to `/lool/convert-to/<format>`

    - the format is e.g. "png", "pdf" or "txt"
    - the file itself in the payload

- example

    - `curl -F "data=@test.txt" https://localhost:9980/lool/convert-to/docx > out.docx`

    - or in html:

        ```html
        <form action="https://localhost:9980/lool/convert-to/docx" enctype="multipart/form-data" method="post">
            File: <input type="file" name="data"><br/>
            <input type="submit" value="Convert to DOCX">
        </form>
        ```

- alternatively you can omit the `<format>`, and instead provide it as another
    parameter

- example

    - `curl -F "data=@test.odt" -F "format=pdf" https://localhost:9980/lool/convert-to > out.pdf`

    - or in html:

        ```html
        <form action="https://localhost:9980/lool/convert-to" enctype="multipart/form-data" method="post">
            File: <input type="file" name="data"><br/>
            Format: <input type="text" name="format"><br/>
            <input type="submit" value="Convert">
        </form>
        ```

## Create your own Online server

The easiest way is using the [Collabora Online Development Edition (CODE)
Docker image](https://www.collaboraoffice.com/code/).

Alternatively you
can [build everything yourself](https://wiki.documentfoundation.org/Development/LibreOffice_Online#Development),
it is all Free Software :-)
