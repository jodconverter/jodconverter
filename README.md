# <img src="https://github.com/jodconverter/jodconverter/wiki/images/jodconverter_w200.png">&nbsp;<sup>&nbsp;LibreOffice</sup>&nbsp;/&nbsp;<sub>Apache OpenOffice</sub>

[![Build Status](https://api.cirrus-ci.com/github/jodconverter/jodconverter.svg)](https://cirrus-ci.com/github/jodconverter/jodconverter)
[![Coverage Status](https://coveralls.io/repos/github/jodconverter/jodconverter/badge.svg?branch=master)](https://coveralls.io/github/jodconverter/jodconverter?branch=master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/ea060b9c1ff04e188c9265ed4f1b7bef)](https://www.codacy.com/gh/jodconverter/jodconverter/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jodconverter/jodconverter&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local)
[![Javadocs](http://javadoc.io/badge/org.jodconverter/jodconverter-local.svg)](http://javadoc.io/doc/org.jodconverter/jodconverter-local)
[![Join the chat at https://gitter.im/jodconverter/Lobby](https://badges.gitter.im/jodconverter/Lobby.svg)](https://gitter.im/jodconverter/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XUYFM5NLLK628)

I created this fork years ago because I had to do some changes to the original project and decided to share my work with the community.

### What you want to know...

- **Documentation**: The JODConverter documentation (work in progress) can be found [here](https://github.com/jodconverter/jodconverter/wiki).
- **Dependencies**:
  * [jodconverter-local](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local) module dependencies. This fork does not depend on SIGAR. JODConverter only needs to retrieve office processes (PIDs) and kill office processes (using PID). It should work just fine without SIGAR with the actual [process managers](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local/src/main/java/org/jodconverter/process) created from an older version of JODConverter. But I added the ability to use any process manager you would like to implement if none of the provided managers can be used. See the [processManager](https://github.com/jodconverter/jodconverter/wiki/Configuration#capital_abcdprocessmanager) configuration option.
  * [jodconverter-remote](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-remote) module dependencies.
  * [jodconverter-spring](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-spring) module dependencies.
  * [jodconverter-spring-boot-starter](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-spring-boot-starter) module dependencies.
- **Tests**: JODConverter is supposed to work just fine on recent versions of Windows, MacOS and Unix/Linux. Any confirmation would be welcome, so we could build a list of official supported OS distributions.

### Usage for local conversions

Build default, JODConverter is built using the OpenOffice libraries. See [here](https://github.com/jodconverter/jodconverter/issues/113) to know why. But you can now decide whether you want to use JODConverter with the LibreOffice libraries or the OpenOffice libraries. 

#### With LibreOffice libraries:

#### Gradle:
```Shell
implementation 'org.jodconverter:jodconverter-local-lo:4.4.5'
```

#### Maven:
```Shell
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-local-lo</artifactId>
  <version>4.4.5</version>
</dependency>
```

#### With OpenOffice libraries:

#### Gradle:
```Shell
implementation 'org.jodconverter:jodconverter-local:4.4.5'
```
or
```Shell
implementation 'org.jodconverter:jodconverter-local-oo:4.4.5'
```

#### Maven:
```Shell
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-local</artifactId>
  <version>4.4.5</version>
</dependency>
```
or
```Shell
<dependency>
  <groupId>org.jodconverter</groupId>
  <artifactId>jodconverter-local-oo</artifactId>
  <version>4.4.5</version>
</dependency>
```

### Building the Project

```Shell
gradlew clean build -x test
```

### Building Cli Executable

```Shell
gradlew clean build -x test distZip
```

#### Support <sup><sup>:speech_balloon:</sup></sup>

JODConverter Gitter Community [![Join the chat at https://gitter.im/jodconverter/Lobby](https://badges.gitter.im/jodconverter/Lobby.svg)](https://gitter.im/jodconverter/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge), growing [FAQ](https://github.com/jodconverter/jodconverter/wiki/FAQ).

### How to contribute

1. Check for [open issues](https://github.com/jodconverter/jodconverter/issues), or open a new issue to start a discussion around a feature idea or a bug.
2. If you feel uncomfortable or uncertain about an issue or your changes, feel free to contact me on Gitter using the link above.
3. [Fork this repository](https://help.github.com/articles/fork-a-repo/) on GitHub to start making your changes.
4. Write a test showing that the bug was fixed or that the feature works as expected.
5. Note that the repository follows the [Google Java style](https://google.github.io/styleguide/javaguide.html). You can format your code to this format by typing gradlew spotlessApply on the subproject you work on (e.g, `gradlew :jodconverter-local:spotlessApply`), by using the [Eclipse plugin](https://github.com/google/google-java-format#eclipse), or by using the [Intellij plugin](https://github.com/google/google-java-format#intellij).
6. [Create a pull request](https://help.github.com/articles/creating-a-pull-request/), and wait until it gets merged and published.

## Credits...

Here are my favorite/inspiration forks/projects:

- [documents4j project](https://github.com/documents4j/documents4j): Nice choice if you want 100% perfect conversion using MS Office. But work only on Windows out of the box (Local implementation) and not totally free (since MS Office is not free). The new "job" package is strongly inspired by this project.

## Original JODConverter

JODConverter (Java OpenDocument Converter) automates document conversions using LibreOffice or OpenOffice.org.

The previous home for this project is at [Google Code](http://code.google.com/p/jodconverter/),
including some [wiki pages](https://code.google.com/archive/p/jodconverter/wikis).

## Donations

If this project helps you, please consider a donation. I have to pay for the jodconverter website (domain and hosting) every year. I also sit on my chair in front of a monitor a lot instead of being outside drinking a :beer: to improve this project :smiley:. Thanks!! :heart:

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XUYFM5NLLK628)
