# Changelog

## [v4.4.9](https://github.com/jodconverter/jodconverter/tree/v4.4.9) (2025-05-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.8...v4.4.9)

**Implemented enhancements:**

- Remove the disableOpengl option. [\#426](https://github.com/jodconverter/jodconverter/issues/426)

**Fixed bugs:**

- Libre Office disconnects when converting a password protected ODT file to
  PDF [\#423](https://github.com/jodconverter/jodconverter/issues/423)
- On more recent Java version like Java 17, JODconverter has runtime exception Unable to create instance
  DocumentFormat [\#408](https://github.com/jodconverter/jodconverter/issues/408)

## [v4.4.8](https://github.com/jodconverter/jodconverter/tree/v4.4.8) (2024-09-01)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.7...v4.4.8)

**Implemented enhancements:**

- Add xlsm support in DefaultDocumentFormatRegistry [\#391](https://github.com/jodconverter/jodconverter/issues/391)

**Fixed bugs:**

- No qualifying bean of type 'org.jodconverter.core.DocumentConverter'
  available [\#390](https://github.com/jodconverter/jodconverter/issues/390)
- Task keeps hanging when using Remote JodConverter [\#384](https://github.com/jodconverter/jodconverter/issues/384)
- Scanner (IO) bug [\#383](https://github.com/jodconverter/jodconverter/issues/383)

**Closed issues:**

- Incompatible with LO 24.x \(probably?\) on macos [\#386](https://github.com/jodconverter/jodconverter/issues/386)
- Removed unnecessary `@ConfigurationPropertiesScan` on `JodConverterLocalProperties` and
  `JodConverterRemoteProperties` [\#377](https://github.com/jodconverter/jodconverter/issues/377)
- Information about supported properties and their
  meaning [\#372](https://github.com/jodconverter/jodconverter/issues/372)
- The Word document with more than 12 pages will automatically cancel the
  task. [\#364](https://github.com/jodconverter/jodconverter/issues/364)
- pptx file conversion of PDF failed [\#359](https://github.com/jodconverter/jodconverter/issues/359)

**Merged pull requests:**

- Remove unnecessary ConfigurationPropertiesScan
  annotation [\#378](https://github.com/jodconverter/jodconverter/pull/378) ([bianjp](https://github.com/bianjp))

## [v4.4.7](https://github.com/jodconverter/jodconverter/tree/v4.4.7) (2023-12-13)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.6...v4.4.7)

**Fixed bugs:**

- Using SpringBoot autoconfiguration with a remote setup fails with
  ClassNotFoundException [\#331](https://github.com/jodconverter/jodconverter/issues/331)

**Merged pull requests:**

- add support for websocket urps available \>= LibreOffice
  24.2 [\#355](https://github.com/jodconverter/jodconverter/pull/355) ([caolanm](https://github.com/caolanm))
- Add support for additional HTML extension
  alias [\#338](https://github.com/jodconverter/jodconverter/pull/338) ([LiamMacP](https://github.com/LiamMacP))

## [v4.4.6](https://github.com/jodconverter/jodconverter/tree/v4.4.6) (2023-01-27)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.5...v4.4.6)

**Implemented enhancements:**

- Added the ability to provide a custom-document-formats.json
  file [\#323](https://github.com/jodconverter/jodconverter/issues/323)
- Jodconverter not working with spring-boot 3 [\#320](https://github.com/jodconverter/jodconverter/issues/320)

**Merged pull requests:**

- Spring boot 3.0 compatibility fixes
  \#320 [\#322](https://github.com/jodconverter/jodconverter/pull/322) ([EugenMayer](https://github.com/EugenMayer))

## [v4.4.5](https://github.com/jodconverter/jodconverter/tree/v4.4.5) (2022-12-21)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.4...v4.4.5)

**Implemented enhancements:**

- ExternalOfficeManager does not work [\#278](https://github.com/jodconverter/jodconverter/issues/278)

**Fixed bugs:**

- java.lang.NullPointerException: byExtension\(extension\) must not be
  null [\#319](https://github.com/jodconverter/jodconverter/issues/319)
- Missing classes exception for the
  DefaultDocumentFormatRegistry [\#317](https://github.com/jodconverter/jodconverter/issues/317)
- Failed to start bean 'documentationPluginsBootstrapper' when starting rest
  version [\#315](https://github.com/jodconverter/jodconverter/issues/315)
- IndexOutOfBoundsException: Index: 1, Size: 1 when Run multiple tasks in
  concurrent. [\#310](https://github.com/jodconverter/jodconverter/issues/310)
- Got error when using org.jodconverter:
  jodconverter-local-lo [\#309](https://github.com/jodconverter/jodconverter/issues/309)

**Merged pull requests:**

- Migrate to swagger v3 / openapi - fixes
  \#317 [\#318](https://github.com/jodconverter/jodconverter/pull/318) ([EugenMayer](https://github.com/EugenMayer))

## [v4.4.4](https://github.com/jodconverter/jodconverter/tree/v4.4.4) (2022-09-22)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.3...v4.4.4)

**Implemented enhancements:**

- Change default UpdateDocMode behavior and add new option to keep old behavior.
- Added FilterData and FilterOption do DocumentFormat builder.

## [v4.4.3](https://github.com/jodconverter/jodconverter/tree/v4.4.3) (2022-09-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.2...v4.4.3)

**Implemented enhancements:**

- Provide builds of both OpenOffice and LibreOffice dependencies in the maven
  center [\#273](https://github.com/jodconverter/jodconverter/issues/273)
- support keepAliveOnShutdown through CLI [\#269](https://github.com/jodconverter/jodconverter/issues/269)
- Issues converting potx and xltx [\#259](https://github.com/jodconverter/jodconverter/issues/259)

**Fixed bugs:**

- gradle build fail~~~~ with libreoffice 7.1.5 [\#271](https://github.com/jodconverter/jodconverter/issues/271)

**Closed issues:**

- Task :distZip FAILED [\#303](https://github.com/jodconverter/jodconverter/issues/303)
- Added WEB document family [\#297](https://github.com/jodconverter/jodconverter/issues/297)
- Spring configuration metadata json not generated [\#295](https://github.com/jodconverter/jodconverter/issues/295)
- Specific Exception for Password Protected files [\#233](https://github.com/jodconverter/jodconverter/issues/233)

**Merged pull requests:**

- Update to spring boot 2.7.3 to fix
  CVEs [\#307](https://github.com/jodconverter/jodconverter/pull/307) ([EugenMayer](https://github.com/EugenMayer))
- Build spring-boot configuration metadata into jar
  \#295 [\#296](https://github.com/jodconverter/jodconverter/pull/296) ([shysteph](https://github.com/shysteph))
- add format definition for PowerPoint XML templates
  \(\#259\) [\#270](https://github.com/jodconverter/jodconverter/pull/270) ([stellingsimon](https://github.com/stellingsimon))
- ✨new document format
  xltx [\#257](https://github.com/jodconverter/jodconverter/pull/257) ([jgoldhammer](https://github.com/jgoldhammer))

## [v4.4.2](https://github.com/jodconverter/jodconverter/tree/v4.4.2) (2021-02-10)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.1...v4.4.2)

**Fixed bugs:**

- DocumentFormat.input family should be optional. [\#249](https://github.com/jodconverter/jodconverter/issues/249)

## [v4.4.1](https://github.com/jodconverter/jodconverter/tree/v4.4.1) (2021-02-10)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.0...v4.4.1)

**Fixed bugs:**

- regression: document-formats with singular extension field are not supported
  anymore. [\#248](https://github.com/jodconverter/jodconverter/issues/248)
- LocalOfficeManager\#afterStartProcessDelay is not validated
  properly. [\#246](https://github.com/jodconverter/jodconverter/issues/246)

**Merged pull requests:**

- bugfix filename contains CJK characters cause error, change to UTF-8
  encoding [\#245](https://github.com/jodconverter/jodconverter/pull/245) ([chunlinyao](https://github.com/chunlinyao))

## [v4.4.0](https://github.com/jodconverter/jodconverter/tree/v4.4.0) (2021-01-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.3.0...v4.4.0)

**Implemented enhancements:**

- Add the ability to wait after an attempt to start an office process before trying to
  connect. [\#244](https://github.com/jodconverter/jodconverter/issues/244)
- Add dotx conversion out of the box [\#213](https://github.com/jodconverter/jodconverter/issues/213)
- Add ability to attach OfficeManager to already running
  Process [\#203](https://github.com/jodconverter/jodconverter/issues/203)
- JODConverterBean, expose office manager to add filters
  support [\#201](https://github.com/jodconverter/jodconverter/issues/201)
- Make office process management asynchronous \(start, restart,
  etc\). [\#200](https://github.com/jodconverter/jodconverter/issues/200)
- Remove unnecessary dependencies. [\#198](https://github.com/jodconverter/jodconverter/issues/198)
- Allow conversions on remote host with LibreOffice directly (
  ExternalOfficeManager) [\#195](https://github.com/jodconverter/jodconverter/issues/195)
- Use ExternalOfficeManager with a pool of processes [\#191](https://github.com/jodconverter/jodconverter/issues/191)
- Allow process restart to be asynchronous [\#171](https://github.com/jodconverter/jodconverter/issues/171)
- Add ability to reuse already running libreoffice
  instances [\#72](https://github.com/jodconverter/jodconverter/issues/72)

**Fixed bugs:**

- ExternalOfficeManager :: makeTempDir not called when connectOnStart =
  false [\#211](https://github.com/jodconverter/jodconverter/issues/211)

**Closed issues:**

- ExternalOfficeManager always connects sockets to
  127.0.0.1 [\#241](https://github.com/jodconverter/jodconverter/issues/241)
- DocUpdateMode not working? [\#227](https://github.com/jodconverter/jodconverter/issues/227)
- Depending on the operating system, /tmp is getting regularly
  cleaned [\#220](https://github.com/jodconverter/jodconverter/issues/220)
- Temporary file name added in CSV -\> PDF conversion [\#219](https://github.com/jodconverter/jodconverter/issues/219)
- Wiki page for LibreOffice Online example code should be
  RemoteOfficeManager [\#216](https://github.com/jodconverter/jodconverter/issues/216)
- Wiki page for LibreOffice Online still references "
  jodconverter-online" [\#214](https://github.com/jodconverter/jodconverter/issues/214)
- How to disable AutoCalculate? [\#207](https://github.com/jodconverter/jodconverter/issues/207)
- JODConverter Reached limit Tasks and Restart  [\#196](https://github.com/jodconverter/jodconverter/issues/196)

**Merged pull requests:**

- Make 127.0.0.1 in socket connection
  configurable [\#242](https://github.com/jodconverter/jodconverter/pull/242) ([nikowitt](https://github.com/nikowitt))
- bugfix ps args truncated at 125
  chars [\#238](https://github.com/jodconverter/jodconverter/pull/238) ([chunlinyao](https://github.com/chunlinyao))

## [v4.3.0](https://github.com/jodconverter/jodconverter/tree/v4.3.0) (2020-03-05)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.4...v4.3.0)

**Implemented enhancements:**

- Rename jodconverter-online module to
  jodconverter-remote [\#190](https://github.com/jodconverter/jodconverter/issues/190)
- Issues in java11, requiring package names refactoring [\#178](https://github.com/jodconverter/jodconverter/issues/178)

**Fixed bugs:**

- regression: Upgrade to jodconverter-local 4.2.3 imply to also add a dependency to
  jodconverter-core [\#183](https://github.com/jodconverter/jodconverter/issues/183)
- Unthrown MalformedInputException when looking for soffice
  PID [\#180](https://github.com/jodconverter/jodconverter/issues/180)

## [v4.2.4](https://github.com/jodconverter/jodconverter/tree/v4.2.4) (2020-01-16)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.3...v4.2.4)

**Fixed bugs:**

- regression: Upgrade to jodconverter-local 4.2.3 imply to also add a dependency to
  jodconverter-core [\#183](https://github.com/jodconverter/jodconverter/issues/183)

## [v4.2.3](https://github.com/jodconverter/jodconverter/tree/v4.2.3) (2020-01-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.2...v4.2.3)

**Implemented enhancements:**

- sample-webapp throws
  java.lang.IllegalArgumentException [\#165](https://github.com/jodconverter/jodconverter/issues/165)
- Add support for "XHTML" LibreOffice filters [\#134](https://github.com/jodconverter/jodconverter/issues/134)
- Add all supported extensions to PDF conversion
  support. [\#132](https://github.com/jodconverter/jodconverter/issues/132)
- Java 11 compatibility [\#127](https://github.com/jodconverter/jodconverter/issues/127)
- Add support for "XHTML" LibreOffice
  filters [\#135](https://github.com/jodconverter/jodconverter/pull/135) ([linux-warrior](https://github.com/linux-warrior))

**Fixed bugs:**

- sample-webapp throws
  org.apache.commons.io.FileExistsException [\#166](https://github.com/jodconverter/jodconverter/issues/166)
- Errors in tests when building jodconverter 4.2.2 with Java
  9+ [\#159](https://github.com/jodconverter/jodconverter/issues/159)
- Could not establish connection [\#148](https://github.com/jodconverter/jodconverter/issues/148)
- LibreOffice path on FreeBSD is not autodetected [\#137](https://github.com/jodconverter/jodconverter/issues/137)
- ExternalOfficeManager creates temporary files in the current
  directory [\#130](https://github.com/jodconverter/jodconverter/issues/130)
- class ExternalOfficeManager is not Public [\#121](https://github.com/jodconverter/jodconverter/issues/121)
- Build fails with JDK10 on macOS [\#79](https://github.com/jodconverter/jodconverter/issues/79)

## [v4.2.2](https://github.com/jodconverter/jodconverter/tree/v4.2.2) (2018-11-30)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.1...v4.2.2)

**Implemented enhancements:**

- Create a filter to embed linked images into output
  document. [\#117](https://github.com/jodconverter/jodconverter/issues/117)
- Fix ExternalOfficeManager can't convert streams [\#116](https://github.com/jodconverter/jodconverter/issues/116)
- Filter chain should be reusable without reset [\#112](https://github.com/jodconverter/jodconverter/issues/112)
- static JodConverter.convert methods dont work with
  ExternalOfficeManagerBuilder\(\) [\#111](https://github.com/jodconverter/jodconverter/issues/111)

**Fixed bugs:**

- Fix regression introduced by \#99. Use AOO libraries.  [\#113](https://github.com/jodconverter/jodconverter/issues/113)

## [v4.2.1](https://github.com/jodconverter/jodconverter/tree/v4.2.1) (2018-11-02)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.0...v4.2.1)

**Implemented enhancements:**

- Stop using deprecated command lines options using
  LibreOffice [\#106](https://github.com/jodconverter/jodconverter/issues/106)
- Redirect office output to jodconverter logs. [\#105](https://github.com/jodconverter/jodconverter/issues/105)
- Add support for auto detecting OpenOffice4 path for linux DEB-based
  Installation [\#101](https://github.com/jodconverter/jodconverter/issues/101)
- Add property for setting ProcessManager explicitly [\#100](https://github.com/jodconverter/jodconverter/issues/100)
- Use LibreOffice libraries instead of Apache Open-Office ones by
  default [\#99](https://github.com/jodconverter/jodconverter/issues/99)
- Add a property to trust all certificate in jodconverter-online
  module [\#98](https://github.com/jodconverter/jodconverter/issues/98)
- Add properties to the spring-boot-starter allowing document formats
  customization. [\#94](https://github.com/jodconverter/jodconverter/issues/94)
- Add templateProfileDirOrDefault option to the LocalOfficeManager
  builder. [\#81](https://github.com/jodconverter/jodconverter/issues/81)
- gradlew is not executable [\#74](https://github.com/jodconverter/jodconverter/issues/74)
- Check workingDir for writing [\#67](https://github.com/jodconverter/jodconverter/issues/67)
- No way to specify filter parameters with CLI version [\#63](https://github.com/jodconverter/jodconverter/issues/63)
- No-args constructor for DocumentFormat does not exist [\#59](https://github.com/jodconverter/jodconverter/issues/59)
- Added bean and property for ProcessManager for custom
  implementation. [\#104](https://github.com/jodconverter/jodconverter/pull/104) ([alexey-atiskov](https://github.com/alexey-atiskov))
- Add Server / Client hint for better
  understanding [\#90](https://github.com/jodconverter/jodconverter/pull/90) ([EugenMayer](https://github.com/EugenMayer))
- Add BMP support [\#86](https://github.com/jodconverter/jodconverter/pull/86) ([ggsurrel](https://github.com/ggsurrel))
- Supporting more
  platforms [\#85](https://github.com/jodconverter/jodconverter/pull/85) ([damienvdb06](https://github.com/damienvdb06))
- Make `gradlew` executable \(refs
  \#74\) [\#78](https://github.com/jodconverter/jodconverter/pull/78) ([michelole](https://github.com/michelole))
- remove sourcefile extension
  check [\#65](https://github.com/jodconverter/jodconverter/pull/65) ([aruis](https://github.com/aruis))
- Update LocalOfficeUtils.java,fix Mac OS find
  Officehome [\#64](https://github.com/jodconverter/jodconverter/pull/64) ([aruis](https://github.com/aruis))
- Added JPG, TIFF, and GIF
  support [\#60](https://github.com/jodconverter/jodconverter/pull/60) ([recurve](https://github.com/recurve))

**Fixed bugs:**

- Incorrect usage of Validate.notNull method [\#97](https://github.com/jodconverter/jodconverter/issues/97)

## [v4.2.0](https://github.com/jodconverter/jodconverter/tree/v4.2.0) (2018-03-01)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.1.1...v4.2.0)

**Implemented enhancements:**

- Add JodConverter Online to the spring boot starter [\#56](https://github.com/jodconverter/jodconverter/issues/56)

**Closed issues:**

- Use of Spring 5 with Spring Boot 1.x is unusual [\#54](https://github.com/jodconverter/jodconverter/issues/54)
- Wrong scope for `spring-boot-configuration-processor`  [\#53](https://github.com/jodconverter/jodconverter/issues/53)
- Consider not adding "default to" in property description [\#52](https://github.com/jodconverter/jodconverter/issues/52)
- Support for the latest LibreOffice [\#51](https://github.com/jodconverter/jodconverter/issues/51)

## [v4.1.1](https://github.com/jodconverter/jodconverter/tree/v4.1.1) (2018-02-16)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.1.0...v4.1.1)

**Implemented enhancements:**

- Changing Margins when converting .rtf to .pdf [\#50](https://github.com/jodconverter/jodconverter/issues/50)
- Send load and store custom FilterOptions when using
  jodconverter-online [\#47](https://github.com/jodconverter/jodconverter/issues/47)
- When using Input/Output streams, temporary file are created with the tmp
  extension. [\#46](https://github.com/jodconverter/jodconverter/issues/46)
- Add merging support. [\#45](https://github.com/jodconverter/jodconverter/issues/45)
- Add support for Flat XML formats [\#44](https://github.com/jodconverter/jodconverter/issues/44)
- Add SSL support for JODConvetrer Online module [\#35](https://github.com/jodconverter/jodconverter/issues/35)
- Create a sample application using the jodconverter-spring-boot-starter
  module. [\#34](https://github.com/jodconverter/jodconverter/issues/34)

**Fixed bugs:**

- Online conversion never fill OutputStream nor deletes the temp file when converting to
  OutputStream [\#43](https://github.com/jodconverter/jodconverter/issues/43)

**Closed issues:**

- Merge multiple fodt files and convert to PDF  [\#42](https://github.com/jodconverter/jodconverter/issues/42)
