# Changelog

## [v4.4.6](https://github.com/jodconverter/jodconverter/tree/v4.4.6) (2023-01-27)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.5...v4.4.6)

**Implemented enhancements:**

- Added the ability to provide a custom-document-formats.json file [\#323](https://github.com/jodconverter/jodconverter/issues/323)
- Jodconverter not working with spring-boot 3 [\#320](https://github.com/jodconverter/jodconverter/issues/320)

**Merged pull requests:**

- Spring boot 3.0 compatibility fixes \#320 [\#322](https://github.com/jodconverter/jodconverter/pull/322) ([EugenMayer](https://github.com/EugenMayer))

## [v4.4.5](https://github.com/jodconverter/jodconverter/tree/v4.4.5) (2022-12-21)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.4.4...v4.4.5)

**Implemented enhancements:**

- ExternalOfficeManager does not work [\#278](https://github.com/jodconverter/jodconverter/issues/278)
- The address www.jodconverter.org redirects to malware [\#266](https://github.com/jodconverter/jodconverter/issues/266)

**Fixed bugs:**

- java.lang.NullPointerException: byExtension\(extension\) must not be null [\#319](https://github.com/jodconverter/jodconverter/issues/319)
- Failed to start bean 'documentationPluginsBootstrapper' when starting rest version [\#315](https://github.com/jodconverter/jodconverter/issues/315)
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
- Docx does not recognize the password [\#286](https://github.com/jodconverter/jodconverter/issues/286)
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
- fixes \#151 [\#152](https://github.com/jodconverter/jodconverter/pull/152) ([anghelutar](https://github.com/anghelutar))

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
- 🐧 Supporting more platforms [\#85](https://github.com/jodconverter/jodconverter/pull/85) ([damienvdb06](https://github.com/damienvdb06))
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

**Merged pull requests:**

- Using remote LibreOffice Online server on demand [\#20](https://github.com/jodconverter/jodconverter/pull/20) ([Wastack](https://github.com/Wastack))

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
