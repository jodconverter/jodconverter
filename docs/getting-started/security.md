# Security

Converting a document means opening it in LibreOffice or Apache OpenOffice, with everything the document format allows.
When documents come from untrusted users, treat the office process as code that handles hostile input, and isolate it.

## External references in documents

Documents can reference external resources: linked images, linked sections or data, OLE objects, `file:///` and
`http://` URLs. When the office program resolves them while loading a document, a crafted document can make the server
read a local file and put its content in the converted output (file disclosure), or send requests to internal services
(server-side request forgery, SSRF).

**What JODConverter does:** since version 4.4.4, documents are loaded with `UpdateDocMode.NO_UPDATE`, so links are not
updated on load. The previous default, `QUIET_UPDATE`, updated them silently, which was exploited in
[CVE-2020-25820](https://nvd.nist.gov/vuln/detail/CVE-2020-25820). The `useUnsafeQuietUpdate` option of the
[LocalConverter](../configuration/local-converter.md#useunsafequietupdate) brings the old behavior back: don't enable it
for untrusted documents.

**What it does not cover:** `NO_UPDATE` does not stop every external resource from being loaded; for example, linked
images in a document can still be fetched while it is loaded. Isolation is what protects you.

## Isolate the office process

- **Network:** run the office processes where they can't reach internal services, cloud metadata endpoints or the
  Internet. A container or a network policy without outbound access blocks SSRF whatever the document contains.
- **File system:** run them as a dedicated, unprivileged user that can only read the documents to convert and its own
  working directory, so that a `file:///` reference has nothing sensitive to read.
- **Dedicated installation:** don't share the office installation and its user profile with people who use it
  interactively.
- **Limits:** keep a `taskExecutionTimeout`, and limit the size of the uploaded documents in your application.
- **Updates:** keep LibreOffice up to date; security fixes in document filters are released regularly.

Office settings (Tools > Options), such as the security and load/save options, can be applied to the office processes
started by **JODConverter** with a [templateProfileDir](../configuration/local-manager.md#templateprofiledir).

## Converting through a separate server

Converting through a remote server, such as [LibreOffice Online or Collabora Online](libreoffice-online.md), keeps the
office program out of your application's host altogether. The isolation advice above then applies to that server.

## Related

- [Running in containers](containers.md)
- [LocalConverter configuration](../configuration/local-converter.md)

--8<-- "note.md"
