# Using Filters

When converting a document, **JODConverter** allows you to modify the loaded source document before it is stored into
the target format. The source document itself will never be modified, only the loaded one will. What you can do as
modifications is only limit by what you can do with OOo. Processing conversion using JODConverter is the same as opening
the document yourself with OOo, apply your modifications, whatever they are, and then use the File > Save As menu item
to save your document as the desired format (pdf, txt, docx, etc.).

Suppose you want to export only the second page of a RTF document as HTML. Using OOo, the faster way would be to select
the second page, copy it (Ctrl+C), then select the whole document (Ctrl+A), and paste the previously copied page (Ctrl +
V). Finally, you would use the File >Save As menu item to export your modified document as HTML.

This is exactly what
the [PagesSelectorFilter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/filter/PagesSelectorFilter.java)
is doing. The following example will convert only the second page of a given source document:

```java
final File inputFile = new File("document.rtf");
final File outputFile = new File("document.html");

final PagesSelectorFilter selectorFilter = new PagesSelectorFilter(2);

LocalConverter
  .builder()
  .filterChain(selectorFilter)
  .build()
  .convert(inputFile)
  .to(outputFile)
  .execute();
```

Note that you can use more than one filter per conversion. Also, such a filter (page selector) is only required when the
target format is not PDF. Indeed, when converting to PDF, you are better off using the
[PDF options](pdf-options.md):

```java
File inputFile = new File("document.rtf");
File outputFile = new File("document.pdf");

LocalConverter
  .make()
  .convert(inputFile)
  .to(outputFile)
  .with(PdfOptions.builder().pages(pages -> pages.range("2")).build())
  .execute();
```

## Merging documents

Text documents are merged with `merge(...)`: the first document is loaded, the others are inserted at its end, each
one starting on a new page, and the result is converted like any document, with the target format and the options of
a conversion:

```java
LocalConverter
  .make()
  .merge(new File("chapter1.docx"), new File("chapter2.docx"), new File("chapter3.docx"))
  .to(new File("book.pdf"))
  .with(PdfOptions.archive())
  .execute();
```

The page styles, headers and footers of the result are those of the first document. The filters of the converter are
applied after the insertions, so a `DocumentIndexesUpdaterFilter` rebuilds the table of contents of the merged
document.

Underneath, `merge` chains a
[DocumentInserterFilter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/filter/text/DocumentInserterFilter.java)
per document. Use the filter directly to insert a document without a page break (`new
DocumentInserterFilter(file, false)`, the default of the filter), or at a chosen place in the chain:

```java
LocalConverter
  .builder()
  .filterChain(
      new DocumentInserterFilter(new File("chapter2.docx")),
      new DocumentInserterFilter(new File("chapter3.docx")))
  .build()
  .convert(new File("chapter1.docx"))
  .to(new File("merged.pdf"))
  .execute();
```

## Updating the indexes of a document

A text document is saved with its table of contents, alphabetical index, table of figures or bibliography as they were
when they were last updated by someone. An export shows them that way: a heading added since is missing, and the page
numbers are those of that time. The
[DocumentIndexesUpdaterFilter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/filter/text/DocumentIndexesUpdaterFilter.java)
updates every index of the document before it is stored, in two passes: the first one rebuilds the indexes, which can
change the pagination (a table of contents that grows by a page), and the second one fixes the page numbers for the new
layout. It can also change the number of levels of the tables of contents.

```java
LocalConverter
  .builder()
  .filterChain(new DocumentIndexesUpdaterFilter())
  .build()
  .convert(new File("report.docx"))
  .to(new File("report.pdf"))
  .execute();
```

The filter is not applied by default: rebuilding an index replaces its entries, so a document whose index was edited by
hand, or that holds links to the entries of its index, is better exported as it is.

## Available filters

**JODConverter** provides these filters out of the box, in the
[filter](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local/src/main/java/org/jodconverter/local/filter)
package of the local module:

| Filter                         | What it does                                                                                                                                |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `RefreshFilter`                | Refreshes the document (fields, layout). Applied by default when no filter chain is given to the converter.                                 |
| `PagesSelectorFilter`          | Keeps only the given pages (text documents), sheets (spreadsheets) or slides (presentations and drawings).                                  |
| `PageCounterFilter`            | Counts the pages, sheets or slides of the document, available after the conversion with `getPageCount()`.                                   |
| `text.DocumentIndexesUpdaterFilter` | Updates the indexes of a text document, and optionally the number of levels of its tables of contents. Replaces `TableOfContentUpdaterFilter`. |
| `text.PageMarginsFilter`       | Changes the page margins of a text document.                                                                                                |
| `text.TextInserterFilter`      | Inserts a text, in a frame placed at the given position and size.                                                                           |
| `text.TextReplacerFilter`      | Replaces texts in a text document.                                                                                                          |
| `text.GraphicInserterFilter`   | Inserts an image, at the given position and size.                                                                                           |
| `text.DocumentInserterFilter`  | Inserts another document at the end of the loaded one, on a new page or not (see [Merging documents](#merging-documents)).                 |
| `text.LinkedImagesEmbedderFilter` | Embeds the linked images of a text document, so that the output does not depend on them.                                                  |

You can implement (and share obviously 😁) any filter you need. Your filter must implement
the [Filter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/filter/Filter.java)
interface and is responsible for calling the next filter in the filter chain.

--8<-- "note.md"
