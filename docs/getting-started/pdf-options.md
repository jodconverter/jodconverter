# PDF Options

`PdfOptions` sets the options of a conversion to PDF with plain Java methods, so you don't have to know the names and
values of the `FilterData` properties of the office PDF export filter. It works with the local and the remote
converters.

```java
// A preset for the common cases
converter.convert(source).to(target).with(PdfOptions.archive()).execute();

// Or built step by step
PdfOptions options =
    PdfOptions.builder()
        .version(PdfVersion.PDF_A_2B)
        .tagged(true)
        .images(images -> images.jpegQuality(85).maxResolution(300))
        .pages(pages -> pages.range("1-5"))
        .watermark(watermark -> watermark.text("DRAFT"))
        .build();

converter.convert(source).to(target).with(options).execute();
```

The options apply to that conversion only. With a target that is not a PDF document, `execute()` throws an
`IllegalArgumentException`. When the target is an `OutputStream`, give the format first:
`.to(outputStream).as(DefaultDocumentFormatRegistry.PDF).with(options)`.

## Presets

| Preset                    | What it sets                                                         | Requires             |
|---------------------------|----------------------------------------------------------------------|----------------------|
| `PdfOptions.archive()`    | PDF/A-2b, tagged PDF, bookmarks, images compressed without loss       | LibreOffice 6.3+     |
| `PdfOptions.accessible()` | PDF/UA, tagged PDF, bookmarks                                         | LibreOffice 7.0+     |
| `PdfOptions.compact()`    | Tagged PDF, images compressed as JPEG with a quality of 75 and reduced to 150 DPI | any version |

A preset is a starting point: `PdfOptions.archive().toBuilder().pages(pages -> pages.range("1-3")).build()`.

## What you need to know

!!! warning "Options that are not set"

    Without any option, the office installation exports with the PDF settings of its configuration, the ones of its
    PDF export dialog. As soon as **one** option is set, it no longer reads its configuration: every option that is
    not set takes the default value built into the export filter, which is not always the same.

    With LibreOffice 25.2 for example, a plain conversion produces a tagged PDF that opens with the bookmarks pane,
    and a conversion with only a page range produces a PDF that is not tagged and opens without that pane.
    According to the source code of the filter, the comments are then exported as PDF annotations as well.

    Set the options that matter for your documents explicitly; `tagged(true)` is the usual one.

- **Only the options you set are sent.** Empty options (`PdfOptions.builder().build()`) change nothing.
- **Office versions.** An office installation silently ignores an option it does not know. The tables below give the
  first version that supports each option, and the local converter logs a warning for each option the running office
  is too old for. "all" means every LibreOffice version and Apache OpenOffice.
- **Invalid combinations.** `build()` throws an `IllegalArgumentException` for options that cannot work together: a
  password with a PDF/A version (the office would silently produce a document that is not encrypted), a permission
  without a permission password, PDF/UA with `tagged(false)`, or signature details without a certificate.
- **Precedence.** The options are merged into the `FilterData` set on the target format and on the converter, and
  win when both set the same property.
- **Remote conversions.** The options are sent as request parameters, like the other store properties. Only servers
  that support custom properties, such as the JODConverter sample REST service, use them; LibreOffice Online and
  Collabora ignore them. Passwords are then part of the request URL, so prefer a local conversion for them.

## Options

### General

| Method                       | FilterData            | Requires         |
|------------------------------|-----------------------|------------------|
| `version(PdfVersion)`        | `SelectPdfVersion`    | see below        |
| `pdfUa(boolean)`             | `PDFUACompliance`     | LibreOffice 7.0+ |
| `tagged(boolean)`            | `UseTaggedPDF`        | all              |
| `embedSourceDocument(boolean)` | `IsAddStream`       | all              |
| `referenceXObjects(boolean)` | `UseReferenceXObject` | LibreOffice 5.4+ |

| `PdfVersion` | Requires                                                                                              |
|--------------|-------------------------------------------------------------------------------------------------------|
| `DEFAULT`    | all. PDF 1.7 since LibreOffice 7.6, 1.6 from 7.0 to 7.5, 1.5 from 6.1 to 6.4, 1.4 before and in OpenOffice |
| `PDF_1_5`    | LibreOffice 7.0+                                                                                      |
| `PDF_1_6`    | LibreOffice 6.2+                                                                                      |
| `PDF_1_7`    | LibreOffice 7.5+                                                                                      |
| `PDF_2_0`    | LibreOffice 25.2+                                                                                     |
| `PDF_A_1B`   | all                                                                                                   |
| `PDF_A_2B`   | LibreOffice 6.3+                                                                                      |
| `PDF_A_3B`   | LibreOffice 7.0+                                                                                      |
| `PDF_A_4`    | LibreOffice 25.2+                                                                                     |

An office installation that does not know a version produces its default PDF version instead.

### Images: `images(...)`

| Method                      | FilterData                                    | Requires |
|-----------------------------|-----------------------------------------------|----------|
| `lossless(boolean)`         | `UseLosslessCompression`                      | all      |
| `jpegQuality(int)`          | `Quality`, from 1 to 100                      | all      |
| `reduceResolution(boolean)` | `ReduceImageResolution`                       | all      |
| `maxResolution(int)`        | `MaxImageResolution`, in DPI; also sets `ReduceImageResolution` | all |

### Pages: `pages(...)`

| Method                    | FilterData             | Requires          |
|---------------------------|------------------------|-------------------|
| `range(String)`           | `PageRange`, such as `"1-3;7"` | all       |
| `skipEmptyPages(boolean)` | `IsSkipEmptyPages`     | all               |
| `placeholders(boolean)`   | `ExportPlaceholders`   | LibreOffice 5.1+  |
| `trackedChanges(boolean)` | `ExportTrackedChanges` | LibreOffice 26.2+ |

### Comments: `comments(...)`

| Method                      | FilterData            | Requires         |
|-----------------------------|-----------------------|------------------|
| `asPdfAnnotations(boolean)` | `ExportNotes`         | all              |
| `inMargin(boolean)`         | `ExportNotesInMargin` | LibreOffice 7.5+ |

### Bookmarks: `bookmarks(...)`

| Method                         | FilterData                        | Requires |
|--------------------------------|-----------------------------------|----------|
| `export(boolean)`              | `ExportBookmarks`                 | all      |
| `openLevels(int)`              | `OpenBookmarkLevels`, -1 for all  | all      |
| `asNamedDestinations(boolean)` | `ExportBookmarksToPDFDestination` | all      |

### Forms: `forms(...)`

| Method                         | FilterData                                  | Requires |
|--------------------------------|---------------------------------------------|----------|
| `export(boolean)`              | `ExportFormFields`                          | all      |
| `submitFormat(SubmitFormat)`   | `FormsType`: `FDF`, `PDF`, `HTML` or `XML`  | all      |
| `allowDuplicateNames(boolean)` | `AllowDuplicateFieldNames`                  | all      |

### Links: `links(...)`

| Method                            | FilterData                                              | Requires |
|-----------------------------------|---------------------------------------------------------|----------|
| `relativeFileLinks(boolean)`      | `ExportLinksRelativeFsys`                               | all      |
| `convertOdfTargetsToPdf(boolean)` | `ConvertOOoTargetToPDFTarget`                           | all      |
| `crossDocumentLinks(LinkTarget)`  | `PDFViewSelection`: `DEFAULT`, `PDF_READER` or `BROWSER` | all      |

### Initial view: `initialView(...)`

| Method                         | FilterData                                                              | Requires |
|--------------------------------|-------------------------------------------------------------------------|----------|
| `pane(Pane)`                   | `InitialView`: `NONE`, `BOOKMARKS` or `THUMBNAILS`                      | all      |
| `page(int)`                    | `InitialPage`                                                           | all      |
| `magnification(Magnification)` | `Magnification`: `DEFAULT`, `FIT_PAGE`, `FIT_WIDTH` or `FIT_VISIBLE`    | all      |
| `zoom(int)`                    | `Zoom`, in percent; also sets `Magnification`                           | all      |
| `layout(PageLayout)`           | `PageLayout`: `DEFAULT`, `SINGLE_PAGE`, `CONTINUOUS` or `CONTINUOUS_FACING` | all  |

### Viewer window: `viewer(...)`

| Method                          | FilterData                  | Requires |
|---------------------------------|-----------------------------|----------|
| `resizeToInitialPage(boolean)`  | `ResizeWindowToInitialPage` | all      |
| `centerWindow(boolean)`         | `CenterWindow`              | all      |
| `fullScreen(boolean)`           | `OpenInFullScreenMode`      | all      |
| `displayDocumentTitle(boolean)` | `DisplayPDFDocumentTitle`   | all      |
| `hideMenubar(boolean)`          | `HideViewerMenubar`         | all      |
| `hideToolbar(boolean)`          | `HideViewerToolbar`         | all      |
| `hideWindowControls(boolean)`   | `HideViewerWindowControls`  | all      |

### Security: `security(...)`

| Method                         | FilterData                                                    | Requires |
|--------------------------------|---------------------------------------------------------------|----------|
| `openPassword(String)`         | `EncryptFile` and `DocumentOpenPassword`                      | all      |
| `permissionPassword(String)`   | `RestrictPermissions` and `PermissionPassword`                | all      |
| `printing(Printing)`           | `Printing`: `NONE`, `LOW_RESOLUTION` or `HIGH_RESOLUTION`     | all      |
| `changes(Changes)`             | `Changes`: `NONE`, `PAGES`, `FORMS`, `FORMS_AND_COMMENTS` or `ALL_EXCEPT_EXTRACTION` | all |
| `copying(boolean)`             | `EnableCopyingOfContent`                                      | all      |
| `accessibilityAccess(boolean)` | `EnableTextAccessForAccessibilityTools`                       | all      |

The permissions (the last four options) require a permission password. PDF/A does not allow encryption.

```java
PdfOptions.builder()
    .security(security -> security
        .openPassword("to-open")
        .permissionPassword("to-change-permissions")
        .printing(Printing.LOW_RESOLUTION)
        .copying(false))
    .build();
```

### Watermark: `watermark(...)`

| Method               | FilterData                                    | Requires         |
|----------------------|-----------------------------------------------|------------------|
| `text(String)`       | `Watermark`                                   | all              |
| `tiledText(String)`  | `TiledWatermark`                              | LibreOffice 6.3+ |
| `color(int)`         | `WatermarkColor`, an RGB value such as `0xFF0000` | LibreOffice 7.4+ |
| `fontName(String)`   | `WatermarkFontName`                           | LibreOffice 7.4+ |
| `fontHeight(int)`    | `WatermarkFontHeight`, in points              | LibreOffice 7.4+ |
| `rotation(int)`      | `WatermarkRotateAngle`, given in degrees      | LibreOffice 7.4+ |

### Digital signature: `signature(...)`

| Method                           | FilterData                                         | Requires          |
|----------------------------------|----------------------------------------------------|-------------------|
| `certificateSubjectName(String)` | `SignPDF` and `SignCertificateSubjectName`         | LibreOffice 7.4+  |
| `certificatePem(String, String)` | `SignPDF`, `SignCertificateCertPem` and `SignCertificateKeyPem` | LibreOffice 25.2+ |
| `caPem(String)`                  | `SignCertificateCaPem`                             | LibreOffice 25.2+ |
| `password(String)`               | `SignaturePassword`                                | LibreOffice 4.0+  |
| `location(String)`               | `SignatureLocation`                                | LibreOffice 4.0+  |
| `reason(String)`                 | `SignatureReason`                                  | LibreOffice 4.0+  |
| `contactInfo(String)`            | `SignatureContactInfo`                             | LibreOffice 4.0+  |
| `timestampAuthority(String)`     | `SignatureTSA`                                     | LibreOffice 5.0+  |

`certificateSubjectName` takes the certificate, by its subject name such as `"CN=My Company"`, from the certificate
store used by the office installation: the Windows certificate store on Windows, an NSS database on Linux (the
`MOZILLA_CERTIFICATE_FOLDER` environment variable of the office process can point to it). `certificatePem` needs no
certificate store. When the certificate cannot be found or used, the conversion fails with an `OfficeException`.

!!! warning "Limitations of LibreOffice when signing"

    - **On Linux, an office process can only sign if its first PDF export is a signed one** (observed with
      LibreOffice 26.2). Once it has exported a PDF without signature, every signed export fails until the process is
      restarted. Use an office manager dedicated to the signed conversions.
    - **On Windows, a PEM certificate cannot be used** (observed with LibreOffice 25.2 and 26.2): the conversion
      fails. Use `certificateSubjectName` with a certificate of the Windows certificate store.

LibreOffice versions older than 7.4 can only sign with a certificate object given through the `SignatureCertificate`
property; set it with `filterData("SignatureCertificate", certificate)` and `filterData("SignPDF", true)`.

### Presentations (Impress): `presentation(...)`

| Method                    | FilterData             | Requires                          |
|---------------------------|------------------------|-----------------------------------|
| `hiddenSlides(boolean)`   | `ExportHiddenSlides`   | all LibreOffice versions, not OpenOffice |
| `notesPages(boolean)`     | `ExportNotesPages`     | all                               |
| `onlyNotesPages(boolean)` | `ExportOnlyNotesPages` | LibreOffice 5.2+                  |
| `transitions(boolean)`    | `UseTransitionEffects` | all                               |

### Spreadsheets (Calc): `spreadsheet(...)`

| Method                      | FilterData                                  | Requires          |
|-----------------------------|---------------------------------------------|-------------------|
| `singlePageSheets(boolean)` | `SinglePageSheets`                          | LibreOffice 6.4+  |
| `sheetRange(String)`        | `SheetRange`, sheet positions such as `"1-3"` | LibreOffice 24.8+ |

LibreOffice ignores `sheetRange` when `singlePageSheets` is set.

### Any other property

`filterData(name, value)` sets any `FilterData` property that has no dedicated method. These properties are applied
last and are not validated.

```java
PdfOptions.builder().filterData("ExportNotesInMargin", true).build();
```

## How the options were tested

Each option was converted with a real office installation, and the PDF document it produced was inspected (document
catalog, encryption permissions, annotations, form fields, images, page content, signature) to check that the option
had its effect:

- **LibreOffice 25.2.7 and 26.2.6 on Windows:** every option, except the digital signature.
- **LibreOffice 26.2.6 on Linux:** the digital signature, by subject name and with a PEM certificate, with its
  location, reason and contact information.
- **Not tested:** the `password` and `timestampAuthority` options of the signature, the effect of `caPem`, and the
  signature by subject name on Windows. No option was tested with Apache OpenOffice or with LibreOffice versions
  older than 25.2.

One option of the export filter is not offered: `FirstPageOnLeft`. Every version of LibreOffice and OpenOffice reads
it and then discards it, so it never has any effect.

## How the versions were established

The "Requires" columns come from the source code of the PDF export filter (`filter/source/pdf/pdfexport.cxx`) of
every LibreOffice release branch from 3.5 to 26.8, and of Apache OpenOffice 4.1: an option requires the first version
whose filter reads it. This tells when an option started to exist, not how well it worked at the time.
