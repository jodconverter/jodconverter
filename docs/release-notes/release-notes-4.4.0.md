## [v4.4.0](https://github.com/jodconverter/jodconverter/tree/v4.4.0) (2021-01-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.3.0...v4.4.0)

### **Implemented enhancements**

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
  ExternalOfficeManager) [\#195](https://github.com/sbraconnier/jodconverter/issues/195)
- Use ExternalOfficeManager with a pool of processes [\#191](https://github.com/sbraconnier/jodconverter/issues/191)
- Allow process restart to be asynchronous [\#171](https://github.com/sbraconnier/jodconverter/issues/171)
- Add ability to reuse already running libreoffice
  instances [\#72](https://github.com/sbraconnier/jodconverter/issues/72)

### **Fixed bugs**

- ExternalOfficeManager :: makeTempDir not called when connectOnStart =
  false [\#211](https://github.com/sbraconnier/jodconverter/issues/211)

### **Closed issues**

- ExternalOfficeManager always connects sockets to
  127.0.0.1 [\#241](https://github.com/sbraconnier/jodconverter/issues/241)
- DocUpdateMode not working? [\#227](https://github.com/sbraconnier/jodconverter/issues/227)
- Depending on the operating system, /tmp is getting regularly
  cleaned [\#220](https://github.com/sbraconnier/jodconverter/issues/220)
- Temporary file name added in CSV -\> PDF conversion [\#219](https://github.com/sbraconnier/jodconverter/issues/219)
- Wiki page for LibreOffice Online example code should be
  RemoteOfficeManager [\#216](https://github.com/sbraconnier/jodconverter/issues/216)
- Wiki page for LibreOffice Online still references "
  jodconverter-online" [\#214](https://github.com/sbraconnier/jodconverter/issues/214)
- How to disable AutoCalculate? [\#207](https://github.com/sbraconnier/jodconverter/issues/207)
- JODConverter Reached limit Tasks and Restart  [\#196](https://github.com/sbraconnier/jodconverter/issues/196)

### **Merged pull requests**

- Make 127.0.0.1 in socket connection
  configurable [\#242](https://github.com/sbraconnier/jodconverter/pull/242) ([nikowitt](https://github.com/nikowitt))
- bugfix ps args truncated at 125
  chars [\#238](https://github.com/sbraconnier/jodconverter/pull/238) ([chunlinyao](https://github.com/chunlinyao))
