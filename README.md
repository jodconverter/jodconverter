# JODConverter&nbsp;<sup>&nbsp;LibreOffice</sup>&nbsp;/&nbsp;<sub>Apache OpenOffice</sub>

[![Build status](https://ci.appveyor.com/api/projects/status/mvn8oqr2m8xorslk?svg=true)](https://ci.appveyor.com/project/sbraconnier/jodconverter)
[![Build Status](https://travis-ci.org/sbraconnier/jodconverter.svg?branch=master)](https://travis-ci.org/sbraconnier/jodconverter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-core)
[![Javadocs](http://javadoc.io/badge/org.jodconverter/jodconverter-core.svg)](http://javadoc.io/doc/org.jodconverter/jodconverter-core)

I created this fork because I had to do some changes to the original project and now it is time to share my work with the community.

## What you want to know...

- **Documentation**: The JODConverter documentation (work in progress) can be found [here](https://github.com/sbraconnier/jodconverter/wiki). 
- **SIGAR**: This fork does not depend on SIGAR. JODConverter only needs to retrieve office processes (PIDs) and kill office processes (using PID). It should work just fine without SIGAR with the actual process managers base on an older version of JODConverter. But I added the ability to use any process manager you would like to implement. More details on this feature later. For now, just look at the DefaultOfficeManagerBuilder class documentation. 
- **Dependencies**: See [this](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-core) for core project dependencies.
- **Tests**: JODConverter is supposed to work just fine on Windows, MacOS and Unix/Linux. Any confirmation would be welcome so we could build a list of official supported OS distributions.

## Usage

### Using Gradle:
```Shell
compile 'org.jodconverter:jodconverter-core:4.0.0-RELEASE'
```

### Using Maven:
```Shell
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-core</artifactId>
  <version>4.0.0-RELEASE</version>
</dependency>
```

## Building the Project

### Using LibreOffice libraries:
```Shell
gradlew clean build -x test -x integTest
```

### Using OpenOffice libraries:
```Shell
gradlew clean build -x test -x integTest -PuseOpenOffice
```

## Building Cli Executable

```Shell
gradlew clean build -x test -x integTest distZip
```

## Credits...

Here are my favorite/inspiration forks/projects:

- [XWiki fork](https://github.com/xwiki/jodconverter): Nice choice if you can afford using the SIGAR tools (which I couldn't sadly). The **filter** package is strongly inspired by this project.
- [Nuxeo fork](https://github.com/nuxeo/jodconverter): Nice choice if you don't want to use SIGAR and don't came across an unresponsive office process following a timeout exception that drives you crazy.
- [documents4j project](https://github.com/documents4j/documents4j): Nice choice if you want 100% perfect conversion using MS Office. But work only on Windows out of the box (Local implementation) and not totally free (since MS Office is not free). The new "job" package is strongly inspired by this project.

### A special thanks to:

[@michelole](https://github.com/michelole) who created a pull request in these two forks with a [stress test](https://github.com/sbraconnier/jodconverter/blob/master/jodconverter-core/src/integTest/java/org/jodconverter/StressITest.java) that made my office process to crash every times! My first commit was when I was able to build my things with his stress test on!!  

## Original JODConverter

JODConverter (Java OpenDocument Converter) automates document conversions using LibreOffice or OpenOffice.org.

The previous home for this project is at [Google Code](http://code.google.com/p/jodconverter/),
including some [wiki pages](https://code.google.com/archive/p/jodconverter/wikis).
