# Command Line

> Whenever OpenOffice.org (OOo for short) is mentioned, this can generally be interpreted to include any office suite derived from OOo such as [Apache OpenOffice](https://www.openoffice.org) and [LibreOffice](https://www.libreoffice.org).

The command line tool provides a good way to check that everything is working, i.e. that you have the right OOo version installed etc. To convert a document, just use the provided batch file, located in the bin directory of the cli module distribution:

```
jodconverter-cli test.odt test.pdf
```

jodconverter-cli can be used to convert multiple documents according to the given arguments. Use to -h switch in order to know all the available options of the cli module:

```
jodconverter-cli -h
```