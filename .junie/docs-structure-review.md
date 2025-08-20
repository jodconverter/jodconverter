# Documentation Structure Review and Recommendations (Aug 2025)

This document evaluates the current documentation site (MkDocs + Material) and proposes actionable improvements. It focuses on information architecture, navigation, consistency, contribution workflow, and DX for maintainers/readers.

Summary of findings
- Overall structure is solid: clear top-level sections (Getting Started, Configuration, Samples, Releases, Migration Guides, FAQ, Contributing). Pages use Material features (tabs, admonitions, snippets, code copy) and cross-link reasonably well. ✅
- Navigation can be streamlined: long flat lists under Releases and Migration Guides make the left nav crowded. Consider index landing pages with latest highlights. ➔ Proposal below.
- Contributor experience can be improved with an Edit on GitHub link and local preview instructions. ➔ Add edit_uri and enhance docs/contributing/documentation.md.
- Cross-linking and consistency: Some pages include a "Related Pages" footer; others don’t. Standardize a short "Related" section for key guides.
- Terminology and abbreviations: Pages sometimes use OOo, LibreOffice Online, LOOL/Collabora Online interchangeably. Prefer consistent terminology across the site (see Style recommendations).

Information architecture recommendations
1) Home section
- Keep Overview concise. It’s currently good. Consider moving the large modules overview to a dedicated "Modules" page under Getting Started and link from the home page. Optional.

2) Getting Started
- The section is strong. Ensure each subpage has:
  - Brief intro, prerequisites, examples, and a Related section.
  - Cross-links to Configuration pages for deeper options (already present in many pages).
- Consider a quick "Choosing a module" guide (flowchart or bullets) at the top of the section.

3) Configuration
- Already split by manager and converter types. Good.
- Add a short index/landing page (configuration/index.md) with a matrix linking each component to its key options, defaults, and where they are used (CLI vs Java). This helps first-time readers.

4) Releases and Migration Guides
- Current nav lists all versions individually. This is helpful, but long in the left nav.
- Proposed change:
  - Keep a short list of the last 3–5 releases in the nav, and add an "All Releases" index page that lists the rest.
  - Same for Migration Guides.
  - Alternatively, enable navigation.sections (commented in mkdocs.yml) and group by major/minor to reduce clutter.

5) Samples
- Consider adding runnable prerequisites (Java version, LibreOffice requirement) and how to start each sample quickly.
- Where appropriate, add links to GitHub subfolders or code excerpts using snippets.

6) FAQ
- Great central place. Periodically audit for duplicates with Guides.

Navigation and MkDocs configuration
- Add edit_uri to mkdocs.yml so an "Edit this page" button appears. Suggested:
  edit_uri: edit/master/docs/
- Consider enabling theme feature navigation.sections to visually group large sections.
- Consider adding the tags plugin (mkdocs-material extensions) and using page-level tags for search facets (optional).

Style and consistency guidelines
- Use "LibreOffice" (LO) and "Apache OpenOffice" (AOO) explicitly. Avoid "OOo" unless referring to legacy naming.
- Prefer American English spelling for consistency unless the project policy states otherwise.
- Code blocks: annotate language and use Material tabs for Gradle/Maven when relevant (already used in many pages).
- Related links: add a brief Related section at the end of concept/guide pages.
- Anchors in intra-site links: when linking to headings, keep them stable; avoid deep anchors if titles may change.

Contribution workflow for docs
- Local preview: Document how to install mkdocs-material and run mkdocs serve locally. (Added to contributing/documentation.md in this change.)
- Snippets: Prefer docs/snippets for shared fragments and include with pymdownx.snippets to avoid duplication.
- Versioning: Project uses mike. Document how to publish a new version of docs (e.g., mike deploy X.Y.Z and update latest).

Actionable changes proposed in this PR
- Added this review document under Contributing so maintainers have a reference.
- Suggest adding edit_uri to mkdocs.yml so contributors can easily edit pages from the site UI.
- Propose creating two index pages:
  - configuration/index.md (landing + matrix)
  - release-notes/index.md and migration-guides/index.md (list all versions; nav can then show only the latest N)
  These can be added in a follow-up change if desired.

Suggested nav adjustments (optional, follow-up)
- Home → Releases → show last 3–5 entries; add "All Releases" pointing to release-notes/index.md with a full list.
- Home → Migration Guides → show last 3–5 entries; add "All Migrations" pointing to migration-guides/index.md.

Checklist for future improvements
- [ ] Create configuration/index.md landing page with a configuration matrix.
- [ ] Add All Releases and All Migration Guides index pages and trim nav lists to last N.
- [ ] Audit "Related" footers across Getting Started and Configuration pages for consistency.
- [ ] Standardize terminology across pages (LO, AOO; avoid OOo except historically).
- [ ] Consider enabling navigation.sections in mkdocs.yml to reduce left-nav cognitive load.
- [ ] Add tags support if you want search facets and page badges.

If you’d like, I can follow up with the creation of the proposed index pages and corresponding nav updates.
