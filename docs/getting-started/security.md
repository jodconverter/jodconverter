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

**What it does not cover:** `NO_UPDATE` does not stop every external resource from being loaded. A linked image is still fetched while the document is loaded: with the default settings of LibreOffice 25.2, an image linked to a `file:///` URL ends up in the converted PDF, and an image linked to an `http://` URL makes the office send three requests (`OPTIONS`, `HEAD`, `GET`) to that address. The [profile settings below](#harden-the-office-profile) stop both, and isolation protects you from what settings miss.

## Isolate the office process

- **Network:** run the office processes where they can't reach internal services, cloud metadata endpoints or the
    Internet. A container or a network policy without outbound access blocks SSRF whatever the document contains.
- **File system:** run them as a dedicated, unprivileged user that can only read the documents to convert and its own
    working directory, so that a `file:///` reference has nothing sensitive to read.
- **Dedicated installation:** don't share the office installation and its user profile with people who use it
    interactively.
- **Limits:** keep a `taskExecutionTimeout`, and limit the size of the uploaded documents in your application.
- **Updates:** keep LibreOffice up to date; security fixes in document filters are released regularly.

## Harden the office profile

The office processes started by a `LocalOfficeManager` get a new, empty user profile, so they run with the default settings of the office. A [templateProfileDir](../configuration/local-manager.md#templateprofiledir) gives them settings of your own: the directory is copied to the profile of each process before it starts. It only needs a `user` directory with one file, `registrymodifications.xcu`, which is where the office keeps the settings changed in Tools > Options.

### Block the links of untrusted documents

```text
hardened-profile/
└── user/
    └── registrymodifications.xcu
```

```xml title="hardened-profile/user/registrymodifications.xcu"
<?xml version="1.0" encoding="UTF-8"?>
<oor:items xmlns:oor="http://openoffice.org/2001/registry" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<item oor:path="/org.openoffice.Office.Common/Security/Scripting"><prop oor:name="BlockUntrustedRefererLinks" oor:op="fuse"><value>true</value></prop></item>
</oor:items>
```

=== "Java"

    ```java
    OfficeManager officeManager =
        LocalOfficeManager.builder()
            .templateProfileDir("/opt/jodconverter/hardened-profile")
            .build();
    ```

=== "Spring Boot"

    ```yml title="application.yml"
    jodconverter:
      local:
        template-profile-dir: "/opt/jodconverter/hardened-profile"
    ```

=== "Command Line"

    ```shell
    jodconverter-cli -u /opt/jodconverter/hardened-profile infile outfile
    ```

`BlockUntrustedRefererLinks` is the setting behind the "Block any links from documents not among the trusted locations" option of *Tools > Options > LibreOffice > Security > Security Options and Warnings*. With it, the office does not follow the links of a document that is not in one of its trusted file locations, and a new profile has none: the linked image of the examples above is neither read from the file system nor requested from the network. The integration tests of **JODConverter** check both with this very file.

The setting does not tell a hostile link from a wanted one. Documents that legitimately link to images lose them in the output, and the [LinkedImagesEmbedderFilter](using-filters.md#available-filters) has nothing left to embed. It is a setting of LibreOffice; with another office, check that it has the same effect before relying on it.

### Send the remaining requests through a proxy

To control what the office may still reach, for example when the links must stay enabled, give it a proxy that filters the destinations. The proxy settings of the office are in the same file:

```xml title="registrymodifications.xcu (inside oor:items)"
<item oor:path="/org.openoffice.Inet/Settings"><prop oor:name="ooInetProxyType" oor:op="fuse"><value>2</value></prop></item>
<item oor:path="/org.openoffice.Inet/Settings"><prop oor:name="ooInetHTTPProxyName" oor:op="fuse"><value>proxy.internal</value></prop></item>
<item oor:path="/org.openoffice.Inet/Settings"><prop oor:name="ooInetHTTPProxyPort" oor:op="fuse"><value>3128</value></prop></item>
<item oor:path="/org.openoffice.Inet/Settings"><prop oor:name="ooInetHTTPSProxyName" oor:op="fuse"><value>proxy.internal</value></prop></item>
<item oor:path="/org.openoffice.Inet/Settings"><prop oor:name="ooInetHTTPSProxyPort" oor:op="fuse"><value>3128</value></prop></item>
```

`ooInetProxyType` 2 means a manual proxy (0 is none, 1 the proxy of the system). With these settings, LibreOffice 25.2 sends the request of an `http://` link to the proxy and opens the tunnel of an `https://` link through it (`CONNECT`), which the integration tests check too. A proxy does nothing about `file:///` links: the file system isolation above does.

## Converting through a separate server

Converting through a remote server, such as [LibreOffice Online or Collabora Online](libreoffice-online.md), keeps the
office program out of your application's host altogether. The isolation advice above then applies to that server.

## Related

- [Running in containers](containers.md)
- [LocalConverter configuration](../configuration/local-converter.md)

--8<-- "note.md"
