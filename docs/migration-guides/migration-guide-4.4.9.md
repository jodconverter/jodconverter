This guide discusses migration from JODConverter version 4.4.8 to version 4.4.9

## Background

The `disableOpengl` option is no longer supported. On recent LibreOffice versions, the option didn't work anymore. To
disable OpenGL, you now must use the
[templateProfileDir](https://github.com/jodconverter/jodconverter/wiki/Configuration#file_foldertemplateprofiledir)
option and disable OpenGL by following
[these suggestions](https://wiki.documentfoundation.org/OpenGL#:~:text=LibreOffice%205.3%20and%20newer%3A,Click%20%22Apply%20Changes%20and%20Restart%22)