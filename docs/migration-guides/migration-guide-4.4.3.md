This guide discusses migration from JODConverter version 4.4.2 to version 4.4.3

## New WEB document family.

There is a new document family for better web documents support.

See [here](https://github.com/sbraconnier/jodconverter/issues/297) for more details.

## New builds for OpenOffice and LibreOffice

There are two new JODConverter modules published in the maven center allowing you to easily choose whether you want to use the LibreOffice dependencies or the OpenOffice dependencies in your project. Since the gap between LibreOffice and OpenOffice is increasing each year, you may want to rely only on LibreOffice dependencies, which are not the default.

### Using LibreOffice dependencies

#### Gradle Setup

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-local-lo:4.4.3'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-local-lo:4.4.3")
    ```

#### Maven Setup

```xml
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-local-lo</artifactId>
  <version>4.4.3</version>
</dependency>
```

### Using OpenOffice dependencies

#### Gradle Setup

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-local-oo:4.4.3'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-local-oo:4.4.3")
    ```

or you can continue to use

=== "Groovy"

    ```groovy
    compile 'org.jodconverter:jodconverter-local:4.4.3'
    ```

=== "Kotlin"

    ```kotlin
    compile("org.jodconverter:jodconverter-local:4.4.3")
    ```

#### Maven Setup

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