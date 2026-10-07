# Working on JODConverter

Guidelines for coding agents (and humans in a hurry). Keep this file short: it holds what cannot be read from the code. The facts that change with time (versions, module lists, options) live in the build files and the docs; read them rather than trusting a number written here.

## What this is

A Java library that converts office documents by driving LibreOffice (or Apache OpenOffice) over the UNO bridge, or a LibreOffice Online / Collabora Online server over HTTP. Gradle multi-module build (Kotlin DSL) with conventions in `build-logic/`. The Java baseline is `java` in `gradle/libs.versions.toml`; the Spring Boot line is `spring-boot` there. `develop` is the default branch; releases are merged to `master` and tagged.

Modules: `jodconverter-core` (API, formats, the office manager pool, PDF options), `jodconverter-local` (UNO: `LocalOfficeManager`, `AttachedOfficeManager`, `LocalConverter`, filters, `exportPages`, `merge`), `jodconverter-local-lo` / `jodconverter-local-oo` (the office jars), `jodconverter-remote` (HTTP client of an online server), `jodconverter-cli` (command line tool, not published to Maven Central), `jodconverter-spring-boot-starter`.

## Build and test

Always the wrapper. `.github/CONTRIBUTING.md` has the table of commands; the essentials:

- `./gradlew build -x javadoc`: everything, as CI does on Linux, including the integration tests.
- `./gradlew build -x javadoc -x integrationTest`: without the tests that need an office.
- `./gradlew spotlessApply`: format before committing; `spotlessCheck` fails the build otherwise.
- Unit tests are in `src/test/java`; tests that need a running office or a WireMock server are in `src/integTest/java` and run with the `integrationTest` task.

Integration tests start office processes on fixed ports (2002 and 2099): never run two integration test runs at once on the same machine, including from another checkout. They need LibreOffice installed in its default location; the `jodconverter-remote` ones need no office.

Checkstyle (`checkstyle.xml`) and PMD (`ruleset.xml`) run with the build; PMD does not fail the build but its report is read, so do not add violations. JaCoCo reports per module (`jacocoTestReport`) and aggregated (`jacocoRootReport`). New code is expected to be covered by unit tests, and by an integration test when it talks to an office.

## Conventions

- Google Java Style through Spotless; the license header is added by Spotless. Use the Java syntax of the baseline: `var`, records, switch expressions, pattern matching, `List.of`.
- Acronyms stay upper case in names, as in the JDK: `URL`, `IOException`, `OSUtils`, not `Url`; Checkstyle allows `URL`, `IO` and `OS`, other acronyms get a `@SuppressWarnings("checkstyle:AbbreviationAsWordInName")`.
- Nullness annotations are `org.checkerframework.checker.nullness.qual.NonNull` / `Nullable` on the public API.
- Public API is deliberate: builders with primitive setters, immutable results, `@Deprecated(forRemoval = true)` with a replacement for anything renamed in a major version, and a line in the migration guide of the version (`docs/migration-guides/`).
- Logging through SLF4J; no `System.out`.
- Tests: JUnit Jupiter, AssertJ (`assertThat`, `assertThatIllegalArgumentException`...), Mockito; test method names read `when..._Should...` or `with..._Should...`; nested classes group the cases of one method.
- Documentation lives in `docs/` (Zensical site, navigation in `zensical.toml`); a user-visible change updates the page that describes it and, in a major version, the migration guide. The pages are formatted with `mdformat` as `docs/contributing/documentation.md` says.

## Git and pull requests

- Branch from `develop`, one topic per pull request, squash-merged. The title is a sentence that says what changes and why it matters, not a label.
- Commit messages and pull request bodies are plain prose, one line per paragraph (no hard wrapping). No attribution lines of any kind (no `Co-Authored-By`, no "generated with" footers).
- Do not comment on issues or pull requests, change labels, settings or releases on behalf of the maintainer without being asked to.
- Secrets never go in the repository: the signing key and the Maven Central credentials are repository secrets (release workflow) or `~/.gradle/gradle.properties` on a workstation. `docs/contributing/release.md` describes the release.

## Things that look wrong but are not

- `jodconverter-cli` is published as a distribution archive, not as a Maven artifact.
- `TableOfContentUpdaterFilter`, `ExternalOfficeManager` and the `jodconverter.external.*` properties exist only as deprecated aliases of their renamed counterparts.
- Checkstyle, google-java-format and a few tools are pinned to the last version that runs on the Java baseline; `gradle/libs.versions.toml` says which and why. Dependabot ignores major versions for that reason.
- The Windows and macOS CI matrix entries are commented out on purpose; `.github/workflows/build.yml` says why.
