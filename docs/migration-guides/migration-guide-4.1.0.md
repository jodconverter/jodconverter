This guide discusses migration from JODConverter version 4.0.0-RELEASE to version 4.1.0.

## Background

Lots of work has been done for 4.1.0. The two major changes have been to create a new fluent API for performing document
conversions and to introduce a new module
for [LibreOffice Online](https://wiki.documentfoundation.org/Development/LibreOffice_Online) support. The addition of
this new module has had an impact on the project structure, and thus, JODConverter users will have to update their
dependency to reflect this change.

## Move to Java 7 for baseline

JODConverter 4.1.0 is built using Java 7 JDK and will require Java 7 JRE at runtime (Java 8 also works).

## New jodconverter-local module

At first, the support for LibreOffice Online was all developed in the main `jodconverter-core` project. But I didn't
like the fact that it added more (HTTP) dependencies to the project. Even worse, the 4.0.0-RELEASE version of
`jodconverter-core` requires an office installation (LibreOffice/Apache OpenOffice), and LibreOffice Online does not. It
would have been pointless to offer an online converter that requires a local OOo installation.

So I decided to create two new modules that depend on the core module; `jodconverter-local` and `jodconverter-online`.
All the classes required for local conversions were moved into the local module, and all the new classes required for
online conversions were moved into the online module. This way, the online module doesn't have all the OOo libraries as
dependencies, nor the local module has the http-components libraries as dependencies.

Since the `jodconverter-core` project is now just a dependency of both the new modules, former JODConverter users must
now use the `jodconverter-local` in their project, instead of `jodconverter-core`:

### Maven Setup

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

### Gradle Setup

**Old Gradle Setup 4.0.0-RELEASE**

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-core:4.0.0-RELEASE'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-core:4.0.0-RELEASE")
    ```

**New Gradle Setup 4.1.0**

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-local:4.1.0'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-local:4.1.0")
    ```

## Using the new API

Even if the 4.1.0 version is backward compatible (just changing maven dependency from jodconverter-core to
jodconverter-local is enough), JODConverter users are encouraged to use the new API since the classes
`DefaultOfficeManagerBuilder` and `OfficeDocumentConverter` are now deprecated.

Please read the [Usage as a Java Library](https://github.com/sbraconnier/jodconverter/wiki/Java-Library) wiki page to
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

} finally {
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

} finally {
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
the office manager can now be installed and thus it is no longer required to specify which manager is used when
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

