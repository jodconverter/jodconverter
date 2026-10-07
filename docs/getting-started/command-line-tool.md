# Command Line Tool

The `jodconverter-cli` module provides a standalone command-line tool for converting documents using LibreOffice or
OpenOffice. It enables quick and easy file conversions without writing any Java code, making it ideal for automation,
scripting, and server-side integrations.

The command line tool provides a good way to check that everything is working, i.e., that you have the right OOo
version installed etc. To convert a document, use the provided batch file, located in the bin directory of the cli
module distribution.

### Syntax

```
jodconverter-cli [options] infile outfile [infile outfile ...]
```

or

```
jodconverter-cli [options] -f output-format infile [infile ...]
```

### Parameters

#### infile

The input file to convert (required). When used with the **-f** switch, infile may contain wildcards to match multiple
files to convert. Thus, it is possible with the jodconverter-cli tool to convert more than 1 file at the time.

#### outfile

The target file which is the result of the conversion.

#### -c, --connection-url `<url>`

Remote LibreOffice Online server URL for conversion (optional).

#### -d, --output-directory `<dir>`

Output directory (optional; defaults to input directory).

#### -f, --output-format `<arg>`

Output format (e.g. pdf).

#### -h, --help

Displays help at the command prompt.

#### -i, --office-home `<dir>`

OOo home directory (optional; defaults to auto-detect).
See [Configuration](../../configuration/local-configuration#officehome).

#### -k, --keep-alive

Keep the office process alive on shutdown (optional; defaults to false).
See [Configuration](../../configuration/local-configuration#keepaliveonshutdown).

#### -l, --load-properties

Load properties (optional; eg. -lPassword=myPassword).

#### -m, --process-manager `<classname>`

Class name of the process manager to use (optional; defaults to auto-detect).
See [Configuration](../../configuration/local-configuration#processmanager).

#### -h, --host-name `<arg>`

Host name that will be used in the --accept argument when starting a process.
See [Configuration](../../configuration/local-configuration#hostname).

#### -o, --overwrite

Overwrite existing output file (optional; defaults to false).

#### -p, --port `<arg>`

Office socket port (optional; defaults to 2002).
See [Configuration](../../configuration/local-configuration#portnumbers-pipenames).

#### -r, --registry `<file>`

Document formats registry configuration file (optional).

#### -t, --timeout `<arg>`

Maximum conversion time in seconds (optional; defaults to 120).
See [Configuration](../../configuration/local-configuration#taskexecutiontimeout).

#### -u, --user-profile `<dir>`

Use settings from the given OOo user installation directory (optional).
See [Configuration](../../configuration/local-configuration#templateprofiledir).

#### -v, --version

Displays version information and exit.

#### -w, --working-dir `<dir>`

Directory where temporary office profile directories will be created (optional; defaults to java.io.tmpdir).
See [Configuration](../../configuration/local-configuration#workingdir).

#### --config `<file>`

Configuration file, JSON or YAML (optional): the [filters](#filters) applied to the documents of a local conversion,
and the [SSL options](#ssl-options) of a remote conversion. See [Configuration file](#configuration-file).

#### --pdf-preset `<name>`

PDF options to start from, for the PDF outputs (optional): `archive` (PDF/A-2b), `accessible` (PDF/UA) or `compact`
(reduced JPEG images). See [PDF Options](pdf-options.md#presets).

#### --pdf-option `<name=value>`

Option applied to the PDF outputs (optional). It can be repeated, and is applied on top of the preset if there is
one:

```
jodconverter-cli --pdf-preset archive --pdf-option pages.range=1-3 --pdf-option watermark.text=DRAFT in.docx out.pdf
```

See [PDF Options](pdf-options.md#command-line) for the names and the values. The other outputs of the same command
are converted without these options.

### Configuration file

The `--config` option reads a file that holds what the other options cannot express: the filters applied to a loaded
document before it is saved, and the SSL options of the connection to a LibreOffice Online server. The file is YAML
when its name ends with `.yml` or `.yaml`, and JSON otherwise. Both sections are optional.

#### Filters

The `filters` section lists the [filters](using-filters.md) applied to the loaded document, in order, before it is
saved to the desired format. Each entry names a built-in filter with `type`, followed by the keys of that filter, or a
custom filter with `class`. Here is a configuration that inserts a text into the document, then inserts a graphic, and
finally replaces some text strings:

```yaml title="filters.yml"
filters:
  - type: text-inserter
    text: text to insert
    width: 100               # Width, 10 CM
    height: 10               # Height, 1 CM
    horizontal-position: 50  # Horizontal position, 5 CM
    vertical-position: 100   # Vertical position, 10 CM
  - type: graphic-inserter
    image: /path/to/the/image.jpg
    horizontal-position: 50  # Horizontal position, 5 CM
    vertical-position: 111   # Vertical position, 11.1 CM (just under the text box)
  - type: text-replacer
    replacements:
      text: Text
      to insert: describing the image below
```

```shell
jodconverter-cli --config filters.yml infile outfile
```

The same configuration in JSON:

```json title="filters.json"
{
  "filters": [
    { "type": "text-inserter", "text": "text to insert", "width": 100, "height": 10,
      "horizontal-position": 50, "vertical-position": 100 },
    { "type": "graphic-inserter", "image": "/path/to/the/image.jpg",
      "horizontal-position": 50, "vertical-position": 111 },
    { "type": "text-replacer", "replacements": { "text": "Text", "to insert": "describing the image below" } }
  ]
}
```

The built-in filters and their keys are (the positions and sizes are in millimeters):

| `type`                     | Keys                                                                                                                                                                                                                                                     |
| -------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pages-selector`           | `pages`: a page number, or a list of page numbers; only these pages are converted.                                                                                                                                                                       |
| `text-inserter`            | `text`, `width`, `height`; `horizontal-position` and `vertical-position`, or `shape-properties`, a map of the properties of the created text shape.                                                                                                      |
| `graphic-inserter`         | `image`, the path of the image; `horizontal-position` and `vertical-position`, or `shape-properties`; optionally `width` and `height`, which resize the image.                                                                                           |
| `document-inserter`        | `document`, the path of the document appended to the converted one.                                                                                                                                                                                      |
| `text-replacer`            | `replacements`, a map of the texts to search and their replacements.                                                                                                                                                                                     |
| `page-margins`             | `left`, `top`, `right`, `bottom`, each optional.                                                                                                                                                                                                         |
| `document-indexes-updater` | `level`, the number of levels of the tables of contents, optional; the indexes of a text document (table of contents, alphabetical index, table of figures...) are updated before the document is stored. `table-of-content-updater` is its former name. |
| `linked-images-embedder`   | None; the linked images are embedded in the document.                                                                                                                                                                                                    |
| `refresh`                  | None; the document is refreshed.                                                                                                                                                                                                                         |

A custom filter is a class that implements the
[Filter](https://github.com/jodconverter/jodconverter/blob/master/jodconverter-local/src/main/java/org/jodconverter/local/filter/Filter.java)
interface, with a public no-argument constructor, in a jar added to the `lib` directory of the distribution:

```yaml
filters:
  - type: pages-selector
    pages: [1, 2]
  - class: com.example.WatermarkFilter
```

#### SSL options

Combined with the `-c` option, the `ssl` section configures the
[SSL support](libreoffice-online.md#ssl-support) of the connection to the LibreOffice Online server. Its
keys are the properties of the `SslConfig` class, in kebab case:

```yaml title="ssl.yml"
ssl:
  # Whether SSL support is enabled. Defaults to false.
  enabled: true
  # The supported SSL ciphers; a list, or comma-separated. Defaults to the JVM default values.
  ciphers: [ECDHE_RSA_WITH_AES_256_CBC_SHA384, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA]
  # The enabled SSL protocols; a list, or comma-separated. Defaults to the JVM default values.
  enabled-protocols: [TLSv1.2, TLSv1.3]
  # The alias that identifies the key in the key store.
  key-alias: keyalias
  # The password used to access the key in the key store.
  key-password: keypassword
  # The path to the key store.
  key-store: /path/to/the/keystore.jks
  # The password used to load the key store.
  key-store-password: keystorepassword
  # The type of key store.
  key-store-type: JKS
  # The provider for the key store.
  key-store-provider: BC
  # The path to the trust store.
  trust-store: /path/to/the/truststore.p12
  # The password used to load the trust store.
  trust-store-password: truststorepassword
  # The type of trust store.
  trust-store-type: PKCS12
  # The provider for the trust store.
  trust-store-provider: SUN
  # The SSL protocol to use. Defaults to TLS.
  protocol: TLS
  # Whether every certificate is trusted, without a trust store. Defaults to false.
  trust-all: false
  # Whether the host name is verified during the SSL handshake. Defaults to true.
  verify-hostname: true
```

```shell
jodconverter-cli -c "https://localhost:8001/lool/convert-to/" --config ssl.yml infile outfile
```

--8<-- "note.md"
