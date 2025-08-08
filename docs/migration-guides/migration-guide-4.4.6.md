This guide discusses migration from JODConverter version 4.4.5 to version 4.4.6

## Custom document formats support.

You can now provide a `custom-document-formats.json` file as a resource in your project to customize the document
formats supported by jodconverter instead of overwriting the whole `document-formats.json`. The formats specified in the
`custom-document-formats.json` file will be added to the main registry.

## Spring Boot 3 support.

This version of jodconverter can be used with Spring Boot 3.0+

## New jodconverter-samples repository

The jodconverter-samples module has been moved to
a [dedicated repository](https://github.com/jodconverter/jodconverter-samples).

We hope that more examples will be added to this repository over time.