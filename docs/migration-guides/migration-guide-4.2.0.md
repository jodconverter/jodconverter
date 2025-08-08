This guide discusses migration from JODConverter version 4.1.1 to version 4.2.0

## Background

The [LibreOffice Online](https://wiki.documentfoundation.org/Development/LibreOffice_Online) support was introduced in
the 4.1.0 version of JODConverter through
the [jodconverter-online](https://github.com/sbraconnier/jodconverter/tree/master/jodconverter-online) module, but there
was no Spring Boot support for this new module. This was the main goal of the 4.2.0 version of JODConverter. The reason
why the version is 4.2.0 and not 4.1.2 is because a jodconverter-spring-boot-starter user will have to change slightly
its project configuration.

## Spring Boot changes

### Dependencies

The `jodconverter-local` and `jodconverter-online` dependencies has been marked as optional in the
`jodconverter-spring-boot-starter` module. Thus, a user must specify which module(s) they want to use through
dependencies:

#### Maven Setup

**Old Maven Setup 4.1.1**

```xml
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-spring-boot-starter</artifactId>
      <version>4.1.1</version>
   </dependency>
</dependencies>
```

**New Maven Setup 4.2.0**

```xml
<dependencies>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-local</artifactId>
      <version>4.2.0</version>
   </dependency>
   <dependency>
      <groupId>org.jodconverter</groupId>
      <artifactId>jodconverter-spring-boot-starter</artifactId>
      <version>4.2.0</version>
   </dependency>
</dependencies>
```

#### Gradle Setup

**Old Gradle Setup 4.1.1**

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-spring-boot-starter:4.1.1'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-spring-boot-starter:4.1.1")
    ```

**New Gradle Setup 4.2.0**

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-local:4.2.0'
    compile 'org.jodconverter:jodconverter-spring-boot-starter:4.2.0'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-local:4.2.0")
    compile("org.jodconverter:jodconverter-spring-boot-starter:4.2.0")
    ```

### Configurable properties

Since the user can now use both the `jodconverter-local` and `jodconverter-online` modules in a Spring Boot application,
the configurable JODConverter properties prefix has changed. Now there are 2 distinct prefixes; `jodconverter.local` and
`jodconverter.online`.

A user migrating from 4.1.1 to 4.2.0 must use `jodconverter.local` instead of `jodconverter` only.