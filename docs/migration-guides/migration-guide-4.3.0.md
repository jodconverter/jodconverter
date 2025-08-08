This guide discusses migration from JODConverter version 4.2.4 to version 4.3.0

## Package names refactoring

All the jodconverter modules now have their own base package name, to
fix [#178](https://github.com/sbraconnier/jodconverter/issues/178). This means that you'll have to refactor all the
imports that cannot be resolved anymore. The best way to do that is to remove the old import and to allow the IDE you
are working with to resolve the new import.

Example:
The class: `org.jodconverter.LocalConverter` is now `org.jodconverter.local.LocalConverter`

## Deprecated classes removed

* `org.jodconverter.filter.text.PageCounterFilter`. Please use `org.jodconverter.local.filter.PagesCounterFilter`
* `org.jodconverter.filter.text.PageSelectorFilter`. Please use `org.jodconverter.local.filter.PagesSelectorFilter`
* `org.jodconverter.office.LocalOfficeUtils#closeQuietly`. Please use
  `org.jodconverter.core.office.OfficeUtils#closeQuietly`

## New jodconverter-remote module

The jodconverter-online module was a contribution made by the LibreOffice Online team. But the name was confusing since
most people thought that this module could be used as a server processing conversion requests. But it is in fact a
client that can send conversion requests to a server. Hopefully, this new name will clarify the purpose of the module.

### Maven Setup

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

### Gradle Setup

**Old Gradle Setup 4.2.4**

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-online:4.2.4'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-online:4.2.4")
    ```

**New Gradle Setup 4.3.0**

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-remote:4.3.0'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-remote:4.3.0")
    ```

