# Running in Containers

**JODConverter** runs well in a container, as long as the image provides a complete office installation, an init process
that reaps child processes, and the fonts your documents use. This page covers the points that cause most container
issues.

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

## Install the fonts your documents use

When a font used by a document is missing, LibreOffice substitutes another one. If the substitute has different metrics,
lines wrap differently and the output can get extra pages or a shifted layout, typically when converting Word documents
to PDF. This is the most common reason why a converted document doesn't look like the original.

Install the fonts your documents use, or fonts with the same metrics:

| Font in the document                | Metric-compatible font       | Debian/Ubuntu package     |
|-------------------------------------|------------------------------|---------------------------|
| Arial, Times New Roman, Courier New | Liberation Sans, Serif, Mono | `fonts-liberation`        |
| Calibri                             | Carlito                      | `fonts-crosextra-carlito` |
| Cambria                             | Caladea                      | `fonts-crosextra-caladea` |
| Chinese, Japanese, Korean text      | Noto CJK                     | `fonts-noto-cjk`          |

Check which font replaces a missing one with `fc-match`, for example `fc-match "Calibri"`. To compare candidate
replacement fonts visually, the community tool
[LibreOffice-fonts-compare](https://github.com/cuipengfei/LibreOffice-fonts-compare) can help.

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
