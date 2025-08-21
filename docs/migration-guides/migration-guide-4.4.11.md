This guide discusses migration from JODConverter version 4.4.10 to version 4.4.11

## Background

This only contains two enhancements. It shouldn't have any impact.

One of the changes is that the `LocalOfficeManager.isRunning()` method now returns true only when there is at least one
started office process to which JODConverter is connected.

See [\#428](https://github.com/jodconverter/jodconverter/issues/428).