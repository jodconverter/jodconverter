# Documentation

## Table of Contents

- General
  - [Requirements](general/requirements.md)
  - [Supported Formats](general/supported-formats.md)
  - [Performance](general/performance.md)
  - [FAQ](general/faq.md)
- Usage
  - [Configuration](usage/configuration.md)
  - [Command Line Tool](usage/command-line.md)
  - [Java Library](usage/java-library.md)
  - [Web Application](usage/web-application.md)

## Overview

**JODConverter**, the Java OpenDocument Converter, converts documents between different office formats.
It leverages [Apache OpenOffice](https://www.openoffice.org) or [LibreOffice](https://www.libreoffice.org), which provide arguably the best free import/export filters for OpenDocument and Microsoft Office formats available today.

*JODConverter* automates all conversions supported by OpenOffice/LibreOffice. Supported conversions include:

| Document Type | Input Format               | Output Format                            |
| ------------- | -------------------------- | ---------------------------------------- |
| Text          | DOC, DOCX, ODT, RTF, TEXT  | DOC, DOCX, HTML, ODT, PDF, PNG, RTF, TXT |
| Spreadsheet   | CSV, ODS, TSV, XLS, XLSX   | CSV, HTML, ODS, PDF, PNG, TSV, XLS, XLSX |
| Presentation  | ODP, PPT, PPTX             | HTML, ODP, PDF, PNG, PPT, PPTX, SWF      |
| Drawing       | ODG                        | ODG, PDF, PNG, SWF                       |
| Other         | HTML                       | DOC, DOCX, HTML, ODT, PDF, PNG, RTF, TXT |


*JODConverter* can be used in many different ways:

- As a Java library, embedded in your own Java application.
- As a command line tool, possibly invoked from your own scripts.
- As a simple web application: upload your input document, select the desired format and download the converted version.