This guide discusses migration from JODConverter version 5.0.0 to version 5.0.1

## Background

This release only contains three bug fixes. It shouldn't have any impact.

If you worked around the first one by building a new `LocalConverter` for each `exportPages(...)`, you can go back to
one converter: its filters are now applied at every export.

See the [release notes](../release-notes/release-notes-5.0.1.md).
