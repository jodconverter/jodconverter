# Changelog

## [v5.0.0](https://github.com/jodconverter/jodconverter/tree/v5.0.0) (2026-10-08)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.11...v5.0.0)

JODConverter 5.0 is the first major version since 4.0: it moves to **Java 17**, **Spring Boot 4** and **SLF4J 2**,
rewrites the pool of office processes, and adds typed PDF options, asynchronous conversions, a pool size, document
merging and the export of slides to images. The changes that need an action in an application are described in the
[migration guide](../migration-guides/migration-guide-5.0.0.md).

### **Highlights**

- **Java 17 baseline.** The library, the command line tool and the Spring Boot starter require Java 17 at build time
    and at runtime, and use the language and the APIs of that version. Applications on Java 8 or 11 stay on 4.4.
- **Spring Boot 4.** The starter is built against Spring Boot 4.1 and Spring Framework 7. Its properties classes are
    records, the timeouts are bound as `Duration`, the document format registry is configured once under
    `jodconverter.document-formats`, the attached office manager is auto-configured by the `jodconverter.attached.*`
    properties, and a `jodconverter` health indicator reports the state of the office processes.
- **Office worker pool.** The pool of office processes is rewritten around a single queue and one worker per process
    or connection: a process that is starting or restarting is never given a task while another one is free, a task
    that exceeds its execution timeout is cancelled and its process restarted, a process that lost its connection is
    killed after two seconds instead of the whole process timeout, and `getStatus()` gives a snapshot of the pool.
- **PDF options.** `PdfOptions` gives typed options for the conversions to PDF (PDF/A, PDF/UA, image compression,
    page range, passwords and permissions, watermark, digital signature, initial view...), with presets. The command
    line tool takes them with `--pdf-preset` and `--pdf-option`, the Spring Boot starter with the `jodconverter.pdf.*`
    properties.
- **Asynchronous conversions.** `executeAsync()` on a conversion job returns a `CompletableFuture`, and
    `OfficeManager.submit(task)` does the same for a task.
- **Slides to images.** `LocalConverter.exportPages(document).to(directory)` exports each slide or draw page as its own
    image (PNG, JPEG, SVG, GIF, BMP, TIFF, WebP), which replaces the one-image-per-slide output that LibreOffice 24.2
    removed with its HTML export wizard.
- **Document merging.** `LocalConverter.merge(first, others...).to(target)` converts several text documents into one
    output, each one starting on a new page.
- **Process management without a process listing.** The office processes are followed through their `ProcessHandle`:
    no more `ps`, `wmic` or PowerShell command after each start, and the Windows manager no longer depends on `wmic`,
    which recent Windows versions removed. Office processes can be started through a launcher (a snap or an AppImage)
    with `officeExecutable`, a `poolSize` starts a number of processes on free ports, and a port used by another
    program fails the start right away.
- **Smaller footprint.** The remote module uses the HTTP client of the JDK instead of Apache HttpClient, the command
    line tool no longer embeds the Spring Framework (its configuration is a JSON or YAML file), the library modules
    no longer import the Spring Boot BOM, and the `jodconverter-spring` module is removed.

### **Implemented enhancements**

- Export each slide or draw page of a presentation to its own image (PNG, JPEG, SVG)
    [#504](https://github.com/jodconverter/jodconverter/issues/504)
- Support snap installation of LO [#448](https://github.com/jodconverter/jodconverter/issues/448)
- PDF/A Conformance level for pdf export [#447](https://github.com/jodconverter/jodconverter/issues/447)
- Add support for Markdown [#444](https://github.com/jodconverter/jodconverter/issues/444)
- Regarding the PID retrieval issue, it's returning null, and the PID cannot be obtained.
    [#440](https://github.com/jodconverter/jodconverter/issues/440)
- AbstractOfficeManagerPool lacks accessor method for tempDir field
    [#434](https://github.com/jodconverter/jodconverter/issues/434)
- Add the ability to set the pool size and auto-detect free port numbers.
    [#432](https://github.com/jodconverter/jodconverter/issues/432)
- Conversion of ppt doesn't work anymore with LibreOffice 24.2.4.2
    [#396](https://github.com/jodconverter/jodconverter/issues/396)
- CompletableFuture [#393](https://github.com/jodconverter/jodconverter/issues/393)

### **Fixed bugs**

- Timed-out task is never cancelled and blocks its pool entry
    [#453](https://github.com/jodconverter/jodconverter/issues/453)
- Pool hands out entries that are restarting, so tasks wait under their execution timeout
    [#451](https://github.com/jodconverter/jodconverter/issues/451)
- WindowsProcessManager silently falls back to PureJavaProcessManager on recent Windows (wmic.exe removed)
    [#450](https://github.com/jodconverter/jodconverter/issues/450)
- LocalOfficeManagerPoolEntry leak [#449](https://github.com/jodconverter/jodconverter/issues/449)
- com.sun.star.lang.DisposedException causes LocalOfficeManagerPoolEntry object unavailable.
    [#443](https://github.com/jodconverter/jodconverter/issues/443)
- java_remote_bridge is disposed (Docker, Ubuntu 24, Spring Boot)
    [#427](https://github.com/jodconverter/jodconverter/issues/427)
- Numeric properties of a custom `document-formats.json` were read as doubles and ignored by LibreOffice
    [#481](https://github.com/jodconverter/jodconverter/pull/481)
- Remote conversions sent the load properties of the target format instead of the source format
    [#480](https://github.com/jodconverter/jodconverter/pull/480)
- The command line tool did not load its logging configuration on Linux and macOS
    [#490](https://github.com/jodconverter/jodconverter/pull/490)

### **Documentation**

- Zombie processes accumulation after LibreOffice restart upon reaching maxTasksPerProcess
    [#439](https://github.com/jodconverter/jodconverter/issues/439)
- gpg, gpgconf, gpgsm zombie processes after each conversion
    [#412](https://github.com/jodconverter/jodconverter/issues/412)
- When converting Word to PDF, some documents end up with more pages or an inconsistent format
    [#445](https://github.com/jodconverter/jodconverter/issues/445)
- ubuntu:24.04 on LibreOffice in docker framework/source/loadenv/loadenv.cxx
    [#442](https://github.com/jodconverter/jodconverter/issues/442)
- Manual restart of processes [#441](https://github.com/jodconverter/jodconverter/issues/441)
- How to skip hidden sheets when converting Excel to PDF?
    [#436](https://github.com/jodconverter/jodconverter/issues/436)
- The documentation moved to [Zensical](https://zensical.org) and gained pages on the containers, the security, the
    performance, the PDF options, the page images and the known LibreOffice issues.

### **Breaking changes**

The [migration guide](../migration-guides/migration-guide-5.0.0.md) has the details and the replacements. In short:

- Java 17, Spring Boot 4 and SLF4J 2 are required.
- `jodconverter-spring` and its `JodConverterBean` are removed; a Spring Framework application declares the manager and
    the converter as beans.
- `ExternalOfficeManager` is renamed `AttachedOfficeManager` and `TableOfContentUpdaterFilter` is renamed
    `DocumentIndexesUpdaterFilter`; the old names remain as deprecated aliases, like the `jodconverter.external.*`
    properties of the starter.
- The `ProcessManager` contract works with `ProcessHandle`; `MacProcessManager` and `FreeBSDProcessManager` are gone.
- `ProcessQuery` and `RequestConfig` are records; the builder setters take primitives; `DocumentFormat` is always
    immutable; `OfficeManager` makes temporary files; the classes of the previous pool are removed.
- The command line tool replaces `--application-context` with `--config`, a JSON or YAML file.
- The remote module no longer depends on Apache HttpClient; the library modules no longer import the Spring Boot BOM.
- The office processes write their temporary files in their instance profile directory.

### **Merged pull requests**

Build, tooling and dependencies:

- Add contributing guide, pull request and issue templates, Dependabot and security policy
    [#457](https://github.com/jodconverter/jodconverter/pull/457)
- Set Java 17 as the minimum version for JODConverter 5.0 [#458](https://github.com/jodconverter/jodconverter/pull/458)
- Update checker-qual, commons-cli, commons-io, the LibreOffice jars and the Foojay resolver
    [#459](https://github.com/jodconverter/jodconverter/pull/459)
- Bump actions/checkout, actions/setup-python and actions/cache
    [#461](https://github.com/jodconverter/jodconverter/pull/461), [#462](https://github.com/jodconverter/jodconverter/pull/462),
    [#463](https://github.com/jodconverter/jodconverter/pull/463)
- Upgrade Gradle to 9.8.0 and replace the Nebula integtest plugin
    [#465](https://github.com/jodconverter/jodconverter/pull/465)
- Bump the Maven publish plugin to 0.37.0 [#466](https://github.com/jodconverter/jodconverter/pull/466)
- Use the Spring Boot BOM in the Spring Boot starter only [#467](https://github.com/jodconverter/jodconverter/pull/467)
- Upgrade to Spring Boot 3.5 and Spring Framework 6.2 [#468](https://github.com/jodconverter/jodconverter/pull/468)
- Upgrade PMD to 7.28.0 and migrate the ruleset [#469](https://github.com/jodconverter/jodconverter/pull/469)
- Update CONTRIBUTING for the current CI and the branch rules
    [#471](https://github.com/jodconverter/jodconverter/pull/471)
- Use the Java 17 language features and APIs [#473](https://github.com/jodconverter/jodconverter/pull/473)
- Migrate the documentation to Zensical [#474](https://github.com/jodconverter/jodconverter/pull/474) and publish it
    from develop [#475](https://github.com/jodconverter/jodconverter/pull/475)
- Install LibreOffice through Chocolatey and Homebrew again in CI
    [#489](https://github.com/jodconverter/jodconverter/pull/489)
- Replace log4j 1 with Log4j 2 in the CLI and the tests [#490](https://github.com/jodconverter/jodconverter/pull/490)
- Upgrade to SLF4J 2 [#491](https://github.com/jodconverter/jodconverter/pull/491)
- Use var for local variables, clean up unused code and format the documentation
    [#498](https://github.com/jodconverter/jodconverter/pull/498)
- Add the release tooling: setVersion task, release workflow on the version tag, generated release notes
    [#525](https://github.com/jodconverter/jodconverter/pull/525)
- Build the starter against Spring Boot 4.1, and upgrade the libraries to their latest versions on Java 17
    [#528](https://github.com/jodconverter/jodconverter/pull/528)
- Send the coverage to Coveralls [#529](https://github.com/jodconverter/jodconverter/pull/529),
    [#534](https://github.com/jodconverter/jodconverter/pull/534)
- Replace the stale Junie guidelines with an AGENTS.md for every coding agent
    [#530](https://github.com/jodconverter/jodconverter/pull/530)
- Remove the IDE style files of 2017 and the module READMEs nothing uses
    [#531](https://github.com/jodconverter/jodconverter/pull/531)
- Refresh checkstyle.xml from the Google checks of Checkstyle 12.3.1 and bring the report to zero
    [#532](https://github.com/jodconverter/jodconverter/pull/532), [#533](https://github.com/jodconverter/jodconverter/pull/533)
- Cover the realistic gaps of the coverage report [#535](https://github.com/jodconverter/jodconverter/pull/535)
- Bring the PMD report of the main code down to the design metrics, and the test code to zero
    [#536](https://github.com/jodconverter/jodconverter/pull/536), [#537](https://github.com/jodconverter/jodconverter/pull/537)

Office processes and the pool:

- Cancel timed-out tasks and never lose a pool entry on interrupt
    [#476](https://github.com/jodconverter/jodconverter/pull/476)
- Kill a process that lost its connection instead of waiting the process timeout
    [#477](https://github.com/jodconverter/jodconverter/pull/477)
- Keep the office temporary files in the instance profile directory
    [#478](https://github.com/jodconverter/jodconverter/pull/478)
- Expose the temporary directory of office manager pools [#483](https://github.com/jodconverter/jodconverter/pull/483)
- Fail fast when the office port is used by another program
    [#484](https://github.com/jodconverter/jodconverter/pull/484)
- Add a pool size option that picks free ports for the local office manager
    [#485](https://github.com/jodconverter/jodconverter/pull/485)
- Add an office executable option to start office through a launcher
    [#487](https://github.com/jodconverter/jodconverter/pull/487)
- Add the office worker pool, a dispatcher to replace the office manager pool
    [#497](https://github.com/jodconverter/jodconverter/pull/497), and move the office managers to it
    [#499](https://github.com/jodconverter/jodconverter/pull/499), [#500](https://github.com/jodconverter/jodconverter/pull/500)
- Add the asynchronous API: executeAsync on the conversion job and submit on the office manager
    [#501](https://github.com/jodconverter/jodconverter/pull/501)
- Add a status snapshot to the office worker pool and a health indicator to the Spring Boot starter
    [#502](https://github.com/jodconverter/jodconverter/pull/502)
- Document the office worker pool and add the taskQueueCapacity property
    [#503](https://github.com/jodconverter/jodconverter/pull/503)
- Follow the office processes through their ProcessHandle instead of searching their pid
    [#516](https://github.com/jodconverter/jodconverter/pull/516)
- Wait for the fail-fast workers outside the monitor of the pool, so that a stop aborts a start in progress
    [#519](https://github.com/jodconverter/jodconverter/pull/519)
- Rename ExternalOfficeManager to AttachedOfficeManager, keeping the old name and the jodconverter.external properties
    [#524](https://github.com/jodconverter/jodconverter/pull/524)

Conversions, formats and filters:

- Send the source format's load properties in remote conversions
    [#480](https://github.com/jodconverter/jodconverter/pull/480)
- Keep whole numbers as integers in JSON document format registries
    [#481](https://github.com/jodconverter/jodconverter/pull/481)
- Add the Markdown format to the default registry [#488](https://github.com/jodconverter/jodconverter/pull/488)
- Add typed PDF options for conversions to PDF [#493](https://github.com/jodconverter/jodconverter/pull/493), to the
    command line tool [#494](https://github.com/jodconverter/jodconverter/pull/494), as defaults of the converters and
    of the Spring Boot starter [#495](https://github.com/jodconverter/jodconverter/pull/495), parsed by name in the
    core module [#518](https://github.com/jodconverter/jodconverter/pull/518)
- Keep the load and store properties whose value contains an equal sign
    [#496](https://github.com/jodconverter/jodconverter/pull/496)
- Send the remote conversions with the HTTP client of the JDK, built once per worker with its SSL material
    [#517](https://github.com/jodconverter/jodconverter/pull/517)
- Rename TableOfContentUpdaterFilter to DocumentIndexesUpdaterFilter, updating the indexes in two passes
    [#522](https://github.com/jodconverter/jodconverter/pull/522)
- Assert that the archive PDF preset embeds every font, which PDF/A requires
    [#523](https://github.com/jodconverter/jodconverter/pull/523)
- Export each slide or draw page of a presentation or a drawing as its own image, with LocalConverter.exportPages
    [#526](https://github.com/jodconverter/jodconverter/pull/526)
- Merge text documents with LocalConverter.merge, each one starting on a new page
    [#527](https://github.com/jodconverter/jodconverter/pull/527)

Spring Boot starter and command line tool:

- Stop jodconverter-spring from pulling the log4j binding into users' runtime
    [#479](https://github.com/jodconverter/jodconverter/pull/479)
- Auto-configure the external office manager in the Spring Boot starter
    [#486](https://github.com/jodconverter/jodconverter/pull/486)
- Replace the deprecated commons-cli HelpFormatter and drop SecurityManager from the CLI tests
    [#470](https://github.com/jodconverter/jodconverter/pull/470), and widen the CLI help output to 120 columns
    [#472](https://github.com/jodconverter/jodconverter/pull/472)
- Remove the jodconverter-spring module [#507](https://github.com/jodconverter/jodconverter/pull/507)
- Replace the Spring application context of the command line tool with a configuration file
    [#508](https://github.com/jodconverter/jodconverter/pull/508)
- Clean up the Spring Boot starter: shared pool properties, durations, enums, no redundant bean conditions
    [#510](https://github.com/jodconverter/jodconverter/pull/510)
- Bind the jodconverter.remote.ssl properties of the Spring Boot starter directly to SslConfig
    [#513](https://github.com/jodconverter/jodconverter/pull/513)
- Configure the document format registry of the Spring Boot starter once, under jodconverter.document-formats
    [#514](https://github.com/jodconverter/jodconverter/pull/514)
- Turn the properties classes of the Spring Boot starter into records
    [#515](https://github.com/jodconverter/jodconverter/pull/515)

API cleanup and documentation:

- Document containers, security and known LibreOffice issues
    [#482](https://github.com/jodconverter/jodconverter/pull/482)
- Add the 5.0 migration guide [#492](https://github.com/jodconverter/jodconverter/pull/492), and keep it right
    [#511](https://github.com/jodconverter/jodconverter/pull/511), [#512](https://github.com/jodconverter/jodconverter/pull/512)
- Remove the deprecated createInstanceMSF and createInstanceMCF methods of Lo
    [#505](https://github.com/jodconverter/jodconverter/pull/505)
- Fix the bugs found by the 5.0 code review [#506](https://github.com/jodconverter/jodconverter/pull/506)
- Clean up the core API: office managers make temporary files, immutable document formats, primitive builder setters
    [#509](https://github.com/jodconverter/jodconverter/pull/509)
- Sweep the minor cleanups of the 5.0 plan: unused helpers, stale comments, internal classes, versioned office homes
    [#520](https://github.com/jodconverter/jodconverter/pull/520)
- Update Gradle scripts: fix Javadoc task property and use HTTPS for license URLs
    [#521](https://github.com/jodconverter/jodconverter/pull/521)
