{
  "documentFormats":
  [
    {
      "name": "PDF",
      "extension": "pdf",
      "mediaType": "application/pdf",
      "storePropertiesByFamily": {
        "TEXT": { "FilterName": "writer_pdf_Export" },
        "SPREADSHEET": { "FilterName": "calc_pdf_Export" },
        "PRESENTATION": { "FilterName": "impress_pdf_Export" },
        "DRAWING": { "FilterName": "draw_pdf_Export" }
      }
    },
    {
      "name": "Flash",
      "extension": "swf",
      "mediaType": "application/x-shockwave-flash",
      "storePropertiesByFamily": {
        "PRESENTATION": { "FilterName": "impress_flash_Export" },
        "DRAWING": { "FilterName": "draw_flash_Export" }
      }
    },
    {
      "name": "HTML",
      "extension": "html",
      "mediaType": "text/html",
      "inputFamily": "TEXT",
      "storePropertiesByFamily": {
      	"TEXT": { "FilterName": "HTML (StarWriter)" },
        "SPREADSHEET": { "FilterName": "HTML (StarCalc)" },
        "PRESENTATION": { "FilterName": "impress_html_Export" }
      }
    },
    {
      "name": "OpenDocument Text",
      "extension": "odt",
      "mediaType": "application/vnd.oasis.opendocument.text",
      "inputFamily": "TEXT",
      "storePropertiesByFamily": {
      	"TEXT": { "FilterName": "writer8" }
      }
    },
    {
      "name": "OpenOffice.org 1.0 Text Document",
      "extension": "sxw",
      "mediaType": "application/vnd.sun.xml.writer",
      "inputFamily": "TEXT",
      "storePropertiesByFamily": {
      	"TEXT": { "FilterName": "StarOffice XML (Writer)" }
      }
    },
    {
      "name": "Microsoft Word",
      "extension": "doc",
      "mediaType": "application/msword",
      "inputFamily": "TEXT",
      "storePropertiesByFamily": {
      	"TEXT": { "FilterName": "MS Word 97" }
      }
    },
    {
      "name": "Rich Text Format",
      "extension": "rtf",
      "mediaType": "text/rtf",
      "inputFamily": "TEXT",
      "storePropertiesByFamily": {
        "TEXT": { "FilterName": "Rich Text Format" }
      }
    },
    {
      "name": "Wordperfect",
      "extension": "wpd",
      "mediaType": "application/wordperfect",
      "inputFamily": "TEXT"
    },
    {
      "name": "Plain Text",
      "extension": "txt",
      "mediaType": "text/plain",
      "inputFamily": "TEXT",
      "loadProperties" {
        "FilterName": "Text (encoded)",
        "FilterOptions": "utf8"
      }
      "storePropertiesByFamily": {
        "TEXT": {
          "FilterName": "Text (encoded)",
          "FilterOptions": "utf8"
        }
      }
    },
    {
      "name": "MediaWiki wikitext",
      "extension": "wiki",
      "mediaType": "text/x-wiki",
      "storePropertiesByFamily": {
        "TEXT": { "FilterName": "MediaWiki" }
      }
    },
    {
      "name": "OpenDocument Spreadsheet",
      "extension": "ods",
      "mediaType": "application/vnd.oasis.opendocument.spreadsheet",
      "inputFamily": "SPREADSHEET",
      "storePropertiesByFamily": {
        "SPREADSHEET": { "FilterName": "calc8" }
      }
    },
    {
      "name": "OpenOffice.org 1.0 Spreadsheet",
      "extension": "sxc",
      "mediaType": "application/vnd.sun.xml.calc",
      "inputFamily": "SPREADSHEET",
      "storePropertiesByFamily": {
        "SPREADSHEET": { "FilterName": "StarOffice XML (Calc)" }
      }
    },
    {
      "name": "Microsoft Excel",
      "extension": "xls",
      "mediaType": "application/vnd.ms-excel",
      "inputFamily": "SPREADSHEET",
      "storePropertiesByFamily": {
        "SPREADSHEET": { "FilterName": "MS Excel 97" }
      }
    },
    {
      "name": "Comma Separated Values",
      "extension": "csv",
      "mediaType": "text/csv",
      "inputFamily": "SPREADSHEET",
      "loadProperties": {
        "FilterName": "Text - txt - csv (StarCalc)",
        "FilterOptions": "44,34,0"
      },
      "storePropertiesByFamily": {
        "SPREADSHEET": {
          "FilterName": "Text - txt - csv (StarCalc)",
          "FilterOptions": "44,34,0"
        }
      }
    },
    {
      "name": "Tab Separated Values",
      "extension": "tsv",
      "mediaType": "text/tab-separated-values",
      "inputFamily": "SPREADSHEET",
      "loadProperties": {
        "FilterName": "Text - txt - csv (StarCalc)",
        "FilterOptions": "9,34,0"
      },
      "storePropertiesByFamily": {
        "SPREADSHEET": {
          "FilterName": "Text - txt - csv (StarCalc)",
          "FilterOptions": "9,34,0"
        }
      }
    },
    {
      "name": "OpenDocument Presentation",
      "extension": "xls",
      "mediaType": "application/vnd.ms-excel",
      "inputFamily": "SPREADSHEET",
      "storePropertiesByFamily": {
        "SPREADSHEET": { "FilterName": "MS Excel 97" }
      }
    },
  ]
}
