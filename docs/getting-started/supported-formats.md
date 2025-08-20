# Supported Formats

**JODConverter** automates conversions that are actually performed by OOo. This means that if you can convert from
format ABC to format XYZ from OOo (by opening ABC and saving/exporting to XYZ) then you can do the same with
**JODConverter**. You have to discover the magic string used by OOo as the export filter name. A useful list can be
found in the [OpenOffice Wiki](https://wiki.openoffice.org/wiki/Framework/Article/Filter/FilterList_OOo_3_0) or
the [LibreOffice documentation](https://help.libreoffice.org/latest/en-US/text/shared/guide/convertfilters.html).

That said, **JODConverter** maintains a registry of the most common formats, their associated file extensions, mime
types, and OOo filter names to simplify your life. These predefined conversions are shown in the following table **(if a
format is not there, it doesn't mean that the format is not supported. The same is also true if the format is there and
the conversion does not work. All conversions supported by your OOo installation are supported by JODConverter)**:

<table aligh="center">
    <tr><th align="left">Format Family</th><th align="left">From (any of)</th><th align="left">To (any of)</th></tr>
    <tr valign="top">
        <th align="left">Text</th>
        <td>
            <samp><b>*.odt</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Text<br>
            <samp><b>*.ott</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Text Template<br>
            <samp><b>*.sxw</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenOffice.org 1.0 Text<br>
            <samp><b>*.rtf</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Rich Text Format<br>
            <samp><b>*.doc</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Microsoft Word<br>
            <samp><b>*.docx</b>&nbsp;&nbsp;&nbsp;</samp>Microsoft Word XML<br>
            <samp><b>*.wpd</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>WordPerfect<br>
            <samp><b>*.txt</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Plain Text<br>
            <samp><b>*.html</b>&nbsp;&nbsp;&nbsp;</samp>HTML<sup>1</sup>
        </td>
        <td>
            <samp><b>*.pdf</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Portable Document Format<br>
            <samp><b>*.odt</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Text<br>
            <samp><b>*.ott</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Text Template<br>
            <samp><b>*.sxw</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenOffice.org 1.0 Text<br>
            <samp><b>*.rtf</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Rich Text Format<br>
            <samp><b>*.doc</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Microsoft Word<br>
            <samp><b>*.docx</b>&nbsp;&nbsp;&nbsp;</samp>Microsoft Word XML<br>
            <samp><b>*.txt</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Plain Text<br>
            <samp><b>*.html</b>&nbsp;&nbsp;&nbsp;</samp>HTML<sup>2</sup><br>
            <samp><b>*.wiki</b>&nbsp;&nbsp;&nbsp;</samp>MediaWiki wikitext
        </td>
    </tr>
    <tr valign="top">
        <th align="left">Spreadsheet</th>
        <td>
            <samp><b>*.ods</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Spreadsheet<br>
            <samp><b>*.ots</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Spreadsheet Template<br>
            <samp><b>*.sxc</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenOffice.org 1.0 Spreadsheet<br>
            <samp><b>*.xls</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Microsoft Excel<br>
            <samp><b>*.xlsx</b>&nbsp;&nbsp;&nbsp;</samp>Microsoft Excel XML<br>
            <samp><b>*.csv</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Comma-Separated Values<br>
            <samp><b>*.tsv</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Tab-Separated Values
        </td>
        <td>
            <samp><b>*.pdf</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Portable Document Format<br>
            <samp><b>*.ods</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Spreadsheet<br>
            <samp><b>*.ots</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Spreadsheet Template<br>
            <samp><b>*.sxc</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenOffice.org 1.0 Spreadsheet<br>
            <samp><b>*.xls</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Microsoft Excel<br>
            <samp><b>*.xlsx</b>&nbsp;&nbsp;&nbsp;</samp>Microsoft Excel XML<br>
            <samp><b>*.csv</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Comma-Separated Values<br>
            <samp><b>*.tsv</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Tab-Separated Values<br>
            <samp><b>*.html</b>&nbsp;&nbsp;&nbsp;</samp>HTML<sup>2</sup>
        </td>
    </tr>
    <tr valign="top">
        <th align="left">Presentation</th>
        <td>
            <samp><b>*.odp</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Presentation<br>
            <samp><b>*.otp</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Presentation Template<br>
            <samp><b>*.sxi</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenOffice.org 1.0 Presentation<br>
            <samp><b>*.ppt</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Microsoft PowerPoint<br>
            <samp><b>*.pptx</b>&nbsp;&nbsp;&nbsp;</samp>Microsoft PowerPoint XML
        </td>
        <td>
            <samp><b>*.pdf</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Portable Document Format<br>
            <samp><b>*.swf</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Macromedia Flash<br>
            <samp><b>*.odp</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Presentation<br>
            <samp><b>*.otp</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Presentation Template<br>
            <samp><b>*.sxi</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenOffice.org 1.0 Presentation<br>
            <samp><b>*.ppt</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Microsoft PowerPoint<br>
            <samp><b>*.pptx</b>&nbsp;&nbsp;&nbsp;</samp>Microsoft PowerPoint XML<br>
            <samp><b>*.html</b>&nbsp;&nbsp;&nbsp;</samp>HTML<sup>2</sup>
        </td>
    </tr>
    <tr valign="top">
        <th align="left">Drawing</th>
        <td>
            <samp><b>*.odg</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Drawing<br>
            <samp><b>*.otg</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>OpenDocument Drawing Template
        </td>
        <td>
            <samp><b>*.svg</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Scalable Vector Graphics<br>
            <samp><b>*.swf</b>&nbsp;&nbsp;&nbsp;&nbsp;</samp>Macromedia Flash
        </td>
    </tr>
</table>
  
&nbsp;
> 1. HTML can be used as an input format but you should not expect OOo to properly render complex web pages as Chrome or
     IE do. Works reasonably well for simple and "printer friendly" web pages only.
> 2. HTML can be used as an output format but while all other formats always generate a single output file, HTML can
     produce multiple files. In addition to the HTML file in fact, any images contained in the input document will also
     be saved in the same directory. This requires extra care in your code, especially in a web environment.

--8<-- "note.md"
