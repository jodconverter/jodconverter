<style>
  .md-content .md-typeset h1 {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
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

Looking for the list of JODConverter modules? See Getting Started: [Modules](getting-started/modules.md) for a concise
overview of all modules and when to use each one.

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
