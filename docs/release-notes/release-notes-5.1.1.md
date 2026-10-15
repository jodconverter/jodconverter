# Changelog

## [v5.1.1](https://github.com/jodconverter/jodconverter/tree/v5.1.1) (2026-10-15)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v5.1.0...v5.1.1)

A patch release: a wrong password is reported as such with the office libraries of LibreOffice, slides exported without a size have the same size on every machine, the error output of the office is no longer logged as errors, and a page export beyond the last page fails with an exception of its own. Nothing changes or goes away in the API; one exception class is added.

### **Implemented enhancements**

- **`PageOutOfRangeException`.** A [page export](../getting-started/page-images.md) whose range goes beyond the last page of the document failed with a plain `OfficeException`, the same type as a failure of the office. It now fails with a `PageOutOfRangeException`, a subclass of `OfficeException` in `org.jodconverter.local.office`, which gives the page asked for (`getPage()`) and the number of pages of the document (`getPageCount()`), so that an application can tell a wrong request from a failed office. The message is unchanged, and code that catches `OfficeException` keeps working ([#591](https://github.com/jodconverter/jodconverter/pull/591)).

### **Changed behaviors**

- **The error output of the office process is logged as warnings.** JODConverter relays what the office process writes to its logs, with the `org.jodconverter.local.office.VerboseProcess` logger, and it logged the error output at the `ERROR` level. The office writes things there that are no failure: at each first start of a profile, which is each start of an office process by default, its XML parser reports `Entity: line 1: parser error : Document is empty`. The error output is now logged at the `WARN` level, the standard output stays at `INFO`, and blank lines are no longer logged. What fails is still reported by an exception. The [FAQ](../faq.md) says where the message comes from and how to leave the output of the office out of the logs ([#590](https://github.com/jodconverter/jodconverter/pull/590)).

### **Fixed bugs**

- **A wrong password was not reported with the office libraries of LibreOffice.** With `jodconverter-local-lo`, which most applications use, the conversion of a protected document with a wrong password failed with `OfficeException: Local conversion failed`, caused by `Could not open document` for an OpenDocument file or by an `IllegalArgumentException` for a Microsoft Office file, instead of the `PasswordProtectedException` of 5.1.0, whose message says that the password is wrong. A missing password was reported correctly. The office libraries of OpenOffice, with which the project ran its tests, did not show the bug ([#587](https://github.com/jodconverter/jodconverter/pull/587)).

- **Slides exported without a size followed the resolution of the display.** A [page export](../getting-started/page-images.md) without a size is documented to give the size of the page at 96 dpi, but the size was left to the office, which takes the resolution of the display: on Windows with a display scaling of 125%, a 28 cm slide came out 1323 pixels wide instead of 1058. The export now gives the office the size of the page at 96 dpi, and the images have the same size on every machine ([#596](https://github.com/jodconverter/jodconverter/pull/596)).

### **Documentation**

- [Samples](../samples/index.md): one page that lists the sample applications, now six, with what each shows and a link to it in the [samples repository](https://github.com/jodconverter/jodconverter-samples), where they are built and tested with each change. It replaces the copies of three of their READMEs, which had become outdated ([#592](https://github.com/jodconverter/jodconverter/pull/592)).

### **Project**

- The local module is also tested in CI with the office libraries of LibreOffice, the ones that applications use, since the default ones, those of OpenOffice, hid the wrong-password bug ([#589](https://github.com/jodconverter/jodconverter/pull/589)).
- The CI jobs check that LibreOffice is installed at the end of each install step, and try the Windows install three times ([#588](https://github.com/jodconverter/jodconverter/pull/588)); a coverage upload that fails no longer fails the build ([#586](https://github.com/jodconverter/jodconverter/pull/586)).
- The release procedure includes the merge of `master` back into `develop` after a release ([#585](https://github.com/jodconverter/jodconverter/pull/585)).
