This guide discusses migration from JODConverter version 5.0.1 to version 5.1.0

## Background

This release adds features and removes nothing: an application that compiles with 5.0.1 compiles with 5.1.0. Three points may still need a look.

## Conversions that now fail

Two kinds of conversions that never gave the expected result are no longer offered by the default document format registry. They fail with an `OfficeException` whose cause says "Unsupported conversion", and nothing is written.

- **A drawing to `vsd` or `vsdx`.** The conversion wrote a PDF file with a Visio extension, since no office writes Visio files. If you relied on it, convert to `pdf`.
- **A text document or a spreadsheet to `svg`.** The conversion failed already, later and with an IO error (`Could not store document ...; errorCode: ...`), because LibreOffice only exports presentations and drawings to SVG. Only the error changes.

If you use an office that does one of these conversions, describe the format in a registry of your own; see [Document Format Registry](../getting-started/document-format-registry.md).

## The status of a pool has a third value

`OfficeWorkerPoolStatus` is a record, and it has a new component, `taskRetries`. Code that reads a status is not affected, and the constructor with two arguments is kept. Code that takes a status apart with a record pattern, `OfficeWorkerPoolStatus(var workers, var queueSize)`, must name the third component.

## New meters with Spring Boot Actuator

An application that has the Spring Boot starter and Micrometer, which Spring Boot Actuator brings, gets the `jodconverter.*` meters without asking for them. Set `jodconverter.metrics.enabled=false` to leave them out.

See the [release notes](../release-notes/release-notes-5.1.0.md).
