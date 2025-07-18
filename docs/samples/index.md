# Samples

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Here you will find sample projects using [JODConverter](https://github.com/jodconverter/jodconverter).

The projects demonstrate typical use cases using the JODConverter project. They include:

- [`samples/basic-webapp`](basic-webapp.md): Demonstrates how to use the
  [jodconverter-local-lo](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local-lo) module to
  build a basic web application.
- [`samples/spring-boot-rest`](spring-boot-rest.md): Demonstrates how to use
  the [jodconverter-spring-boot-starter](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-spring-boot-starter)
  module to build a REST API supporting document conversions.
- [`samples/spring-boot-webapp`](spring-boot-webapp.md): Demonstrates how to use
  the [jodconverter-spring-boot-starter](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-spring-boot-starter)
  module to build a web application with thymeleaf and bootstrap.

## Build and run

See the subprojects. In short

```shell
# run webapp
./gradlew :samples:spring-boot-webapp:bootRun
# build rest-app
./gradlew :samples:spring-boot-rest:bootRun
```

You can access the apps via `http://localhost:8080`

### Docker based

If you do not have a local java SDK or just prefer docker, use

```
# run webapp
docker run --rm -p 8080:8080 ghcr.io/jodconverter/jodconverter-examples:gui

# unr rest-only app
docker run --rm -p 8080:8080 ghcr.io/jodconverter/jodconverter-examples:rest
```

You can access the apps via `http://localhost:8080`

The docker images are build
in [jodconverter/docker-image-jodconverter-examples](https://github.com/jodconverter/docker-image-jodconverter-examples).
Use them as a starting point for an application, if you like.