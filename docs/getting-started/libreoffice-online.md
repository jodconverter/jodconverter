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
    implementation "org.jodconverter:jodconverter-remote:4.4.10"
    ```

=== "Kotlin"
    ```kotlin
    implementation("org.jodconverter:jodconverter-remote:4.4.10")
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

See [Java Library](java-library.md) for more.

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

When JODConverter remote is used as a Command Line Tool, you must provide the SSL configuration through an application
context configuration file, which is the **-a** or **--application-context**. Here's an example of an SSL configuration
file.

```xml
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

## Using the Collabora Online / LibreOffice Online without JODConverter

It is possible to use the Online converting functionality directly, without the JODConverter API:

### LibreOffice Online API

- API: HTTP POST to /lool/convert-to/<format>
    - the format is e.g. "png", "pdf" or "txt"
    - the file itself in the payload
- example
    - ```curl -F "data=@test.txt" https://localhost:9980/lool/convert-to/docx > out.docx```
    - or in html:
```
<form action="https://localhost:9980/lool/convert-to/docx" enctype="multipart/form-data" method="post">
    File: <input type="file" name="data"><br/>
    <input type="submit" value="Convert to DOCX">
</form>
```

- alternatively you can omit the <format>, and instead provide it as another
  parameter
- example
    - ```curl -F "data=@test.odt" -F "format=pdf" https://localhost:9980/lool/convert-to > out.pdf```
    - or in html:

```
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
