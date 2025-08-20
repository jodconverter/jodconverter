# Configuration Overview

This page is a landing hub for configuring JODConverter components. Use it to quickly navigate to the right manager or converter and discover key options.

## What do you want to configure?

- Office Managers (how office processes are run and managed)
  - LocalOfficeManager — start and manage local LibreOffice/AOO processes.
  - ExternalOfficeManager — connect to an already-running local process you manage externally.
  - RemoteOfficeManager — connect to a remote LibreOffice Online/Collabora server.
- Document Converters (how to execute conversions)
  - LocalConverter — convert using a local OfficeManager (UNO).
  - RemoteConverter — convert via a remote HTTP API (LOOL/Collabora).

## Quick matrix

| Component              | Where it runs           | Typical use case                              | Key page |
|------------------------|-------------------------|-----------------------------------------------|----------|
| LocalOfficeManager     | Local machine (headless)| Server/service with LibreOffice/AOO installed | [LocalOfficeManager](./local-manager.md) |
| ExternalOfficeManager  | Local, started externally| You control soffice lifecycle outside the JVM | [ExternalOfficeManager](./external-manager.md) |
| RemoteOfficeManager    | Remote (HTTP/WebSocket) | Use LibreOffice Online/Collabora remotely      | [RemoteOfficeManager](./remote-manager.md) |
| LocalConverter         | Local UNO               | In‑process conversions via local office        | [LocalConverter](./local-converter.md) |
| RemoteConverter        | Remote HTTP             | Call a remote conversion service               | [RemoteConverter](./remote-converter.md) |

## Tips

- Start with Office Managers to decide how and where office processes will run.
- Then pick the matching Converter (LocalConverter with Local/External managers; RemoteConverter with RemoteOfficeManager).
- For global defaults and format handling, see Getting Started → Document Format Registry.

## Related

- Getting Started: [Office Managers](../getting-started/office-managers.md)
- Getting Started: [Document Converters](../getting-started/document-converters.md)
- Getting Started: [Document Format Registry](../getting-started/document-format-registry.md)
