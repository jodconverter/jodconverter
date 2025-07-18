# Basic Web Application

A basic example of a web application is available in the
[JODConverter Samples](https://github.com/jodconverter/jodconverter-samples) repository;
the [basic-webapp](https://github.com/jodconverter/jodconverter-samples/tree/main/samples/basic-webapp) module.

The most important concept is that you should start your OfficeManager instance(s) when your webapp starts,
stop it(them), when your webapp stops, and share it(them) across all requests. The OfficeManager(s) will take
care of multi-threading.

The sample is a web application that uses the local module
[jodconverter-local-lo](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local-lo) library of
the JODConverter project, which means the
[jodconverter-local](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local-lo) built with the
LibreOffice dependencies.

### Running the project using Gradle

First, build the project:

```shell
gradlew :samples:basic-webapp:build
```

Then, run:

```shell
gradlew :samples:basic-webapp:tomcatRun
```

If you experience a connection issue on Windows, you may have to set a system property pointing to a
templateProfileDir where OpenGL is disabled by default. Read
[this](https://wiki.documentfoundation.org/OpenGL#:~:text=LibreOffice%205.3%20and%20newer%3A,Click%20%22Apply%20Changes%20and%20Restart%22)
to know how to disable OpenGL. Then, run:

```shell
gradlew :samples:basic-webapp:tomcatRun -Dorg.jodconverter.local.manager.templateProfileDir=<path to your directory>
```

Once started, use your favorite browser and visit this page:

```
http://localhost:8080/jodconverter-sample-webapp/
```

Happy conversions!!