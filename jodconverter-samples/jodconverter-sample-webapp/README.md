## JODConverter - Sample - Webapp

This is a sample web application that uses the local module of the Java OpenDocument Converter (JODConverter) project.

### Running the Project using gradle

First, [build the entire jodconverter project](https://github.com/jodconverter/jodconverter#building-the-project)

Then, run

```Shell
gradlew :jodconverter-samples:jodconverter-sample-webapp:tomcatRun
```

Once started, use your favorite browser and visit this page:

```
http://localhost:8080/jodconverter-sample-webapp/
```

Happy conversions!!