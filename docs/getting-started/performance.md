# Performance

Conversion performance varies widely depending on the input document size and complexity, input and output formats etc.
Since conversions are done by OOo, raw conversion time is not something that can be improved by **JODConverter**.

Just to give an idea, the following table shows some benchmarks for converting different ODT documents to PDF:

| Document                                                                                                                                                     | Size   | Pages | Avg Time (ms) | Throughput (per minute) |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|-------|---------------|-------------------------|
| Hello World!                                                                                                                                                 | 7 kb   | 1 p   | 98 ms         | 612 p/m                 |
| [Metadata Use Cases and Requirements](http://www.oasis-open.org/committees/download.php/20492/UCR.odt)                                                       | 13 kb  | 5 p   | 710 ms        | 422 p/m                 |
| [Open Document Format v1.1 Accessibility Guidelines](http://docs.oasis-open.org/office/office-accessibility/v1.0/cd01/ODF_Accessibility_Guidelines-v1.0.odt) | 81 kb  | 52 p  | 2314 ms       | 1348 p/m                |
| [OpenDocument v1.1 Specification](http://docs.oasis-open.org/office/v1.1/OS/OpenDocument-v1.1.odt)                                                           | 475 kb | 737 p | 24084 ms      | 1836 p/m                |

> Tests made with jodconverter-cli and Apache OpenOffice 4.1.3 on a laptop with a quad-core Intel(R) Core(TM) i7-6500U
> CPU @ 2.50GHz processor and Windows 10

In general, conversions to/from OpenDocument take less time than equivalent conversions involving other formats since
OpenDocument is the native OOo format. (E.g., ODT to PDF is faster than DOC to PDF.) Other factors affecting performance
include the presence of graphics, charts, and other objects in the document, macros etc.

The bottom line is that you should make some benchmarks of your own - with documents of the types and complexities
required by your application - to understand if the performance is adequate for your purposes, or if you need to throw
more hardware at the problem.