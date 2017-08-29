# <img src="https://github.com/sbraconnier/jodconverter/wiki/images/jodconverter_w200.png">&nbsp;<sup>&nbsp;LibreOffice</sup>&nbsp;/&nbsp;<sub>Apache OpenOffice</sub>

[![Build status](https://ci.appveyor.com/api/projects/status/mvn8oqr2m8xorslk?svg=true)](https://ci.appveyor.com/project/sbraconnier/jodconverter)
[![Build Status](https://travis-ci.org/sbraconnier/jodconverter.svg?branch=master)](https://travis-ci.org/sbraconnier/jodconverter)
[![Coverage Status](https://coveralls.io/repos/github/sbraconnier/jodconverter/badge.svg?branch=master)](https://coveralls.io/github/sbraconnier/jodconverter?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/20f3adafce26439fb6f38a7767388944)](https://www.codacy.com/app/sbraconnier/jodconverter?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sbraconnier/jodconverter&amp;utm_campaign=Badge_Grade)
[![Dependency Status](https://www.versioneye.com/user/projects/596d252e6725bd000e2d8b3b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/596d252e6725bd000e2d8b3b)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-core)
[![Javadocs](http://javadoc.io/badge/org.jodconverter/jodconverter-core.svg)](http://javadoc.io/doc/org.jodconverter/jodconverter-core)
[![Join the chat at https://gitter.im/jodconverter/Lobby](https://badges.gitter.im/jodconverter/Lobby.svg)](https://gitter.im/jodconverter/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

I created this fork because I had to do some changes to the original project and now it is time to share my work with the community.

### What you want to know...

- **Documentation**: The JODConverter documentation (work in progress) can be found [here](https://github.com/sbraconnier/jodconverter/wiki).
- **Dependencies**: See [this](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-core) for core project dependencies. This fork does not depend on SIGAR. JODConverter only needs to retrieve office processes (PIDs) and kill office processes (using PID). It should work just fine without SIGAR with the actual [process managers](https://github.com/sbraconnier/jodconverter/tree/master/jodconverter-core/src/main/java/org/jodconverter/process) created from an older version of JODConverter. But I added the ability to use any process manager you would like to implement if none of the provided managers can be used. See the [processManager](https://github.com/sbraconnier/jodconverter/wiki/Configuration#capital_abcdprocessmanager) configuration option.
- **Tests**: JODConverter is supposed to work just fine on recent versions of Windows, MacOS and Unix/Linux. Any confirmation would be welcome so we could build a list of official supported OS distributions.
supported OS

### Usage

#### Gradle:
```Shell
compile 'org.jodconverter:jodconverter-core:4.0.0-RELEASE'
```

#### Maven:
```Shell
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-core</artifactId>
  <version>4.0.0-RELEASE</version>
</dependency>
```

### Building the Project

#### With LibreOffice libraries:
```Shell
gradlew clean build -x test -x integTest
```

#### With OpenOffice libraries:
```Shell
gradlew clean build -x test -x integTest -PuseOpenOffice
```

### Building Cli Executable

```Shell
gradlew clean build -x test -x integTest distZip
```

#### Support <sup><sup>:speech_balloon:</sup></sup>

JODConverter Gitter Community [![Join the chat at https://gitter.im/jodconverter/Lobby](https://badges.gitter.im/jodconverter/Lobby.svg)](https://gitter.im/jodconverter/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge), growing [FAQ](https://github.com/sbraconnier/jodconverter/wiki/FAQ).

### How to contribute

1. Check for [open issues](https://github.com/sbraconnier/jodconverter/issues), or open a new issue to start a discussion around a feature idea or a bug.
2. If you feel uncomfortable or uncertain about an issue or your changes, feel free to contact me on Gitter using the link above.
3. [Fork this repository](https://help.github.com/articles/fork-a-repo/) on GitHub to start making your changes.
4. Write a test showing that the bug was fixed or that the feature works as expected.
5. Note that the repository follows the [Google Java style](https://google.github.io/styleguide/javaguide.html). You can format your code to this format by typing gradlew spotlessApply on the subproject you work on (e.g, `gradlew :jodconverter-local:spotlessApply`), by using the [Eclipse plugin](https://github.com/google/google-java-format#eclipse), or by using the [Intellij plugin](https://github.com/google/google-java-format#intellij).
6. [Create a pull request](https://help.github.com/articles/creating-a-pull-request/), and wait until it gets merged and published.

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
