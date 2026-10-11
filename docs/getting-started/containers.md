# Running in Containers

**JODConverter** runs well in a container, as long as the image provides a complete office installation, an init process that reaps child processes, a UTF-8 locale, and the fonts your documents use. This page covers the points that cause most container issues.

## Use an init process

LibreOffice starts helper processes of its own. When **JODConverter** stops or kills an office process, those children
are re-parented to process 1 of the container. If process 1 is your Java application, nobody reaps them and they pile up
as zombie processes (`<defunct>` in `ps`), for example after each restart triggered by `maxTasksPerProcess`.

Run the container with an init process that reaps them:

=== "Docker"

    ```bash
    docker run --init my-converter-image
    ```

=== "Docker Compose"

    ```yaml
    services:
      converter:
        image: my-converter-image
        init: true
    ```

=== "Image entrypoint"

    ```dockerfile
    # Debian/Ubuntu based image; use this when you can't pass --init (Kubernetes for example)
    RUN apt-get update && apt-get install -y --no-install-recommends tini && rm -rf /var/lib/apt/lists/*
    ENTRYPOINT ["/usr/bin/tini", "--", "java", "-jar", "/app/app.jar"]
    ```

## Install a complete office

The office installation must include the modules for every document family you convert: Writer for text documents,
Calc for spreadsheets, Impress for presentations and Draw for drawings. A partial installation fails with errors such as
`URL seems to be an unsupported one` when loading a document whose module is missing.

On Debian or Ubuntu, `--no-install-recommends` keeps the image smaller while installing the modules you need:

```dockerfile
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      libreoffice-writer libreoffice-calc libreoffice-impress libreoffice-draw \
 && rm -rf /var/lib/apt/lists/*
```

If a distribution package misbehaves (for example a `java_remote_bridge ... is disposed` error right after the
connection), try the official packages
from [libreoffice.org](https://www.libreoffice.org/download/download-libreoffice/)
instead.

## Set a UTF-8 locale

A minimal image has no locale set, which means the `POSIX` locale and ASCII file names. A document whose name has an accent or a non-Latin character then fails before the office is even asked: Java cannot map the name to a path and throws `InvalidPathException: Malformed input or input contains unmappable characters`. Set a UTF-8 locale in the image; `C.UTF-8` needs no extra package on Debian and Ubuntu:

```dockerfile
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
```

## Install the fonts your documents use

When a font used by a document is missing, LibreOffice substitutes another one. If the substitute has different metrics,
lines wrap differently and the output can get extra pages or a shifted layout, typically when converting Word documents
to PDF. This is the most common reason why a converted document doesn't look like the original.

Install the fonts your documents use, or fonts with the same metrics:

| Font in the document                | Metric-compatible font       | Debian/Ubuntu package     |
| ----------------------------------- | ---------------------------- | ------------------------- |
| Arial, Times New Roman, Courier New | Liberation Sans, Serif, Mono | `fonts-liberation`        |
| Calibri                             | Carlito                      | `fonts-crosextra-carlito` |
| Cambria                             | Caladea                      | `fonts-crosextra-caladea` |
| Chinese, Japanese, Korean text      | Noto CJK                     | `fonts-noto-cjk`          |
| Emoji                               | Noto Color Emoji             | `fonts-noto-color-emoji`  |

Check which font replaces a missing one with `fc-match`, for example `fc-match "Calibri"`. To compare candidate
replacement fonts visually, the community tool
[LibreOffice-fonts-compare](https://github.com/cuipengfei/LibreOffice-fonts-compare) can help.

The fonts are not the only thing that moves the line breaks. A document with automatic hyphenation is hyphenated with the dictionary of its language, and without that dictionary the words are not split, so the lines wrap elsewhere. On Debian and Ubuntu the dictionaries are the `hyphen-*` packages (`hyphen-en-us`, `hyphen-fr`, `hyphen-de`...); install those of the languages of your documents.

## A reference image

[jodconverter/docker-image-jodconverter-runtime](https://github.com/jodconverter/docker-image-jodconverter-runtime) is the image the project maintains as a base for applications that use **JODConverter**: a Debian image with a Java runtime, LibreOffice, a UTF-8 locale and a user that is not root. Use it as it is, or read its `Dockerfile` as a working example of this page; add the fonts your documents need and the init process.

## Size the container

- Each port number (or pipe name) starts one office process, and each process has its own memory footprint, typically
    hundreds of megabytes. Size the container memory for the number of processes you configure.
- Office processes write their profile and temporary files under the `workingDir` (the system temp directory by
    default). Put it on a local disk with enough free space.
- Keep a `taskExecutionTimeout`: a document that makes LibreOffice hang is then killed and the process restarted.

## Related

- [LocalOfficeManager configuration](../configuration/local-manager.md)
- [Security](security.md)
- [LibreOffice Online](libreoffice-online.md), to convert through a separate conversion server instead

--8<-- "note.md"
