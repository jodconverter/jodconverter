# JODConverter EX (EXtended, EXtra, 'EX'cetera)

I created this fork because I had to do some changes to the original project and I want to share my work with the community.

In order to be able to apply changes to the JODConverter project, I had to understand the source code first. So, for those who may have take a look at the original source code, you'll be happy to see a lot more comments in this version. I tried to be as meaningful as possible along the way, but comments (about the comments!) are always welcome. Please note that my first language is not English.

## What you want to know...

- **SIGAR**: This fork does not depend on SIGAR. JODConverter only needs to retrieve office processes (PIDs) and kill office processes (using PID). It should work just fine without SIGAR with the actual process managers base on an older version of JODConverter. But I added the ability to use any process manager you would like to implement. More details on this feature later. For now, just look at the DefaultOfficeManagerBuilder class documentation. 

- **Dependencies**: The core project of this fork depends on slf4j, commons-lang3, commons-io, and the required LibreOffice libraries (for now, everything seems to work nice with OpenOffice too).

- **Tests**: This fork has been tested on Windows and Ubuntu. But JODConverter is supposed to work just fine on Mac and other Unix/Linux OS. Any confirmation would be welcome so we could build a list of official supported OS.

## Usage

### Using Gradle:
```Shell
compile 'org.jodconverter:jodconverter-core:4.0.0-RELEASE"
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

## Thanks To...

I cannot say exactly where you could find some similitude between my source code and the one of these forks I tried to use before I created my own but here are my favorite/inspiration forks:

- [XWiki fork](https://github.com/xwiki/jodconverter): Nice choice if you can afford using the SIGAR tools (which I couldn't sadly).
- [Nuxeo fork](https://github.com/nuxeo/jodconverter): Nice choice if you don't want to use SIGAR and don't came across an unresponsive office process following a timeout exception that drives you crazy.

### A special thanks to:
[@michelole](https://github.com/michelole) who created a pull request in these two forks with a stress test that made my office process to crash every times! I don't know him but my first commit was when I was able to build my things with his stress test on!!  

## Original JODConverter

JODConverter (Java OpenDocument Converter) automates document conversions using LibreOffice or OpenOffice.org.

The previous home for this project is at [Google Code](http://code.google.com/p/jodconverter/),
including some [wiki pages](https://code.google.com/archive/p/jodconverter/wikis).
