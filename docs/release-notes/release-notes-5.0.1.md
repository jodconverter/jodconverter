# Changelog

## [v5.0.1](https://github.com/jodconverter/jodconverter/tree/v5.0.1) (2026-10-08)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v5.0.0...v5.0.1)

A patch release with three bug fixes in the local module. Nothing changes in the API.

### **Fixed bugs**

- **Filters skipped at the second export of page images.** With a converter that has a filter chain, only the first
    `exportPages(...)` applied the filters: the following exports of the same converter exported the document as it
    was loaded. Every export now applies them. Conversions (`convert(...)`) were not affected.

- **A complete conversion failed when the office process was lost while closing the document.** LibreOffice 25.8.3 to
    26.2 may crash on Windows when a document is closed
    ([tdf#172335](https://bugs.documentfoundation.org/show_bug.cgi?id=172335)); the conversion then failed, with a
    `DisposedException` or an `OfficeException` wrapping a UNO `RuntimeException`, although its output was complete.
    The loss is now logged, the conversion succeeds, and the office process is restarted before the next
    conversion, as usual.

- **A failed listing of the processes was read as "no process found" on Windows.** The PowerShell query that lists
    the running processes fails now and then with a cancelled WMI call. The failure was not seen: the office manager
    took the empty answer for the absence of an existing office process, and a failure during the first check marked
    the process manager unusable for the life of the JVM. A query that fails now reports its failure, and the listing
    is tried three times ([#546](https://github.com/jodconverter/jodconverter/pull/546)).

### **Documentation**

- Two entries are added to the [known LibreOffice issues](../faq.md#known-libreoffice-issues): the crash on document
    close on Windows with LibreOffice 25.8.3 to 26.2, and the 35 seconds that an office process may take to accept
    its first connection on macOS 15 and later.
- The Maven snippet of the [LibreOffice Online](../getting-started/libreoffice-online.md) page showed a version that
    never existed ([#544](https://github.com/jodconverter/jodconverter/pull/544)).
