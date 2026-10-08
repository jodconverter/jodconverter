# Changelog

## [v5.0.1](https://github.com/jodconverter/jodconverter/tree/v5.0.1) (2026-10-08)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v5.0.0...v5.0.1)

**Fixed bugs:**

- The filters of a converter were only applied at its first export of page images
- A complete conversion failed when the office process was lost while closing the document (LibreOffice 25.8.3 to 26.2 on Windows, tdf#172335)

**Merged pull requests:**

- Try the process listing again when it fails, and tell a failed listing from an empty one [#546](https://github.com/jodconverter/jodconverter/pull/546)
- Fix the 4.9.0 of the remote module snippet and make setVersion correct every coordinate, whatever version it shows [#544](https://github.com/jodconverter/jodconverter/pull/544)

## [v5.0.0](https://github.com/jodconverter/jodconverter/tree/v5.0.0) (2026-10-08)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.11...v5.0.0)

JODConverter 5.0 moves to Java 17, Spring Boot 4 and SLF4J 2, rewrites the pool of office processes, and adds typed PDF options, asynchronous conversions, a pool size, document merging and the export of slides to images. See the [release notes](https://jodconverter.github.io/jodconverter/latest/release-notes/release-notes-5.0.0/) and the [migration guide](https://jodconverter.github.io/jodconverter/latest/migration-guides/migration-guide-5.0.0/).

**Merged pull requests:**

- Skip the integration tests in the release workflow, which has no office to run them [#540](https://github.com/jodconverter/jodconverter/pull/540)
- Let the release workflow run by hand as a dry run of the signing and the Maven Central credentials [#539](https://github.com/jodconverter/jodconverter/pull/539)
- Add the 5.0.0 release notes page, following develop until the release [#538](https://github.com/jodconverter/jodconverter/pull/538)
- Bring the PMD report of the test code to zero: explicit imports, package-private test doubles, and the rules that do not fit tests [#537](https://github.com/jodconverter/jodconverter/pull/537)
- Bring the PMD report of the main code down to the design metrics: settle the rules the code breaks on purpose, then fix the rest [#536](https://github.com/jodconverter/jodconverter/pull/536)
- Cover the realistic gaps of the coverage report: the PDF properties of the starter, the SSL contexts, the page images task, the command line [#535](https://github.com/jodconverter/jodconverter/pull/535)
- Send the coverage to Coveralls with repository-relative source paths, through the coveralls-jacoco plugin [#534](https://github.com/jodconverter/jodconverter/pull/534)
- Bring the Checkstyle report to zero: explicit imports, Javadoc periods, switch defaults, and acronyms kept upper case [#533](https://github.com/jodconverter/jodconverter/pull/533)
- Refresh checkstyle.xml from the Google checks of Checkstyle 12.3.1, and document the protected members it flags [#532](https://github.com/jodconverter/jodconverter/pull/532)
- Remove the IDE style files of 2017 and the module READMEs nothing uses, and point Codacy at build-logic [#531](https://github.com/jodconverter/jodconverter/pull/531)
- Replace the stale Junie guidelines with an AGENTS.md for every coding agent [#530](https://github.com/jodconverter/jodconverter/pull/530)
- Send the coverage to Coveralls from the Linux job of the build workflow [#529](https://github.com/jodconverter/jodconverter/pull/529)
- Build the starter against Spring Boot 4.1, and upgrade the libraries to their latest versions on Java 17 [#528](https://github.com/jodconverter/jodconverter/pull/528)
- Merge text documents with LocalConverter.merge, each one starting on a new page [#527](https://github.com/jodconverter/jodconverter/pull/527)
- Export each slide or draw page of a presentation or a drawing as its own image, with LocalConverter.exportPages [#526](https://github.com/jodconverter/jodconverter/pull/526)
- Add the release tooling: setVersion task, release workflow on the version tag, generated release notes, automatic Maven Central release, release guide [#525](https://github.com/jodconverter/jodconverter/pull/525)
- Rename ExternalOfficeManager to AttachedOfficeManager, keeping the old name and the jodconverter.external properties deprecated [#524](https://github.com/jodconverter/jodconverter/pull/524)
- Assert that the archive PDF preset embeds every font, which PDF/A requires [#523](https://github.com/jodconverter/jodconverter/pull/523)
- Rename TableOfContentUpdaterFilter to DocumentIndexesUpdaterFilter, updating the indexes in two passes after a refresh [#522](https://github.com/jodconverter/jodconverter/pull/522)
- Update Gradle scripts: fix Javadoc task property and use HTTPS for license URLs [#521](https://github.com/jodconverter/jodconverter/pull/521)
- Sweep the minor cleanups of the 5.0 plan: unused helpers, stale comments, internal classes, versioned office homes [#520](https://github.com/jodconverter/jodconverter/pull/520)
- Wait for the fail-fast workers outside the monitor of the pool, so that a stop aborts a start in progress [#519](https://github.com/jodconverter/jodconverter/pull/519)
- Parse the PDF options by name in the core module, shared by the command line tool and the Spring Boot starter [#518](https://github.com/jodconverter/jodconverter/pull/518)
- Send the remote conversions with the HTTP client of the JDK, built once per worker with its SSL material [#517](https://github.com/jodconverter/jodconverter/pull/517)
- Follow the office processes through their ProcessHandle instead of searching their pid [#516](https://github.com/jodconverter/jodconverter/pull/516)
- Turn the properties classes of the Spring Boot starter into records [#515](https://github.com/jodconverter/jodconverter/pull/515)
- Configure the document format registry of the Spring Boot starter once, under jodconverter.document-formats [#514](https://github.com/jodconverter/jodconverter/pull/514)
- Bind the jodconverter.remote.ssl properties of the Spring Boot starter directly to SslConfig [#513](https://github.com/jodconverter/jodconverter/pull/513)
- Fix the documentation snippets that call APIs removed in 5.0 [#512](https://github.com/jodconverter/jodconverter/pull/512)
- Stop describing the 5.0 pool getters as removed in the migration guide [#511](https://github.com/jodconverter/jodconverter/pull/511)
- Clean up the Spring Boot starter: shared pool properties, durations, enums, no redundant bean conditions [#510](https://github.com/jodconverter/jodconverter/pull/510)
- Clean up the core API: office managers make temporary files, immutable document formats, primitive builder setters, fewer utilities [#509](https://github.com/jodconverter/jodconverter/pull/509)
- Replace the Spring application context of the command line tool with a configuration file [#508](https://github.com/jodconverter/jodconverter/pull/508)
- Remove the jodconverter-spring module [#507](https://github.com/jodconverter/jodconverter/pull/507)
- Fix the bugs found by the 5.0 code review [#506](https://github.com/jodconverter/jodconverter/pull/506)
- Remove the deprecated createInstanceMSF and createInstanceMCF methods of Lo [#505](https://github.com/jodconverter/jodconverter/pull/505)
- Document the office worker pool and add the taskQueueCapacity property to the Spring Boot starter and the Spring bean [#503](https://github.com/jodconverter/jodconverter/pull/503)
- Add a status snapshot to the office worker pool and a health indicator to the Spring Boot starter [#502](https://github.com/jodconverter/jodconverter/pull/502)
- Add the asynchronous API: executeAsync on the conversion job and submit on the office manager [#501](https://github.com/jodconverter/jodconverter/pull/501)
- Move the external and remote office managers to the office worker pool and remove the old pool [#500](https://github.com/jodconverter/jodconverter/pull/500)
- Move LocalOfficeManager to the office worker pool [#499](https://github.com/jodconverter/jodconverter/pull/499)
- Use var for local variables, clean up unused code and format the documentation [#498](https://github.com/jodconverter/jodconverter/pull/498)
- Add the office worker pool, a dispatcher to replace the office manager pool [#497](https://github.com/jodconverter/jodconverter/pull/497)
- Keep the load and store properties whose value contains an equal sign [#496](https://github.com/jodconverter/jodconverter/pull/496)
- Add default PDF options to the converters and to the Spring Boot starter [#495](https://github.com/jodconverter/jodconverter/pull/495)
- Add the PDF options to the command line tool [#494](https://github.com/jodconverter/jodconverter/pull/494)
- Add typed PDF options for conversions to PDF [#493](https://github.com/jodconverter/jodconverter/pull/493)
- Add the 5.0 migration guide [#492](https://github.com/jodconverter/jodconverter/pull/492)
- Upgrade to SLF4J 2 [#491](https://github.com/jodconverter/jodconverter/pull/491)
- Replace log4j 1 with Log4j 2 in the CLI and the tests [#490](https://github.com/jodconverter/jodconverter/pull/490)
- Install LibreOffice through Chocolatey and Homebrew again in CI [#489](https://github.com/jodconverter/jodconverter/pull/489)
- Add the Markdown format to the default registry [#488](https://github.com/jodconverter/jodconverter/pull/488)
- Add an office executable option to start office through a launcher [#487](https://github.com/jodconverter/jodconverter/pull/487)
- Auto-configure the external office manager in the Spring Boot starter [#486](https://github.com/jodconverter/jodconverter/pull/486)
- Add a pool size option that picks free ports for the local office manager [#485](https://github.com/jodconverter/jodconverter/pull/485)
- Fail fast when the office port is used by another program [#484](https://github.com/jodconverter/jodconverter/pull/484)
- Expose the temporary directory of office manager pools [#483](https://github.com/jodconverter/jodconverter/pull/483)
- Document containers, security and known LibreOffice issues [#482](https://github.com/jodconverter/jodconverter/pull/482)
- Keep whole numbers as integers in JSON document format registries [#481](https://github.com/jodconverter/jodconverter/pull/481)
- Send the source format's load properties in remote conversions [#480](https://github.com/jodconverter/jodconverter/pull/480)
- Stop jodconverter-spring from pulling the log4j binding into users' runtime [#479](https://github.com/jodconverter/jodconverter/pull/479)
- Keep the office temporary files in the instance profile directory [#478](https://github.com/jodconverter/jodconverter/pull/478)
- Kill a process that lost its connection instead of waiting the process timeout [#477](https://github.com/jodconverter/jodconverter/pull/477)
- Cancel timed-out tasks and never lose a pool entry on interrupt [#476](https://github.com/jodconverter/jodconverter/pull/476)
- Publish the documentation from develop [#475](https://github.com/jodconverter/jodconverter/pull/475)
- Migrate the documentation to Zensical [#474](https://github.com/jodconverter/jodconverter/pull/474)
- Use the Java 17 language features and APIs [#473](https://github.com/jodconverter/jodconverter/pull/473)
- Widen the CLI help output to 120 columns [#472](https://github.com/jodconverter/jodconverter/pull/472)
- Update CONTRIBUTING for the current CI and the branch rules [#471](https://github.com/jodconverter/jodconverter/pull/471)
- Replace the deprecated commons-cli HelpFormatter and drop SecurityManager from the CLI tests [#470](https://github.com/jodconverter/jodconverter/pull/470)
- Upgrade PMD to 7.28.0 and migrate the ruleset [#469](https://github.com/jodconverter/jodconverter/pull/469)
- Upgrade to Spring Boot 3.5 and Spring Framework 6.2 [#468](https://github.com/jodconverter/jodconverter/pull/468)
- Use the Spring Boot BOM in the Spring Boot starter only [#467](https://github.com/jodconverter/jodconverter/pull/467)
- Bump com.vanniktech.maven.publish:com.vanniktech.maven.publish.gradle.plugin from 0.34.0 to 0.37.0 in the gradle-minor-and-patch group across 1 directory [#466](https://github.com/jodconverter/jodconverter/pull/466)
- Upgrade Gradle to 9.8.0 and replace the Nebula integtest plugin [#465](https://github.com/jodconverter/jodconverter/pull/465)
- Bump actions/cache from 4 to 6 [#463](https://github.com/jodconverter/jodconverter/pull/463)
- Bump actions/setup-python from 5 to 7 [#462](https://github.com/jodconverter/jodconverter/pull/462)
- Bump actions/checkout from 4 to 7 [#461](https://github.com/jodconverter/jodconverter/pull/461)
- Update checker-qual, commons-cli, commons-io, the LibreOffice jars and the Foojay resolver [#459](https://github.com/jodconverter/jodconverter/pull/459)
- Set Java 17 as the minimum version for JODConverter 5.0 [#458](https://github.com/jodconverter/jodconverter/pull/458)
- Add contributing guide, pull request and issue templates, Dependabot and security policy [#457](https://github.com/jodconverter/jodconverter/pull/457)

## [v4.4.11](https://github.com/jodconverter/jodconverter/tree/v4.4.11) (2025-08-21)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.10...v4.4.11)

**Implemented enhancements:**

- Add support for named pipe in spring boot [\#431](https://github.com/jodconverter/jodconverter/issues/431)
- `officeManager.isRunning\(\)` is meaningless with asynchronous process management [\#428](https://github.com/jodconverter/jodconverter/issues/428)

## [v4.4.10](https://github.com/jodconverter/jodconverter/tree/v4.4.10) (2025-07-20)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.9...v4.4.10)

**Closed issues:**

- When I convert docx to pdf, The Pdf file not contains SONG\_GB2312, only simsun [\#417](https://github.com/jodconverter/jodconverter/issues/417)
- "API misuse: modification of a menu's items on a non-main thread when the menu is part of the main menu." on MacOS [\#403](https://github.com/jodconverter/jodconverter/issues/403)

## [v4.4.9](https://github.com/jodconverter/jodconverter/tree/v4.4.9) (2025-05-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.8...v4.4.9)

**Implemented enhancements:**

- Remove the disableOpengl option. [\#426](https://github.com/jodconverter/jodconverter/issues/426)
- WindowsProcessManager\#isUsable triggers antivirus [\#388](https://github.com/jodconverter/jodconverter/issues/388)

**Fixed bugs:**

- Libre Office disconnects when converting a password protected ODT file to PDF [\#423](https://github.com/jodconverter/jodconverter/issues/423)
- On more recent Java version like Java 17, JODconverter has runtime exception Unable to create instance of class org.jodconverter.core.document.DocumentForma [\#408](https://github.com/jodconverter/jodconverter/issues/408)
- When start app using jodconverter-spring-boot-starter, soffice.bin is considered a zombie process [\#392](https://github.com/jodconverter/jodconverter/issues/392)
- The program running on Docker keeps generating zombie processes. [\#367](https://github.com/jodconverter/jodconverter/issues/367)

**Closed issues:**

- remaining soffice process blocking [\#398](https://github.com/jodconverter/jodconverter/issues/398)
- jodconverter-local 4.4.3 or later causes the service to fail to end. [\#375](https://github.com/jodconverter/jodconverter/issues/375)
- 4.4.2word导出pdf，偶尔出现org.jodconverter.core.office.OfficeException: Task did not complete: LocalConversionTask{source=SourceDocumentSpecsFromInputStream{file=null, for mat=null}, loadProperties=null, target=TargetDocumentSpecsFromOutputStream{file=null, format=pdf}, storeProperties=null} [\#336](https://github.com/jodconverter/jodconverter/issues/336)

## [v4.4.8](https://github.com/jodconverter/jodconverter/tree/v4.4.8) (2024-09-01)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.7...v4.4.8)

**Implemented enhancements:**

- Any particular reason we do not support xlsm in DefaultDocumentFormatRegistry [\#391](https://github.com/jodconverter/jodconverter/issues/391)

**Fixed bugs:**

- No qualifying bean of type 'org.jodconverter.core.DocumentConverter' available [\#390](https://github.com/jodconverter/jodconverter/issues/390)
- Scanner bug [\#383](https://github.com/jodconverter/jodconverter/issues/383)

**Closed issues:**

- Incompatible with LO 24.x \(probably?\) [\#386](https://github.com/jodconverter/jodconverter/issues/386)
- Task keeps hanging when using Remote JodConverter [\#384](https://github.com/jodconverter/jodconverter/issues/384)
- Please remove unnecessary `@ConfigurationPropertiesScan` on `JodConverterLocalProperties` and `JodConverterRemoteProperties` [\#377](https://github.com/jodconverter/jodconverter/issues/377)
- Information about supported properties and their meaning [\#372](https://github.com/jodconverter/jodconverter/issues/372)
- The Word document with more than 12 pages will automatically cancel the task. [\#364](https://github.com/jodconverter/jodconverter/issues/364)
- pptx file conversion of PDF failed [\#359](https://github.com/jodconverter/jodconverter/issues/359)

**Merged pull requests:**

- Remove unnecessary ConfigurationPropertiesScan annotation [\#378](https://github.com/jodconverter/jodconverter/pull/378) ([bianjp](https://github.com/bianjp))

## [v4.4.7](https://github.com/jodconverter/jodconverter/tree/v4.4.7) (2023-12-13)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.6...v4.4.7)

**Fixed bugs:**

- Using SpringBoot autoconfiguration with a remote setup fails with ClassNotFoundException [\#331](https://github.com/jodconverter/jodconverter/issues/331)

**Closed issues:**

- there are a blank column in my pdf [\#354](https://github.com/jodconverter/jodconverter/issues/354)
- Error: This office manager is not running. [\#353](https://github.com/jodconverter/jodconverter/issues/353)
- Cannot find a valid unoloader.jar path. [\#348](https://github.com/jodconverter/jodconverter/issues/348)
- An error is reported after the office manager Temporary folder is deleted [\#346](https://github.com/jodconverter/jodconverter/issues/346)
- Getting error: Unsupported URL  "type detection failed" [\#337](https://github.com/jodconverter/jodconverter/issues/337)
- Could not store document: tempfile\_1.pdf [\#329](https://github.com/jodconverter/jodconverter/issues/329)
- Failed to load class "org.slf4j.impl.StaticLoggerBinder". [\#328](https://github.com/jodconverter/jodconverter/issues/328)
- 启动加载异常 bean of type 'org.jodconverter.core.DocumentConverter' that could not be found. [\#326](https://github.com/jodconverter/jodconverter/issues/326)
- Images are not shown correctly [\#311](https://github.com/jodconverter/jodconverter/issues/311)
- Blank first page when converting to any format [\#252](https://github.com/jodconverter/jodconverter/issues/252)

**Merged pull requests:**

- add support for websocket urps available \>= LibreOffice 24.2 [\#355](https://github.com/jodconverter/jodconverter/pull/355) ([caolanm](https://github.com/caolanm))
- Add support for additional HTML extension alias [\#338](https://github.com/jodconverter/jodconverter/pull/338) ([LiamMacP](https://github.com/LiamMacP))

## [v4.4.6](https://github.com/jodconverter/jodconverter/tree/v4.4.6) (2023-01-27)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.5...v4.4.6)

**Implemented enhancements:**

- Add publish mode to presentation-\>html conversion in document-formats.json [\#323](https://github.com/jodconverter/jodconverter/issues/323)
- Jodconverter not working with spring-boot 3 [\#320](https://github.com/jodconverter/jodconverter/issues/320)

**Closed issues:**

- Why does the process restart after "maximum tasks"? [\#321](https://github.com/jodconverter/jodconverter/issues/321)

**Merged pull requests:**

- Spring boot 3.0 compatibility fixes \#320 [\#322](https://github.com/jodconverter/jodconverter/pull/322) ([EugenMayer](https://github.com/EugenMayer))

## [v4.4.5](https://github.com/jodconverter/jodconverter/tree/v4.4.5) (2022-12-21)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.4...v4.4.5)

**Implemented enhancements:**

- ExternalOfficeManager does not work [\#278](https://github.com/jodconverter/jodconverter/issues/278)
- The address www.jodconverter.org redirects to malware [\#266](https://github.com/jodconverter/jodconverter/issues/266)

**Fixed bugs:**

- java.lang.NullPointerException: byExtension\(extension\) must not be null [\#319](https://github.com/jodconverter/jodconverter/issues/319)
-  Failed to start bean 'documentationPluginsBootstrapper' when starting rest version [\#315](https://github.com/jodconverter/jodconverter/issues/315)
- IndexOutOfBoundsException: Index: 1, Size: 1  when Run multiple tasks in concurrent. [\#310](https://github.com/jodconverter/jodconverter/issues/310)
- Got error when using org.jodconverter:jodconverter-local-lo [\#309](https://github.com/jodconverter/jodconverter/issues/309)

**Closed issues:**

- With 4.4.3+ i get an exception missing classes for the DefaultDocumentFormatRegistry [\#317](https://github.com/jodconverter/jodconverter/issues/317)
- how can I use jodconverter-remote connect openOffice? old version 2.2.1 can do it  \#260 [\#312](https://github.com/jodconverter/jodconverter/issues/312)

**Merged pull requests:**

- Migrate to swagger v3 / openapi - fixes \#317 [\#318](https://github.com/jodconverter/jodconverter/pull/318) ([EugenMayer](https://github.com/EugenMayer))

## [v4.4.4](https://github.com/jodconverter/jodconverter/tree/v4.4.4) (2022-09-22)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.3...v4.4.4)

## [v4.4.3](https://github.com/jodconverter/jodconverter/tree/v4.4.3) (2022-09-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.2...v4.4.3)

**Implemented enhancements:**

- Provide builds of both OpenOffice and LibreOffice dependencies in the maven center [\#273](https://github.com/jodconverter/jodconverter/issues/273)
- support keepAliveOnShutdown through CLI [\#269](https://github.com/jodconverter/jodconverter/issues/269)
- Issues converting potx and xltx [\#259](https://github.com/jodconverter/jodconverter/issues/259)
- HTML conversion: image URL encoding [\#125](https://github.com/jodconverter/jodconverter/issues/125)

**Fixed bugs:**

- gradle build faild with libreoffice 7.1.5 [\#271](https://github.com/jodconverter/jodconverter/issues/271)

**Closed issues:**

- jodconverter not able to open Libre office connection in Linux environment \( org.artofsolving.jodconverter.office.OfficeException: could not establish connection\)  [\#308](https://github.com/jodconverter/jodconverter/issues/308)
- Task :distZip FAILED [\#303](https://github.com/jodconverter/jodconverter/issues/303)
- Conversion from \(X\)HTML to ODT does not produce 'real' ODT documents, but HTML documents that don't behave like 'regular' ODT documents in LibreOffice [\#297](https://github.com/jodconverter/jodconverter/issues/297)
- Spring configuration metadata json not generated [\#295](https://github.com/jodconverter/jodconverter/issues/295)
- linux+docker+libreoffice，spring-boot + jodconverter，Could not start the office process. [\#292](https://github.com/jodconverter/jodconverter/issues/292)
- 转pdf异常，Caused by: com.sun.star.io.IOException: SfxBaseModel::impl\_store \<file:///D:/officedest/home/data/localpath/servicecenter/20210817/20210817094906679504091671043/Unit%201%20B%20Read%20and%20write基于深度学习的教学课件.pdf\> failed: 0x11b\(Error Area:Io Class:Abort Code:27\) [\#287](https://github.com/jodconverter/jodconverter/issues/287)
- "Unspecified Application Error" occurs from specific PPTX document [\#272](https://github.com/jodconverter/jodconverter/issues/272)
- org.jodconverter.office.OfficeException: Office process died with exit code 333 [\#268](https://github.com/jodconverter/jodconverter/issues/268)
- No response for pdf converter when more requests [\#267](https://github.com/jodconverter/jodconverter/issues/267)
- Blank PDF when trying to convert any document type to PDF on mac OS [\#265](https://github.com/jodconverter/jodconverter/issues/265)
- how can I use jodconverter-remote connect openOffice? old version 2.2.1 can do it [\#260](https://github.com/jodconverter/jodconverter/issues/260)
- LibreOffice Portable  [\#254](https://github.com/jodconverter/jodconverter/issues/254)
- conversion stalling on stopQuietly [\#247](https://github.com/jodconverter/jodconverter/issues/247)
- org.jodconverter.core.office.OfficeException: Could not store document:  errorCode: 2074 [\#239](https://github.com/jodconverter/jodconverter/issues/239)
- Error while converting document from ODS to PDF Format  [\#236](https://github.com/jodconverter/jodconverter/issues/236)
- Question of convert pdf limitation [\#234](https://github.com/jodconverter/jodconverter/issues/234)
- Specific Exception for Password Protected files [\#233](https://github.com/jodconverter/jodconverter/issues/233)
- LibreOffice conversion on .odt file to pdf is timing out on Ubuntu and succeeding on MacOS [\#231](https://github.com/jodconverter/jodconverter/issues/231)
- How to change font when converting docx to pdf [\#222](https://github.com/jodconverter/jodconverter/issues/222)
- JodConverter Not able to start LibreOffice process on AmazonCorretto  [\#206](https://github.com/jodconverter/jodconverter/issues/206)
- Cannot export HTML to ODT [\#173](https://github.com/jodconverter/jodconverter/issues/173)
- Issue while converting \" from word to pdf on weblogic server. [\#167](https://github.com/jodconverter/jodconverter/issues/167)

**Merged pull requests:**

- Update to spring boot 2.7.3 to fix CVEs [\#307](https://github.com/jodconverter/jodconverter/pull/307) ([EugenMayer](https://github.com/EugenMayer))
- Build spring-boot configuration metadata into jar \#295 [\#296](https://github.com/jodconverter/jodconverter/pull/296) ([shysteph](https://github.com/shysteph))
- add format definition for PowerPoint XML templates \(\#259\) [\#270](https://github.com/jodconverter/jodconverter/pull/270) ([stellingsimon](https://github.com/stellingsimon))
- ✨new document format xltx [\#257](https://github.com/jodconverter/jodconverter/pull/257) ([jgoldhammer](https://github.com/jgoldhammer))

## [v4.4.2](https://github.com/jodconverter/jodconverter/tree/v4.4.2) (2021-02-10)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.1...v4.4.2)

**Fixed bugs:**

- DocumentFormat.input family should be optional. [\#249](https://github.com/jodconverter/jodconverter/issues/249)
- regression: document-formats with singular extension field are not supported anymore [\#248](https://github.com/jodconverter/jodconverter/issues/248)

## [v4.4.1](https://github.com/jodconverter/jodconverter/tree/v4.4.1) (2021-02-10)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.0...v4.4.1)

**Fixed bugs:**

- LocalOfficeManager\#afterStartProcessDelay is not validated properly. [\#246](https://github.com/jodconverter/jodconverter/issues/246)

**Closed issues:**

- 2 concurrent calls to  LocalConvertor-\>convert\(\)  return the same PDF file. [\#243](https://github.com/jodconverter/jodconverter/issues/243)

**Merged pull requests:**

- bugfix filename contains CJK characters cause error, change to UTF-8 encoding [\#245](https://github.com/jodconverter/jodconverter/pull/245) ([chunlinyao](https://github.com/chunlinyao))

## [v4.4.0](https://github.com/jodconverter/jodconverter/tree/v4.4.0) (2021-01-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.3.0...v4.4.0)

**Implemented enhancements:**

- Add the ability to wait after an attempt to start an office process before trying to connect. [\#244](https://github.com/jodconverter/jodconverter/issues/244)
- Issues converting dotx [\#213](https://github.com/jodconverter/jodconverter/issues/213)
- Attaching OfficeManager to already running Process [\#203](https://github.com/jodconverter/jodconverter/issues/203)
- JoDConverterBean: How to add filter [\#201](https://github.com/jodconverter/jodconverter/issues/201)
- Make office process management asynchronous \(start, restart, etc\). [\#200](https://github.com/jodconverter/jodconverter/issues/200)
- Remove unnecessary dependencies. [\#198](https://github.com/jodconverter/jodconverter/issues/198)
- Do conversions on remote host with LibreOffice directly \(not with LibreOffice online, not through spring boot\) [\#195](https://github.com/jodconverter/jodconverter/issues/195)
- Use ExternalOfficeManager with a pool of processes [\#191](https://github.com/jodconverter/jodconverter/issues/191)
- Allow process restart to be asynchronous [\#171](https://github.com/jodconverter/jodconverter/issues/171)
- Patching library to be able reuse already running libreoffice instances \>\> killExistingProcess\(false\) [\#72](https://github.com/jodconverter/jodconverter/issues/72)

**Fixed bugs:**

- ExternalOfficeManager :: makeTempDir not called when connectOnStart = false [\#211](https://github.com/jodconverter/jodconverter/issues/211)

**Closed issues:**

- ExternalOfficeManager always connects sockets to 127.0.0.1 [\#241](https://github.com/jodconverter/jodconverter/issues/241)
- DocUpdateMode not working? [\#227](https://github.com/jodconverter/jodconverter/issues/227)
- Depending on the operating system, /tmp is getting regularly cleaned [\#220](https://github.com/jodconverter/jodconverter/issues/220)
- Temporary file name added in CSV -\> PDF conversion [\#219](https://github.com/jodconverter/jodconverter/issues/219)
- wiki page for LibreOffice Online example code shoule be RemoteOfficeManager [\#216](https://github.com/jodconverter/jodconverter/issues/216)
- Wiki page for LibreOffice Online still references "jodconverter-online" [\#214](https://github.com/jodconverter/jodconverter/issues/214)
- Jodconverter randomly fails. [\#204](https://github.com/jodconverter/jodconverter/issues/204)
- I have an issue when I try to convert MS 97-2003 .DOC file to PDF [\#202](https://github.com/jodconverter/jodconverter/issues/202)
- Jod-Converter Reached limit Tasks and Restart  [\#196](https://github.com/jodconverter/jodconverter/issues/196)

**Merged pull requests:**

- Make 127.0.0.1 in socket connection configurable [\#242](https://github.com/jodconverter/jodconverter/pull/242) ([nikowitt](https://github.com/nikowitt))
- bugfix ps args truncated at 125 chars [\#238](https://github.com/jodconverter/jodconverter/pull/238) ([chunlinyao](https://github.com/chunlinyao))

## [v4.3.0](https://github.com/jodconverter/jodconverter/tree/v4.3.0) (2020-03-05)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.4...v4.3.0)

**Implemented enhancements:**

- Rename jodconverter-online module to jodconverter-remote [\#190](https://github.com/jodconverter/jodconverter/issues/190)
- Issues in java11 project [\#178](https://github.com/jodconverter/jodconverter/issues/178)

**Fixed bugs:**

- regression: Upgrade to jodconverter-local 4.2.3 imply to also add a dependency to jodconverter-core [\#183](https://github.com/jodconverter/jodconverter/issues/183)
- Unthrown MalformedInputException when looking for soffice PID [\#180](https://github.com/jodconverter/jodconverter/issues/180)

**Closed issues:**

- Looking for more information [\#194](https://github.com/jodconverter/jodconverter/issues/194)
- Pdf without bookmarking [\#185](https://github.com/jodconverter/jodconverter/issues/185)
- SocketException: Connection reset Issue [\#184](https://github.com/jodconverter/jodconverter/issues/184)
- if inputFile has no content, then it throw exception   [\#179](https://github.com/jodconverter/jodconverter/issues/179)

## [v4.2.4](https://github.com/jodconverter/jodconverter/tree/v4.2.4) (2020-01-16)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.3...v4.2.4)

## [v4.2.3](https://github.com/jodconverter/jodconverter/tree/v4.2.3) (2020-01-16)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.2...v4.2.3)

**Implemented enhancements:**

- sample-webapp throws java.lang.IllegalArgumentException [\#165](https://github.com/jodconverter/jodconverter/issues/165)
- Add support for "XHTML" LibreOffice filters [\#134](https://github.com/jodconverter/jodconverter/issues/134)
- Add all supported extensions to PDF conversion support. [\#132](https://github.com/jodconverter/jodconverter/issues/132)
- Java 11 compatibility [\#127](https://github.com/jodconverter/jodconverter/issues/127)
- Add support for "XHTML" LibreOffice filters [\#135](https://github.com/jodconverter/jodconverter/pull/135) ([linux-warrior](https://github.com/linux-warrior))

**Fixed bugs:**

- sample-webapp throws org.apache.commons.io.FileExistsException [\#166](https://github.com/jodconverter/jodconverter/issues/166)
- Errors in tests when building jodconverter 4.2.2 with Java 9+ [\#159](https://github.com/jodconverter/jodconverter/issues/159)
- Could not establish connection [\#148](https://github.com/jodconverter/jodconverter/issues/148)
- LibreOffice path on FreeBSD is not autodetected [\#137](https://github.com/jodconverter/jodconverter/issues/137)
- ExternalOfficeManager creates temporary files in the current directory [\#130](https://github.com/jodconverter/jodconverter/issues/130)
- class ExternalOfficeManager is not Public [\#121](https://github.com/jodconverter/jodconverter/issues/121)
- Build fails with JDK10 on macOS [\#79](https://github.com/jodconverter/jodconverter/issues/79)

**Closed issues:**

- Task did not complete within timeout  [\#177](https://github.com/jodconverter/jodconverter/issues/177)
- TIFF conversion to PDF  [\#162](https://github.com/jodconverter/jodconverter/issues/162)
- It‘s not working on jre7？ [\#156](https://github.com/jodconverter/jodconverter/issues/156)
- There was an Exception after a while, and it persisted [\#154](https://github.com/jodconverter/jodconverter/issues/154)
- Add support for vsd and vsdx to PDF [\#151](https://github.com/jodconverter/jodconverter/issues/151)
- java.lang.VerifyError [\#149](https://github.com/jodconverter/jodconverter/issues/149)
- org.jodconverter.office.OfficeException: Task did not complete within timeout [\#146](https://github.com/jodconverter/jodconverter/issues/146)
- How to change the Paper Format before export PDF? [\#144](https://github.com/jodconverter/jodconverter/issues/144)
- how to set defaultLoadProperties in version 4.2.2？ [\#141](https://github.com/jodconverter/jodconverter/issues/141)
- lost images while converting to pdf [\#138](https://github.com/jodconverter/jodconverter/issues/138)
- Great, finally... your the official successor [\#123](https://github.com/jodconverter/jodconverter/issues/123)
- Improve documentation for LibreOffice Portable Support [\#29](https://github.com/jodconverter/jodconverter/issues/29)

**Merged pull requests:**

- If parent dir is not exist,Program will throw exception while it crea… [\#181](https://github.com/jodconverter/jodconverter/pull/181) ([qiangtoudianyan](https://github.com/qiangtoudianyan))
- fixes \#151 [\#152](https://github.com/jodconverter/jodconverter/pull/152) ([anghelutar](https://github.com/anghelutar))
- Get Java11 compatible \#127 [\#128](https://github.com/jodconverter/jodconverter/pull/128) ([EugenMayer](https://github.com/EugenMayer))

## [v4.2.2](https://github.com/jodconverter/jodconverter/tree/v4.2.2) (2018-11-30)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.1...v4.2.2)

**Implemented enhancements:**

- Create a filter to embed linked images into output document. [\#117](https://github.com/jodconverter/jodconverter/issues/117)
- ExternalOfficeManager can't convert streams [\#116](https://github.com/jodconverter/jodconverter/issues/116)
- Filter chain should be reusable without reset [\#112](https://github.com/jodconverter/jodconverter/issues/112)
-  static JodConverter.convert methods dont work with ExternalOfficeManagerBuilder\(\) [\#111](https://github.com/jodconverter/jodconverter/issues/111)

**Fixed bugs:**

- Fix regression introduced by \#99. Use AOO libraries.  [\#113](https://github.com/jodconverter/jodconverter/issues/113)

## [v4.2.1](https://github.com/jodconverter/jodconverter/tree/v4.2.1) (2018-11-02)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.0...v4.2.1)

**Implemented enhancements:**

- Stop using deprecated command lines options using LibreOffice [\#106](https://github.com/jodconverter/jodconverter/issues/106)
- Redirect office output to jodconverter logs. [\#105](https://github.com/jodconverter/jodconverter/issues/105)
- Add support for auto detecting OpenOffice4 path for linux DEB-based Installation [\#101](https://github.com/jodconverter/jodconverter/issues/101)
- Add property for setting ProcessManager explicitly [\#100](https://github.com/jodconverter/jodconverter/issues/100)
- Use LibreOffice libraries instead of Apache Open-Office ones by default [\#99](https://github.com/jodconverter/jodconverter/issues/99)
- Add a property to trust all certificate in jodconverter-online module [\#98](https://github.com/jodconverter/jodconverter/issues/98)
- Add properties to the spring-boot-starter allowing document formats customization. [\#94](https://github.com/jodconverter/jodconverter/issues/94)
- Add templateProfileDirOrDefault option to the LocalOfficeManager builder. [\#81](https://github.com/jodconverter/jodconverter/issues/81)
- gradlew is not executable [\#74](https://github.com/jodconverter/jodconverter/issues/74)
- Check workingDir for writing [\#67](https://github.com/jodconverter/jodconverter/issues/67)
- no way to specify filter parameters with CLI version [\#63](https://github.com/jodconverter/jodconverter/issues/63)
- No-args constructor for DocumentFormat does not exist [\#59](https://github.com/jodconverter/jodconverter/issues/59)
- Added bean and property for ProcessManager for custom implementation. [\#104](https://github.com/jodconverter/jodconverter/pull/104) ([alexey-atiskov](https://github.com/alexey-atiskov))
- http is deprecated AFAIU [\#91](https://github.com/jodconverter/jodconverter/pull/91) ([EugenMayer](https://github.com/EugenMayer))
- Add Server / Client hint for better understanding [\#90](https://github.com/jodconverter/jodconverter/pull/90) ([EugenMayer](https://github.com/EugenMayer))
- Add BMP support [\#86](https://github.com/jodconverter/jodconverter/pull/86) ([ggsurrel](https://github.com/ggsurrel))
- 🐧 Supporting more platforms [\#85](https://github.com/jodconverter/jodconverter/pull/85) ([damien-vdb](https://github.com/damien-vdb))
- Make `gradlew` executable \(refs \#74\) [\#78](https://github.com/jodconverter/jodconverter/pull/78) ([michelole](https://github.com/michelole))
- remove sourcefile extension check [\#65](https://github.com/jodconverter/jodconverter/pull/65) ([aruis](https://github.com/aruis))
- Update LocalOfficeUtils.java,fix Mac OS find Officehome [\#64](https://github.com/jodconverter/jodconverter/pull/64) ([aruis](https://github.com/aruis))
- Added JPG, TIFF, and GIF support [\#60](https://github.com/jodconverter/jodconverter/pull/60) ([recurve](https://github.com/recurve))

**Fixed bugs:**

- Incorrect usage of Validate.notNull method [\#97](https://github.com/jodconverter/jodconverter/issues/97)

**Closed issues:**

- javadocs? [\#69](https://github.com/jodconverter/jodconverter/issues/69)
- can't build successfully on OS X [\#68](https://github.com/jodconverter/jodconverter/issues/68)
- Use TableOfContentUpdaterFilter in Spring Boot [\#55](https://github.com/jodconverter/jodconverter/issues/55)

## [v4.2.0](https://github.com/jodconverter/jodconverter/tree/v4.2.0) (2018-03-01)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.1.1...v4.2.0)

**Implemented enhancements:**

- Add JodConverter Online to the spring boot starter [\#56](https://github.com/jodconverter/jodconverter/issues/56)
- Use of Spring 5 with Spring Boot 1.x is unusual [\#54](https://github.com/jodconverter/jodconverter/issues/54)

**Closed issues:**

- Wrong scope for `spring-boot-configuration-processor`  [\#53](https://github.com/jodconverter/jodconverter/issues/53)
- Consider not adding "default to" in property description [\#52](https://github.com/jodconverter/jodconverter/issues/52)
- Support for the latest LibreOffice [\#51](https://github.com/jodconverter/jodconverter/issues/51)
- Don't start or kill libreoffice related processes automatically. [\#49](https://github.com/jodconverter/jodconverter/issues/49)
- Warning: Office process died with exit code 81; restarting it [\#48](https://github.com/jodconverter/jodconverter/issues/48)

## [v4.1.1](https://github.com/jodconverter/jodconverter/tree/v4.1.1) (2018-02-17)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.1.0...v4.1.1)

**Implemented enhancements:**

- Changing Margins when converting .rtf to .pdf [\#50](https://github.com/jodconverter/jodconverter/issues/50)
- Send load and store custom FilterOptions when using jodconverter-online [\#47](https://github.com/jodconverter/jodconverter/issues/47)
- When using Input/Output streams, temporary file are created with the tmp extension. [\#46](https://github.com/jodconverter/jodconverter/issues/46)
- Add merging support. [\#45](https://github.com/jodconverter/jodconverter/issues/45)
- Add support for Flat XML formats [\#44](https://github.com/jodconverter/jodconverter/issues/44)
- Add SSL support for JODConvetrer Online module [\#35](https://github.com/jodconverter/jodconverter/issues/35)
- Create a sample application using the jodconverter-spring-boot-starter module. [\#34](https://github.com/jodconverter/jodconverter/issues/34)

**Fixed bugs:**

- Online conversion never fill OutputStream nor deletes the temp file when converting to OutputStream [\#43](https://github.com/jodconverter/jodconverter/issues/43)

**Closed issues:**

- Merge multiple fodt files and convert to PDF  [\#42](https://github.com/jodconverter/jodconverter/issues/42)
- custome html format [\#41](https://github.com/jodconverter/jodconverter/issues/41)
- Jodconverter and office in different hosts [\#40](https://github.com/jodconverter/jodconverter/issues/40)
- How to configure the macOS officeHome？ [\#33](https://github.com/jodconverter/jodconverter/issues/33)
- Updating from 4.0.0-RELEASE to 4.1.0 where are this classes? [\#32](https://github.com/jodconverter/jodconverter/issues/32)
- Is jodconverter-online published? [\#31](https://github.com/jodconverter/jodconverter/issues/31)
- Encoding support [\#30](https://github.com/jodconverter/jodconverter/issues/30)

## [v4.1.0](https://github.com/jodconverter/jodconverter/tree/v4.1.0) (2017-10-23)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.0.0...v4.1.0)

**Implemented enhancements:**

- Create a filter to update the table of content of a text document. [\#27](https://github.com/jodconverter/jodconverter/issues/27)
- Open Office template types are missing from the documentformat.json [\#24](https://github.com/jodconverter/jodconverter/issues/24)
- Import documentation from former JODConverter site. [\#7](https://github.com/jodconverter/jodconverter/issues/7)
- Add release feature [\#6](https://github.com/jodconverter/jodconverter/issues/6)
- Publish to Maven Central [\#5](https://github.com/jodconverter/jodconverter/issues/5)

**Closed issues:**

- LibreOffice Portable support for Windows [\#28](https://github.com/jodconverter/jodconverter/issues/28)
- Macros not being run during conversion [\#26](https://github.com/jodconverter/jodconverter/issues/26)
- Several examples in Configuration contain ; instead of . [\#23](https://github.com/jodconverter/jodconverter/issues/23)
- Advice on how to implement a custom local office task [\#22](https://github.com/jodconverter/jodconverter/issues/22)
- Is there going to be a new release soon? [\#21](https://github.com/jodconverter/jodconverter/issues/21)
- Create a logo for the JODConverter project. [\#18](https://github.com/jodconverter/jodconverter/issues/18)
- How do i convert a partucular word document\(docx\) page to html? [\#17](https://github.com/jodconverter/jodconverter/issues/17)
- How to set content encoding of target file? [\#16](https://github.com/jodconverter/jodconverter/issues/16)
- Not able to start multiple OfficeProcesses [\#15](https://github.com/jodconverter/jodconverter/issues/15)
- some class can't find from the maven jar [\#14](https://github.com/jodconverter/jodconverter/issues/14)
- Error trying to build. [\#11](https://github.com/jodconverter/jodconverter/issues/11)
- Issue with soffice.bin and findPid in MacOS [\#10](https://github.com/jodconverter/jodconverter/issues/10)
- how to prevent org.jodconverter.sample.web.WebappContextListener being a listener [\#9](https://github.com/jodconverter/jodconverter/issues/9)

**Merged pull requests:**

- Add open document templates to document formats. [\#25](https://github.com/jodconverter/jodconverter/pull/25) ([benelot](https://github.com/benelot))
- Using remote LibreOffice Online server on demand [\#20](https://github.com/jodconverter/jodconverter/pull/20) ([Wastack](https://github.com/Wastack))
- Add a Gitter chat badge to README.md [\#19](https://github.com/jodconverter/jodconverter/pull/19) ([gitter-badger](https://github.com/gitter-badger))
- Update publish-projects.gradle [\#13](https://github.com/jodconverter/jodconverter/pull/13) ([michelole](https://github.com/michelole))
- Update build.gradle [\#12](https://github.com/jodconverter/jodconverter/pull/12) ([michelole](https://github.com/michelole))

## [v4.0.0](https://github.com/jodconverter/jodconverter/tree/v4.0.0) (2017-04-28)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/f8405ead270869f5bc88a50a44ed0d97166f949e...v4.0.0)

**Closed issues:**

- Unable to compile code [\#4](https://github.com/jodconverter/jodconverter/issues/4)

**Merged pull requests:**

- Spring 4.X bean. Compatibility with JRE 1.6. [\#2](https://github.com/jodconverter/jodconverter/pull/2) ([joseluisll](https://github.com/joseluisll))
- Added OpenOffice 4 Default HOME. [\#1](https://github.com/jodconverter/jodconverter/pull/1) ([joseluisll](https://github.com/joseluisll))



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
