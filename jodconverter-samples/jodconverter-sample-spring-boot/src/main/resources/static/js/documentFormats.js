var importFormatTable = {
    __proto__: null,

    odt: "Text",
    ott: "Text",
    sxw: "Text",
    html: "Text",
    xhtml: "Text",
    rtf: "Text",
    txt: "Text",
    doc: "Text",
    docx: "Text",
    wpd: "Text",
    ods: "Spreadsheet",
    ots: "Spreadsheet",
    sxc: "Spreadsheet",
    csv: "Spreadsheet",
    tsv: "Spreadsheet",
    xls: "Spreadsheet",
    xlsx: "Spreadsheet",
    odp: "Presentation",
    sxi: "Presentation",
    ppt: "Presentation",
    pptx: "Presentation",
    vsd: "Drawing",
    vsdx: "Drawing"
};

var exportFormatTable = {
    __proto__: null,

    Text: [
        new Option("Hypertext Markup Language (html)", "html"),
        new Option("Extensible Hypertext Markup Language (xhtml)", "xhtml"),
        new Option("Portable Document Format (pdf)", "pdf"),
        new Option("OpenDocument Text (odt)", "odt"),
        new Option("OpenOffice.org 1.0 Text Document (sxw)", "sxw"),
        new Option("Microsoft Word 97-2003 (doc)", "doc"),
        new Option("Microsoft Word 2007-2013 (docx)", "docx"),
        new Option("Rich Text Format (rtf)", "rtf"),
        new Option("Plain Text (txt)", "txt"),
        new Option("Portable Network Graphics (png)", "png"),
    ],

    Spreadsheet: [
        new Option("Portable Document Format (pdf)", "pdf"),
        new Option("OpenDocument Spreadsheet (ods)", "ods"),
        new Option("OpenOffice.org 1.0 Spreadsheet (sxc)", "sxc"),
        new Option("Microsoft Excel 97-2003 (xls)", "xls"),
        new Option("Microsoft Excel 2007-2013 (xlsx)", "xlsx"),
        new Option("Comma-Separated Values (csv)", "csv"),
        new Option("Portable Network Graphics (png)", "png"),
    ],

    Presentation: [
        new Option("Portable Document Format (pdf)", "pdf"),
        new Option("Macromedia Flash (swf)", "swf"),
        new Option("OpenDocument Presentation (odp)", "odp"),
        new Option("OpenOffice.org 1.0 Presentation (sxi)", "sxi"),
        new Option("Microsoft PowerPoint 97-2003 (ppt)", "ppt"),
        new Option("Microsoft PowerPoint 2007-2013 (pptx)", "pptx"),
        new Option("Portable Network Graphics (png)", "png"),
    ],

    Drawing: [
        new Option("Portable Document Format (pdf)", "pdf")
	]
};
