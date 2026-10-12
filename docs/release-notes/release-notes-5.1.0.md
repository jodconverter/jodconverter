# Changelog

## [v5.1.0](https://github.com/jodconverter/jodconverter/tree/v5.1.0) (2026-10-11)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v5.0.1...v5.1.0)

JODConverter 5.1 adds to 5.0 without changing it: passwords for protected documents, the retry of a task whose office process was lost, four filters, meters for the Spring Boot starter, and a way to ask the office what it is and to use the formats it has. Two conversions that never gave the expected result now fail with a clear message; the [migration guide](../migration-guides/migration-guide-5.1.0.md) describes them.

### **Highlights**

- **Password-protected documents.** `password(String)` on a conversion job, and on a page images job, opens a protected document; the command line tool takes it with `--password`. Without the password, or with a wrong one, the conversion fails with a `PasswordProtectedException` whose message says which of the two ([#545](https://github.com/jodconverter/jodconverter/pull/545)). See [Document Converters](../getting-started/document-converters.md).
- **Task retries.** With `taskRetries` on a local or an attached office manager, a task whose office process was lost while executing it is executed again, on the same process once it is restarted or on another one, instead of failing. Only the conversions from a file to a file are retried, and the default is 0, as before ([#549](https://github.com/jodconverter/jodconverter/pull/549)). The Spring Boot starter has `jodconverter.local.task-retries` and `jodconverter.attached.task-retries`. See [taskRetries](../configuration/local-manager.md#taskretries).
- **Page styles filters.** `HeaderFooterRemoverFilter` removes the page header and footer that the office adds to a CSV file or to a spreadsheet without page setup (the name of the sheet, "Page 1"), and `PageOrientationFilter` sets the pages of a text document or of a spreadsheet to landscape or to portrait ([#566](https://github.com/jodconverter/jodconverter/pull/566)). See [Converting a CSV file](../getting-started/using-filters.md#converting-a-csv-file).
- **Document comparison.** `DocumentComparerFilter` compares the text document being converted with an earlier version of it, and records the differences as tracked changes, as the Compare Document command of the office does ([#571](https://github.com/jodconverter/jodconverter/pull/571)). See [Comparing two versions of a document](../getting-started/using-filters.md#comparing-two-versions-of-a-document).
- **Meters in the Spring Boot starter.** With Micrometer on the classpath, which Spring Boot Actuator brings, the starter exposes the status of the office manager pools as meters: the tasks waiting in the queue, the workers in each state, their restarts, their failed starts and the task retries ([#567](https://github.com/jodconverter/jodconverter/pull/567), [#568](https://github.com/jodconverter/jodconverter/pull/568)). See [Office Managers](../getting-started/office-managers.md).
- **What the office is.** `OfficeInfoTask.query(officeManager)` returns the product name and the version of the office behind a local or an attached manager, and its import and export filters; `jodconverter-cli --info` prints them ([#569](https://github.com/jodconverter/jodconverter/pull/569), [#570](https://github.com/jodconverter/jodconverter/pull/570)). See [Describing the office](../getting-started/office-managers.md#describing-the-office).
- **The formats of the office.** `OfficeFormatRegistry` completes a document format registry with the formats of the office itself, so that a document can be converted to `epub`, `webp` or `docm`, for example, without describing these formats ([#573](https://github.com/jodconverter/jodconverter/pull/573)). See [Formats of the office](../getting-started/document-format-registry.md#formats-of-the-office).

### **Implemented enhancements**

- `password(String)` on `ConversionJob` and on `PageImagesJob`, and the `--password` option of the command line tool.
- `taskRetries(int)` on the builders of `LocalOfficeManager` and `AttachedOfficeManager`; `OfficeTask.isRetryable()`, false by default, for the tasks of an application.
- `OfficeWorkerPoolStatus.taskRetries()`: the number of times a task was executed again since the pool was created. The health indicator of the starter reports it as `taskRetries`.
- `HeaderFooterRemoverFilter`, `PageOrientationFilter` and `DocumentComparerFilter`, with the `header-footer-remover`, `page-orientation` and `document-comparer` filter types of the configuration file of the command line tool.
- `OfficeManagerMetrics`, a Micrometer `MeterBinder`, auto-configured by the starter: `jodconverter.queue.size`, `jodconverter.tasks.retries`, `jodconverter.workers`, `jodconverter.workers.tasks`, `jodconverter.workers.restarts` and `jodconverter.workers.start.failures`, tagged with the name of the office manager bean. `jodconverter.metrics.enabled=false` disables them.
- `OfficeInfoTask`, `OfficeInfo` and `OfficeFilterInfo`, and the `--info` option of the command line tool.
- `OfficeFormatRegistry.create(info)` and `create(registry, info)`.

### **Changed behaviors**

- **Converting a drawing to `vsd` or `vsdx` fails.** The default registry gave these formats the PDF export filter, so the conversion wrote a PDF file with a Visio extension. No office writes Visio files: the two formats are input formats only, and the conversion fails with "Unsupported conversion" ([#576](https://github.com/jodconverter/jodconverter/pull/576)).
- **Converting a text document or a spreadsheet to `svg` fails sooner.** LibreOffice exports presentations and drawings to SVG, and fails to store a whole text document or spreadsheet with the SVG filters it lists for them. The registry no longer offers these conversions, which now fail with "Unsupported conversion" and write nothing, instead of ending in an IO error ([#578](https://github.com/jodconverter/jodconverter/pull/578)).

### **Fixed bugs**

5.1.0 has the three fixes of [5.0.1](release-notes-5.0.1.md), which were made on the way to this version, and no other.

### **Documentation**

- [Security](../getting-started/security.md#harden-the-office-profile): a ready-made office profile that blocks the links of untrusted documents, and the proxy settings of the office. With its default settings, LibreOffice 25.2 puts an image linked to a local file in the converted PDF and requests an image linked to a web address; the profile stops both ([#574](https://github.com/jodconverter/jodconverter/pull/574)).
- [Running in containers](../getting-started/containers.md): the UTF-8 locale that file names with accents need, the emoji font, the hyphenation dictionaries, and the runtime image of the project ([#577](https://github.com/jodconverter/jodconverter/pull/577)).
- [LocalConverter](../configuration/local-converter.md#loaddocumentmodeloaddocumentmode): what a conversion through streams cannot do (the HTML export of a text document loses its embedded images, the HTML export of a presentation is empty), and when to use files instead ([#575](https://github.com/jodconverter/jodconverter/pull/575)).
- [Known LibreOffice issues](../faq.md#known-libreoffice-issues): with `SinglePageSheets`, the rows and columns before the saved scroll position of an `.xlsx` sheet are missing from the PDF, with the conversion in two steps that avoids it ([#572](https://github.com/jodconverter/jodconverter/pull/572)).
- [Supported formats](../getting-started/supported-formats.md): the Visio formats as inputs, SVG as an output of presentations, and the output formats that only an old office writes.
- The README is an introduction with the requirements and a quick start, and the repository has the text of the Apache License 2.0 in `LICENSE` and the copyright lines in `NOTICE`, both packaged in the jars.

### **Project**

- The archives of the command line tool attached to a GitHub release are signed with the key that signs the Maven artifacts; [the security policy](https://github.com/jodconverter/jodconverter/blob/master/.github/SECURITY.md) says how to verify them.
- The build runs on Linux, Windows and macOS again, the code is analyzed with CodeQL, the workflows use pinned actions and read-only tokens, and the repository publishes its [OpenSSF Scorecard](https://scorecard.dev/viewer/?uri=github.com/jodconverter/jodconverter) and has the [OpenSSF Best Practices](https://www.bestpractices.dev/projects/15365) badge.
