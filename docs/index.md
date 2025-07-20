<style>
  .md-content .md-typeset h1 {
    display: none;
  }
</style>

[![Build Status](https://api.cirrus-ci.com/github/jodconverter/jodconverter.svg)](https://cirrus-ci.com/github/jodconverter/jodconverter)
[![Coverage Status](https://coveralls.io/repos/github/jodconverter/jodconverter/badge.svg?branch=master)](https://coveralls.io/github/jodconverter/jodconverter?branch=master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/90c9707226c6406abbea2353274ac773)](https://www.codacy.com/gh/jodconverter/jodconverter/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jodconverter/jodconverter&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local)
[![Javadocs](http://javadoc.io/badge/org.jodconverter/jodconverter-local.svg)](http://javadoc.io/doc/org.jodconverter/jodconverter-local)
[![Join the chat at https://gitter.im/jodconverter/Lobby](https://badges.gitter.im/jodconverter/Lobby.svg)](https://gitter.im/jodconverter/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XUYFM5NLLK628)

<div style="text-align: center;">
  <img src="assets/logo-with-text.png" alt="Logo" style="max-width: 100%; width: 500px; height: auto;" />
</div>

### Overview

**JODConverter**, the Java OpenDocument Converter, converts documents between different office formats.
It leverages [LibreOffice](https://www.libreoffice.org) or [Apache OpenOffice](https://www.openoffice.org), which
provide arguably the best free import/export filters for OpenDocument and Microsoft Office formats available today.

**JODConverter** automates all conversions supported by LibreOffice/OpenOffice. Supported conversions include
**(but not limited to. All conversions supported by your OOo installation is supported by JODConverter)**:

| Document Type | Input Format                         | Output Format                                                  |
|---------------|--------------------------------------|----------------------------------------------------------------|
| Text          | DOC, DOCX, ODT, OTT, RTF, TEXT, etc. | DOC, DOCX, HTML, JPG, ODT, OTT, FODT, PDF, PNG, RTF, TXT, etc. |
| Spreadsheet   | CSV, ODS, OTS, TSV, XLS, XLSX, etc.  | CSV, HTML, JPG, ODS, OTS, FODS, PDF, PNG, TSV, XLS, XLSX, etc. |
| Presentation  | ODP, OTP, PPT, PPTX, etc.            | GIF, HTML, JPG, ODP, OTP, FODP, PDF, PNG, PPT, PPTX, BMP, etc. |
| Drawing       | ODG, OTG, etc.                       | GIF, JPG, ODG, OTG, FODG, PDF, PNG, SVG, TIF, VSD, BMP, etc.   |
| Other         | HTML                                 | DOC, DOCX, HTML, JPG, ODT, OTT, FODT, PDF, PNG, RTF, TXT, etc. |

**JODConverter** can be used in different ways:

- As a [Java library](getting-started/java-library.md), embedded in your own Java application (Web or not).
- As a [command line tool](getting-started/command-line-tool.md), possibly invoked from your own scripts.

---

### Modules

The **JODConverter** project contains 8 modules.

#### jodconverter-cli

The `jodconverter-cli` module provides a standalone command-line tool for converting documents using LibreOffice or
OpenOffice. It enables quick and easy file conversions without writing any Java code, making it ideal for automation,
scripting, and server-side integrations.

See the [dedicated page](getting-started/command-line-tool.md) to know more about this tool.

#### jodconverter-core

[Dependencies](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-core)

The `jodconverter-core` module provides the core abstractions, used by **JODConverter** module implementations,
such as `jodconverter-local` or `jodconverter-remote`. It abstracts the complexity of working with office managers,
document formats, and conversion pipelines.

#### jodconverter-local

[Dependencies](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local)

The `jodconverter-local` module builds on top of the `jodconverter-core` module and provides a ready-to-use
implementation that connects to a locally installed instance of LibreOffice or Apache OpenOffice to perform document
conversions.

This module handles the lifecycle of the office process, manages one or more office instances, and provides a
convenient API to convert documents using the local desktop installation of an office suite—without requiring
the user to manually start or manage OOo in headless mode.

See the [Java Library section](getting-started/java-library.md) to know more.

#### jodconverter-local-oo

[Dependencies](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local-oo)

The `jodconverter-local-oo` module provides a variant of `jodconverter-local` that is packaged with dependencies
targeting Apache OpenOffice. It contains no additional code, but includes the OpenOffice UNO libraries instead of
the LibreOffice ones.

Use this module when your application needs to convert documents using an Apache OpenOffice installation.

!!! note

    Internally, both `jodconverter-local-oo` and `jodconverter-local-lo` delegate to the same codebase
    (`jodconverter-local`). The only difference lies in the dependencies declared in their build files.

Using this module is in fact the exact same thing as using the `jodconverter-local` module. By default,
**JODConverter** is built using the OpenOffice libraries. See
[here](https://github.com/jodconverter/jodconverter/issues/113) to know why.

#### jodconverter-local-lo

[Dependencies](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-local-lo)

The `jodconverter-local-lo` module is a variant of `jodconverter-local` that packages the project with LibreOffice UNO
libraries instead of the default Apache OpenOffice ones. Like `jodconverter-local-oo`, it contains no additional Java
code—only a different set of dependencies defined in its build.gradle.kts.

Use this module if your application is intended to run with LibreOffice, which is generally recommended due to its more
active development and broader format support.

#### jodconverter-remote

[Dependencies](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-remote)

The `jodconverter-remote` module is a Java client library designed to perform document conversions by connecting to a
remote document conversion REST API, such as those exposed by LibreOffice Online or Collabora Online.

Instead of managing a local or UNO-based office process, this module sends documents and conversion requests over
HTTP(S) to a remote server that handles the conversion, making it ideal for cloud-native or containerized environments.

#### jodconverter-spring

[Dependencies](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-spring)

The `jodconverter-spring` module provides seamless integration of **JODConverter** with the Spring Framework, enabling
developers to easily configure and use document conversion services within Spring-based applications.

This module offers Spring-friendly beans and configuration support for managing office processes and document
converters, helping you embed **JODConverter** capabilities in your web applications, microservices, or backend systems
built with Spring.

#### jodconverter-spring-boot-starter

[Dependencies](https://maven-badges.herokuapp.com/maven-central/org.jodconverter/jodconverter-spring-boot-starter)

The `jodconverter-spring-boot-starter` module provides a convenient Spring Boot starter that simplifies integrating
**JODConverter**’s document conversion capabilities into Spring Boot applications.

It auto-configures and manages all necessary beans, including the office manager and document converter, based on
sensible defaults and externalized configuration properties, allowing developers to quickly enable document conversion
with minimal setup.

---

### Support <sup>&#128172;</sup>

**JODConverter** Gitter
Community [![Join the chat at https://gitter.im/jodconverter/Lobby](https://badges.gitter.im/jodconverter/Lobby.svg)](https://gitter.im/jodconverter/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge),
growing [FAQ](faq.md).

---

### Original **JODConverter**

**JODConverter** (Java OpenDocument Converter) automates document conversions using LibreOffice or OpenOffice.org.

The previous home for this project is at [Google Code](http://code.google.com/p/jodconverter/),
including some [wiki pages](https://code.google.com/archive/p/jodconverter/wikis).

---

### Donations

If this project helps you, please consider a cup of &#9749;. Thanks!! &#128150;

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XUYFM5NLLK628)
