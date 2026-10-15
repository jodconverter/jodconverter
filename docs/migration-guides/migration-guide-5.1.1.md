This guide discusses migration from JODConverter version 5.1.0 to version 5.1.1

## Background

This release contains two bug fixes, a change of the level at which the output of the office is logged, and one new exception class. Nothing changes or goes away in the API, and most applications have nothing to change.

See the [release notes](../release-notes/release-notes-5.1.1.md).

## What to check

- **A wrong password.** With the office libraries of LibreOffice (`jodconverter-local-lo`), a conversion with a wrong password now fails with a `PasswordProtectedException`, whose message says that the password is wrong, as it did with those of OpenOffice. If your application recognized the failure of 5.1.0 instead (an `OfficeException` caused by `Could not open document`, or by an `IllegalArgumentException`), it can catch `PasswordProtectedException` alone.

- **The logs of the office.** What the office process writes on its error output is logged at the `WARN` level instead of `ERROR`, with the `org.jodconverter.local.office.VerboseProcess` logger, and blank lines are no longer logged. If you alert on the `ERROR` lines of that logger, or filter them out, look at its `WARN` lines instead. A failure of a conversion is still reported by an exception.

- **A page export beyond the last page.** A [page export](../getting-started/page-images.md) whose range goes beyond the last page fails with a `PageOutOfRangeException`, a subclass of `OfficeException` with the same message as before. Code that catches `OfficeException` keeps working; code that read the message to tell this case from a failure of the office can catch `PageOutOfRangeException` and use `getPage()` and `getPageCount()`.

- **The size of slides exported without a size.** On Windows with a display scaling, a page export without a size gave larger images than documented (1.25 times larger at 125%, for example). The images now have the size of the page at 96 dpi on every machine. If your application relied on the larger images, give the size you want with `size(...)`, `width(...)` or `height(...)`.
