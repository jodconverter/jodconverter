# Slides to Images

A presentation or a drawing can be exported as one image per slide or draw page, with the `LocalConverter`:

```java
List<File> images =
    LocalConverter.make()
        .exportPages(new File("deck.pptx"))
        .to(new File("out"))
        .execute();
```

The images are written in the directory, named after the source document and the number of the page in the
document: `deck-1.png`, `deck-2.png`... (the numbers are padded to the width of the page count, `deck-01.png` for a
deck of ten slides or more, so that the files sort). The list returned holds the files, in the order of the pages.
Only presentations (Impress) and drawings (Draw) can be exported this way; a text document or a spreadsheet fails
with an `OfficeException`.

The export uses the graphic export filter of the office with a single page as the source, so it works with every
LibreOffice version, including 24.2 and later, where the HTML export wizard that used to produce one image per slide
is gone.

## Options

| Method                | Effect                                                                                                                                     |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `to(File)`            | The directory of the images, created if missing. Required.                                                                                 |
| `as(ImageFormat)`     | `ImageFormat.PNG` (default), `JPEG`, `SVG`, `GIF`, `BMP`, `TIFF`, `WEBP` (LibreOffice 7.4+), or `ImageFormat.of("jpg")` from an extension. |
| `size(width, height)` | The size of the images, in pixels. Without a size, the images have the size of the page at 96 dpi.                                        |
| `width(int)`          | The width in pixels; the height follows the proportions of the page.                                                                       |
| `height(int)`         | The height in pixels; the width follows the proportions of the page.                                                                       |
| `quality(int)`        | The quality of a lossy format (JPEG, WebP), 1 to 100; 90 by default.                                                                       |
| `pages(String)`       | The pages to export, such as `1-3,7`; all of them by default. A page beyond the last one fails the export.                                  |
| `hiddenSlides(bool)`  | Whether the hidden slides of a presentation are exported; they are skipped by default, and their numbers are missing from the sequence.    |
| `baseName(String)`    | The start of the file names; the name of the source file by default, or `page` for a document given as a stream.                           |
| `executeAsync()`      | Exports without waiting: a `CompletableFuture` of the list of files.                                                                        |

```java
List<File> thumbnails =
    LocalConverter.make()
        .exportPages(new File("deck.pptx"))
        .to(new File("thumbnails"))
        .as(ImageFormat.JPEG)
        .width(320)
        .quality(80)
        .pages("1-10")
        .execute();
```

The filters of the converter are applied to the document before the export, like for a conversion; the load
properties of the converter are used to load the document.

## SVG

The SVG of a slide is its vector drawing, with the text as text (1 to 30 KB for a usual slide). It is different from
the SVG produced by a conversion of the whole presentation (`converter.convert(deck).to(deck.svg)`), which embeds the
fonts and is many times larger.

## A note on LibreOffice 26.2 on Windows

With an unpacked (not installed) LibreOffice 26.2 on Windows, the office process was seen to crash when the document
is closed after such an export; the office manager restarts it, and the images are complete. The crash was not
reproduced on Linux, nor with LibreOffice 25.2 on Windows.
