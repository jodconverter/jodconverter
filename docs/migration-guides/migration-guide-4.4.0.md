This guide discusses migration from JODConverter version 4.3.0 to version 4.4.0

## Asynchronous processes management

To improve the startup time of a server using jodconverter, the office processes management is now asynchronous. Suppose
we have an office manager started this way:

```java
OfficeManager officeManager =
    LocalOfficeManager.builder()
        .portNumbers(2002, 2003, 2004, 2005)
        .build();
officeManager.start();
```

Before 4.4.0, the `officeManager.start()` would wait for all the office processes to be started. Now, the call returns
immediately, meaning a faster starting process, and only error logs will be produced if anything goes wrong.

To reproduce the behavior from an older version of jodconverter, the `startFailFast` property must be set to `true`:

```java
OfficeManager officeManager =
    LocalOfficeManager.builder()
        .portNumbers(2002, 2003, 2004, 2005)
        .startFailFast(true)
        .build();
officeManager.start();
```

## Breaking Changes

### Existing process management.

The `killExistingProcess` option has been replaced by
the [existingProcessAction](https://github.com/sbraconnier/jodconverter/wiki/Configuration#capital_abcdexistingprocessaction).
If the `killExistingProcess` was not used, then there is nothing to do; the behavior remains the same. But if
`killExistingProcess` was set to false, you must now set the `existingProcessAction` to `ExistingProcessAction.FAIL`.