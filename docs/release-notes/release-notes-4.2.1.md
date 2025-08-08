# Change Log

## [4.2.1](https://github.com/jodconverter/jodconverter/tree/v4.2.1) (2018-11-02)

[Full Changelog](https://github.com/jodconverter/jodconverter/compare/v4.2.0...HEAD)

### **Implemented enhancements**

- Stop using deprecated command lines options using
  LibreOffice [\#106](https://github.com/jodconverter/jodconverter/issues/106)
- Redirect office output to jodconverter logs. [\#105](https://github.com/jodconverter/jodconverter/issues/105)
- Add support for auto detecting OpenOffice4 path for linux DEB-based
  Installation [\#101](https://github.com/jodconverter/jodconverter/issues/101)
- Add property for setting ProcessManager explicitly [\#100](https://github.com/jodconverter/jodconverter/issues/100)
- Use LibreOffice libraries instead of Apache Open-Office ones by
  default [\#99](https://github.com/jodconverter/jodconverter/issues/99)
- Add a property to trust all certificate in jodconverter-online
  module [\#98](https://github.com/jodconverter/jodconverter/issues/98)
- Add properties to the spring-boot-starter allowing document formats
  customization. [\#94](https://github.com/sbraconnier/jodconverter/issues/94)
- Add templateProfileDirOrDefault option to the LocalOfficeManager
  builder. [\#81](https://github.com/sbraconnier/jodconverter/issues/81)
- gradlew is not executable [\#74](https://github.com/sbraconnier/jodconverter/issues/74)
- Check workingDir for writing [\#67](https://github.com/sbraconnier/jodconverter/issues/67)
- No way to specify filter parameters with CLI version [\#63](https://github.com/sbraconnier/jodconverter/issues/63)
- No-args constructor for DocumentFormat does not exist [\#59](https://github.com/sbraconnier/jodconverter/issues/59)
- Added bean and property for ProcessManager for custom
  implementation. [\#104](https://github.com/sbraconnier/jodconverter/pull/104) ([alexey-atiskov](https://github.com/alexey-atiskov))
- Add Server / Client hint for better
  understanding [\#90](https://github.com/sbraconnier/jodconverter/pull/90) ([EugenMayer](https://github.com/EugenMayer))
- Add BMP support [\#86](https://github.com/sbraconnier/jodconverter/pull/86) ([ggsurrel](https://github.com/ggsurrel))
- Supporting more
  platforms [\#85](https://github.com/sbraconnier/jodconverter/pull/85) ([damienvdb06](https://github.com/damienvdb06))
- Make `gradlew` executable \(refs
  \#74\) [\#78](https://github.com/sbraconnier/jodconverter/pull/78) ([michelole](https://github.com/michelole))
- remove sourcefile extension
  check [\#65](https://github.com/sbraconnier/jodconverter/pull/65) ([aruis](https://github.com/aruis))
- Update LocalOfficeUtils.java,fix Mac OS find
  Officehome [\#64](https://github.com/sbraconnier/jodconverter/pull/64) ([aruis](https://github.com/aruis))
- Added JPG, TIFF, and GIF
  support [\#60](https://github.com/sbraconnier/jodconverter/pull/60) ([recurve](https://github.com/recurve))

### **Fixed bugs**

- Incorrect usage of Validate.notNull method [\#97](https://github.com/sbraconnier/jodconverter/issues/97)