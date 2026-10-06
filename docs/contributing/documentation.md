# Contributing to documentation

The JODConverter documentation is built with [Zensical](https://zensical.org/), from the Markdown files in `docs/` and the `zensical.toml` configuration at the root of the repository. This section explains how to preview changes locally, plus conventions to keep the docs consistent. You can also open an issue to discuss the changes you want to make.

## Prerequisites

- Python 3.10 or newer.

- Zensical, preferably in a virtual environment, at the version the site is built with:

    ```bash
    pip install -r requirements-docs.txt
    ```

## Preview locally

From the repository root:

```bash
zensical serve
```

Then open http://127.0.0.1:8000. Edits in `docs/` reload automatically. Before opening a pull request, check that `zensical build --strict` reports no issues: it also catches broken links and anchors.

## Format the pages

The pages are formatted with [mdformat](https://mdformat.readthedocs.io/), which is installed with the other packages of `requirements-docs.txt`. From the repository root, before opening a pull request:

```bash
mdformat --end-of-line keep --number docs
```

The `--end-of-line keep` option leaves the line endings of your checkout as they are, which matters on Windows where Git checks the files out with CRLF line endings.

Do not use the Markdown formatter of an IDE: it does not know the content tabs, the admonitions and the snippets used by the pages, and it reformats the code blocks. Write code-like text such as `Map<String, Object>` between backticks, since the escapes added by the formatter would be displayed.

## Versioned docs

Each release has its own version of the site, selected from the version menu. Versions are deployed with [mike](https://github.com/squidfunk/mike), using the fork that works with Zensical until Zensical supports versioning natively.

The `.github/workflows/deploy-docs.yml` workflow deploys the documentation: every push to `develop` publishes the `dev` version, and a release pushed to `master` publishes its version number with the `latest` alias. A maintainer can also run the workflow by hand to publish a given release version. You don't need mike to preview your changes.

## Content conventions

- Terminology: use "LibreOffice (LO)" and "Apache OpenOffice (AOO)" explicitly. Avoid "OOo" unless historically relevant.
- Headings: start pages with a single H1 (#). Keep title succinct; add a one-paragraph intro.
- Related section: add a brief "Related" links section at the end of concept/guide pages when applicable.
- Code blocks: annotate language; use content tabs for Gradle/Maven as needed (=== "Gradle" / === "Maven").
- Reuse content: favor docs/snippets + pymdownx.snippets to avoid duplication.
- Links: prefer relative links within the site; avoid deep anchors that are likely to change.
