# Spring Boot REST API

???+ tip "Update"

    See [here](https://github.com/EugenMayer/converter) for an improved version, meaning production ready, of this project!

A basic example of a Spring Boot REST API is available in the
[JODConverter Samples](https://github.com/jodconverter/jodconverter-samples) repository; the
[spring-boot-rest](https://github.com/jodconverter/jodconverter-samples/tree/main/samples/spring-boot-rest) module.

This is a sample application of a REST API that uses the spring boot integration module,
[jodconverter-spring-boot-starter](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-spring-boot-starter),
with the local module
[jodconverter-local-lo](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local-lo) library of
the JODConverter project, which means the
[jodconverter-local](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local-lo) built with the
LibreOffice dependencies.

The goal was to emulate a Collabora Online server that would support the customization of custom (known) load/store
properties.

!!! note

    The exposed REST API interface imitates the Collabora Online REST interface (/lool), you could think of this
    example as a Collabora Online server.

### Running the project using Gradle

First, build the project:

```shell
gradlew :samples:spring-boot-rest:build
```

Then, run:

```shell
gradlew :samples:spring-boot-rest:bootRun
```

If you experience a connection issue on Windows, you may have to set a system property pointing to a
templateProfileDir where OpenGL is disabled by default. Read
[this](https://wiki.documentfoundation.org/OpenGL#:~:text=LibreOffice%205.3%20and%20newer%3A,Click%20%22Apply%20Changes%20and%20Restart%22)
to know how to disable OpenGL. Then, run:

```shell
gradlew :samples:spring-boot-rest:bootRun -Dorg.jodconverter.local.manager.templateProfileDir=<path to your directory>
```

Once started, use your favorite browser and visit this page:

```
http://localhost:8080/
```

Happy conversions!!