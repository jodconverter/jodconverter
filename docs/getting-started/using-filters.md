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

final PageSelectorFilter selectorFilter = new PageSelectorFilter(2);

LocalConverter
  .builder()
  .filterChain(selectorFilter)
  .build()
  .convert(inputFile)
  .to(outputFile)
  .execute();
```

Note that you can use more than one filter per conversion. Also, such a filter (page selector) is only required when the
target format is not PDF. Indeed, when converting to PDF, you are better off using custom store properties:

```java
File inputFile = new File("document.rtf");
File outputFile = new File("document.pdf");

Map<String, Object> filterData = new HashMap<>();
filterData.put("PageRange", "2");
Map<String, Object> customProperties = new HashMap<>();
customProperties.put("FilterData", filterData);

LocalConverter
  .builder()
  .storeProperties(customProperties)
  .build()
  .convert(inputFile)
  .to(outputFile)
  .execute();
```

**JODConverter** provides
some [filters](https://github.com/jodconverter/jodconverter/tree/master/jodconverter-local/src/main/java/org/jodconverter/local/filter)
out of the box, but you can implement (and share obviously &#128513;) any filter you need. Your filter must implement
the [Filter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/filter/Filter.java)
interface and is responsible to call the next filter in the filter chain.