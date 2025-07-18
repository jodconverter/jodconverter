# Complete Migration Guide

## 4.4.8 to 4.4.9

The disableOpengl option is no longer supported. On recent LibreOffice versions, the option didn't work anymore. To
disable OpenGL, you now must use
the [templateProfileDir](../configuration/local-configuration#templateprofiledir)
option and disable OpenGL by
following [these suggestions](https://wiki.documentfoundation.org/OpenGL#:~:text=LibreOffice%205.3%20and%20newer%3A,Click%20%22Apply%20Changes%20and%20Restart%22)

## 4.4.7 to 4.4.8

This only contains bug fix, enhancements and dependency upgrade. It shouldn't have any impact.

## 4.4.6 to 4.4.7

This only contains bug fix, enhancements and dependency upgrade. It shouldn't have any impact.

## 4.4.5 to 4.4.6

**Custom document formats support**

You can now provide a custom-document-formats.json file as a ressource in your project to customize the document formats
supported by jodconverter instead of overwriting the whole document-formats.json. The formats specified in the
custom-document-formats.json file will be added to the main registry.

**Spring Boot 3 support**

This version of jodconverter can be used with Spring Boot 3.0+

**New jodconverter-samples repository**

The jodconverter-samples module has been moved to
a [dedicated repository](https://github.com/jodconverter/jodconverter-samples). We hope that more examples will
be added to this repository over time.

## 4.4.4 to 4.4.5

**New Local Converter Option**

There is a new option available when building a `LocalConverter`, named `loadDocumentMode`.

```java
final File inputFile = File("...")
final File outputFile = File("...")
final OfficeManager manager = ExternalOfficeManager
        .builder()
        .connectFailFast(true)
        .connectOnStart(true)
        .hostName("127.0.0.1")
        .portNumbers(8100)
        .build();
try {
    manager.start();
    LocalConverter.builder()
            .officeManager(manager)
            .loadDocumentMode(LoadDocumentMode.REMOTE)
            .build()
            .convert(inputFile)
            .to(outputFile)
            .execute();
} catch (OfficeException e) {
    e.printStackTrace();
} finally {
    OfficeUtils.stopQuietly(manager);
}
```

This new option has been introduced to support conversions using an OOo instance that doesn't share the same drives as
the process running JODConverter. When `LoadDocumentMode.REMOTE` is set, local files will be converted to/from streams
when loading and storing a document. This is useful when using an `ExternalOfficeManager` to connect to an OOo instance
running on another server or in a Docker container.

## 4.4.3 to 4.4.4

Basic migration with no significant changes.

## 4.4.2 to 4.4.3

**New WEB document family**

There is a new document family for better web documents support.

See [here](https://github.com/jodconverter/jodconverter/issues/297) for more details.

**Provide builds of both OpenOffice and LibreOffice dependencies in the maven center**

There is 2 new JODConverter modules published in the maven center allowing you to easily choose whether you want to use the LibreOffice dependencies or the OpenOffice dependencies in your project. Since the gap between LibreOffice and OpenOffice is increasing each year, you may want to rely only on LibreOffice dependencies, wich was not the default.

**Usage for local conversions using LibreOffice dependencies**

**Gradle:**
```groovy
compile 'org.jodconverter:jodconverter-local-lo:4.4.3'
```

**Maven:**
```xml
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-local-lo</artifactId>
  <version>4.4.3</version>
</dependency>
```

**Usage for local conversions using OpenOffice dependencies**

**Gradle:**
```groovy
compile 'org.jodconverter:jodconverter-local-oo:4.4.3'
```
or you can continue to use
```groovy
compile 'org.jodconverter:jodconverter-local:4.4.3'
```

**Maven:**
```xml
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-local-oo</artifactId>
  <version>4.4.3</version>
</dependency>
```
or you can continue to use
```xml
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-local</artifactId>
  <version>4.4.3</version>
</dependency>
```

## 4.4.1 to 4.4.2

This only contains bug fixes. It shouldn't have any impact.

## 4.4.0 to 4.4.1

This only contains bug fixes. It shouldn't have any impact.

## 4.3.0 to 4.4.0

**Asynchronous office processes management**

To improve the startup time of a server using jodconverter, the office processes management is now asynchronous.
Suppose we have an office manager started this way:

```java
OfficeManager officeManager =
    LocalOfficeManager.builder()
        .portNumbers(2002, 2003, 2004, 2005)
        .build();
officeManager.start();
```

Before 4.4.0, the `officeManager.start()` would wait for all the office processes to be started. Now, the call returns
immediately, meaning a faster starting process, and only error logs will be produced if anything goes wrong.

To reproduce the behavior from an older version of jodconverter, the `startFailFast` property must be set to `true`:

```java
OfficeManager officeManager =
    LocalOfficeManager.builder()
        .portNumbers(2002, 2003, 2004, 2005)
        .startFailFast(true)
        .build();
officeManager.start();
```

**Existing process management (breaking change)**

The `killExistingProcess` option has been replaced by the
[existingProcessAction](../configuration/local-configuration#existingprocessaction).
If the `killExistingProcess` was not used, then there is nothing to do; the behavior remains the same. But if
`killExistingProcess` was set to false, you must now set the `existingProcessAction` to `ExistingProcessAction.FAIL`.

## 4.2.4 to 4.3.0

**Package names refactoring**

All the jodconverter modules have now their own base package name, to fix
[#178](https://github.com/jodconverter/jodconverter/issues/178). This means that you'll have to refactor all the
imports that cannot be resolved anymore. The best way to do that is to remove the old import and to allow the IDE you
are working with to resolve the new import.

Example:
The class: `org.jodconverter.LocalConverter` is now `org.jodconverter.local.LocalConverter`

**Deprecated class removed**

* `org.jodconverter.filter.text.PageCounterFilter`. Please use `org.jodconverter.local.filter.PagesCounterFilter`
* `org.jodconverter.filter.text.PageSelectorFilter`. Please use `org.jodconverter.local.filter.PagesSelectorFilter`
* `org.jodconverter.office.LocalOfficeUtils#closeQuietly`. Please use `org.jodconverter.core.office.OfficeUtils#closeQuietly`

**All remote (online) stuff moved from jodconverter-online to jodconverter-remote**
The jodconverter-online module was a contribution made by the LibreOffice Online team. But the name was confusing since
most people thought that this module could be used as a server processing conversion requests. But it is in fact a
client that can send conversion requests to a server. Hopefully, this new name will clarify the purpose of the module.

**Old Maven Setup 4.2.4**

```xml
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-online</artifactId>
      <version>4.2.4</version>
   </dependency>
</dependencies>
```

**New Maven Setup 4.3.0**

```xml
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-remote</artifactId>
      <version>4.3.0</version>
   </dependency>
</dependencies>
```

**Old Gradle Setup 4.2.4**

```groovy
compile 'org.jodconverter:jodconverter-online:4.2.4'
```

**New Gradle Setup 4.3.0**

```groovy
compile 'org.jodconverter:jodconverter-remote:4.3.0'
```

## 4.2.3 to 4.2.4

This only contains one bug fix. It shouldn't have any impact.

## 4.2.2 to 4.2.3

This only contains bug fix, enhancements and dependency upgrade. It shouldn't have any impact.

## 4.2.1 to 4.2.2

This only contains bug fix, enhancements and dependency upgrade. It shouldn't have any impact.

## 4.2.0 to 4.2.1

This only contains bug fix, enhancements and dependency upgrade.

**Spring Boot**
jodconverter-spring-boot-starter 4.2.1 builds on Spring Boot 2.0.6

## 4.1.1 to 4.2.0

The [LibreOffice Online](https://wiki.documentfoundation.org/Development/LibreOffice_Online) support was introduce in
the 4.1.0 version of JODConverter through
the [jodconverter-online](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-online) module, but there
was no Spring Boot support for this new module. This was the main goal of the 4.2.0 version of JODConverter. The reason
why the version is 4.2.0 and not 4.1.2 is because a jodconverter-spring-boot-starter user will have to change slightly
its project configuration.

**Spring Boot project configuration changes**

The `jodconverter-local` and `jodconverter-online` dependencies has been marked as optional in the
`jodconverter-spring-boot-starter` module. Thus, a user must specify which module(s) he wants to use through
dependencies:

**Old Maven Setup 4.1.1**

```xml
<properties>
    <jodconverter.version>4.1.1</jodconverter.version>
</properties>
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-spring-boot-starter</artifactId>
      <version>${jodconverter.version}</version>
   </dependency>
</dependencies>
```

**New Maven Setup 4.2.0**

```xml
<properties>
    <jodconverter.version>4.2.0</jodconverter.version>
</properties>
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-local</artifactId>
      <version>${jodconverter.version}</version>
   </dependency>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-spring-boot-starter</artifactId>
      <version>${jodconverter.version}</version>
   </dependency>
</dependencies>
```

**Old Gradle Setup 4.1.1**
```groovy
ext {
    jodconverterVersion = 4.1.1
}
compile "org.jodconverter:jodconverter-spring-boot-starter:$jodconverterVersion"
```
**New Gradle Setup 4.2.0**
```groovy
ext {
    jodconverterVersion = 4.2.0
}
compile "org.jodconverter:jodconverter-local:$jodconverterVersion"
compile "org.jodconverter:jodconverter-spring-boot-starter:$jodconverterVersion"
```

**Spring Boot JODConverter configuration changes**

Since the user can now use both the `jodconverter-local` and `jodconverter-online` modules in a Spring Boot application,
the configurable JODConverter properties prefix has changed. Now there is 2 distincts prefix; `jodconverter.local` and
`jodconverter.online`.

A user migrating from 4.1.1 to 4.2.0 must use `jodconverter.local` instead of `jodconverter` only.

## 4.1.0 to 4.1.1

The two major changes have been to set the Java baseline to Java 8 and the addition of SSL support for JODConverter online.

**Move to Java 8 for baseline**
JODConverter 4.1.1 is built using Java 8 JDK and will require Java 8 JRE at runtime (Java 9 not tested).

**Using JODConverter Online SSL Support**

Please read the [SSL Support](../getting-started/libreoffice-online#ssl-support) wiki page
section to fully understand the SSL Support.

**4.0.0-RELEASE to 4.1.0**

Lots of work has been done for 4.1.0. The two major changes have been to create a new fluent API for performing document
conversions, and to introduce a new module
for [LibreOffice Online](https://wiki.documentfoundation.org/Development/LibreOffice_Online) support. The addition of
this new module has had an impact on the project structure and thus, JODConverter users will have to update their
dependency to reflect this change.

**Move to Java 7 for baseline**
JODConverter 4.1.0 is built using Java 7 JDK and will require Java 7 JRE at runtime (Java 8 also works).

**All local stuff moved from jodconverter-core to jodconverter-local**

At first, the support for LibreOffice Online was all developed in the main `jodconverter-core` project. But I didn't
like the fact that it added more (HTTP) dependencies to the project. Even worse, the 4.0.0-RELEASE version of
`jodconverter-core` requires an office installation (LibreOffice/Apache OpenOffice), and LibreOffice Online does not. It
would have been pointless to offer an online converter that requires a local OOo installation.

So I decided to create 2 new modules that depend on the core module; `jodconverter-local` and `jodconverter-online`. All
the classes required for local conversions was moved into the local module and all the new classes required for online
conversions was moved into the online module. This way, the online module doesn't have all the OOo libraries as
dependencies, nor the local module has the http-components libraries as dependencies.

Since the `jodconverter-core` project is now just a dependency of both the new modules, former JODConverter users must
now use the `jodconverter-local` in their project, instead of `jodconverter-core`:

**Old Maven Setup 4.0.0-RELEASE**

```xml
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-core</artifactId>
      <version>4.0.0-RELEASE</version>
   </dependency>
</dependencies>
```

**New Maven Setup 4.1.0**

```xml
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-local</artifactId>
      <version>4.1.0</version>
   </dependency>
</dependencies>
```

**Old Gradle Setup 4.0.0-RELEASE**

```groovy
compile 'org.jodconverter:jodconverter-core:4.0.0-RELEASE'
```

**New Gradle Setup 4.1.0**

```groovy
compile 'org.jodconverter:jodconverter-local:4.1.0'
```

**Using the new API**
Even if the 4.1.0 version is backward compatible (just changing maven dependency from jodconverter-core to
jodconverter-local is enough), JODConverter users are encouraged to use the new API since the classes
`DefaultOfficeManagerBuilder` and `OfficeDocumentConverter` are now deprecated.

Please read the [Usage as a Java Library](../getting-started/java-library) wiki page to
fully understand the new API.

Here is a conversion example using the 4.0.0-RELEASE version:

```java
File source = new File(...);
File dest = new File(...);

OfficeManager officeManager = null;
try {
  officeManager = new DefaultOfficeManagerBuilder()
    .setPortNumbers(2002, 2003)
    .setTaskExecutionTimeout(60000)
    .setOfficeHome(new File("office path"))
    .build();
  officeManager.start();

  OfficeDocumentConverter converter
    = new OfficeDocumentConverter(officeManager);
  converter.convert(source, dest);

} finally  {
  if (officeManager != null) {
    try {
      officeManager.stop();
    } catch (OfficeException ex) {
      // Log the error...
    }
  }
}
```

And here is the exact same example, using the new 4.1.0 version:

```java
File source = new File(...);
File dest = new File(...);

OfficeManager officeManager = null;
try {
  officeManager = LocalOfficeManager.builder()
    .portNumbers(2002, 2003)
    .taskExecutionTimeout(60000)
    .officeHome("office path")
    .build();
  officeManager.start();

  LocalConverter.make(officeManager)
    .convert(source)
    .to(dest)
    .execute();

} finally  {
  if (officeManager != null) {
    try {
      officeManager.stop();
    } catch (OfficeException ex) {
      // Log the error...
    }
  }
}
```

Note that in a WEB application context where an office manager is created and started once when the application starts,
the office manager can now be installed, and thus it is no longer required to specify which manager is used when
converting a document:

```java

// On application start
OfficeManager officeManager = LocalOfficeManager.builder()
  .portNumbers(2002, 2003)
  .taskExecutionTimeout(60000)
  .officeHome("office path")
  .install()
  .build();
officeManager.start();

...

// On document conversion
File source = new File(...);
File dest = new File(...);

LocalConverter.make()
  .convert(source)
  .to(dest)
  .execute();

// Or the shortcut:
JodConverter
  .convert(source)
  .to(dest)
  .execute();
```