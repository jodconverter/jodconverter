# Contributing to documentation

The JODConverter documentation is built with [MkDocs](https://www.mkdocs.org/), using
the [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) theme and extensions. This section explains how
to preview and contribute changes locally, plus conventions to keep docs consistent. If you're not familiar with
MkDocs, check out the [Getting Started](https://www.mkdocs.org/#getting-started) guide. You can also open an issue
to discuss the changes you want to make (and we'll do to MkDocs stuff).

## Prerequisites

- Python 3.8+ recommended
- Install mkdocs-material and plugins:
  pip install mkdocs-material mike

## Preview locally

- From the repository root:
  mkdocs serve
- Then open http://127.0.0.1:8000. Edits in docs/ hot-reload automatically.

## Versioned docs (mike)

We use [mike](https://github.com/jimporter/mike) with Material's version selector.

Note: GitHub Actions workflow .github/workflows/deploy-docs.yml handles deployment on pushes to master/main; use mike
locally mainly for testing and preparing versioned content.

## Content conventions

- Terminology: use "LibreOffice (LO)" and "Apache OpenOffice (AOO)" explicitly. Avoid "OOo" unless historically
  relevant.
- Headings: start pages with a single H1 (#). Keep title succinct; add a one-paragraph intro.
- Related section: add a brief "Related" links section at the end of concept/guide pages when applicable.
- Code blocks: annotate language; use Material tabs for Gradle/Maven as needed (=== "Gradle" / === "Maven").
- Reuse content: favor docs/snippets + pymdownx.snippets to avoid duplication.
- Links: prefer relative links within the site; avoid deep anchors that are likely to change.
