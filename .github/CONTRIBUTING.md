# Contributing to JODConverter

Thanks for taking the time to contribute! Bug reports, fixes, improvements and documentation are all welcome.

## Before you start

- **Bugs and questions**: search the [existing issues](https://github.com/jodconverter/jodconverter/issues) first. If nothing matches, open a new one using the bug report form.
- **Non-trivial changes**: open an issue first to discuss the idea. It avoids spending time on a pull request that goes in a different direction than the project.
- **Small fixes** (typos, obvious bugs, documentation): go straight to a pull request.

## Set up the project

You need:

- **A JDK to run Gradle.** CI uses JDK 17. The code itself is compiled and tested for Java 8 through a Gradle toolchain, which is downloaded automatically when it is missing.
- **LibreOffice** (or Apache OpenOffice), only for the integration tests. It is detected in its default installation location.

Always use the Gradle wrapper (`./gradlew`, or `gradlew.bat` on Windows).

## Build and test

| What                                         | Command                                                                     |
|----------------------------------------------|-----------------------------------------------------------------------------|
| Everything, as CI does                       | `./gradlew build -x javadoc`                                                |
| Everything except the integration tests      | `./gradlew build -x javadoc -x integrationTest`                             |
| Unit tests of one module                     | `./gradlew :jodconverter-local:test`                                        |
| Integration tests of one module              | `./gradlew :jodconverter-local:integrationTest`                             |
| One test class                               | `./gradlew :jodconverter-core:test --tests "org.jodconverter.core.SomeTest"` |

Unit tests live in `src/test/java`. Tests that need a running office live in `src/integTest/java`.

## Code style

- The code follows the [Google Java Style](https://google.github.io/styleguide/javaguide.html), enforced by Spotless. Run `./gradlew spotlessApply` before committing; the build fails when the formatting is off.
- Spotless also adds the license header to new source files.
- Keep the code compatible with Java 8.

## Tests

- Add a test showing that the bug is fixed or that the feature works.
- Tests use JUnit 5 and AssertJ (`assertThat(...)`), with Mockito for test doubles.
- Group the tests of a method in a `@Nested` class named after it, and name each test after the situation and the expected outcome, for example `whenTaskExecutionTimeout_ShouldThrowOfficeException`.

## Branches and commits

1. Fork the repository and create a branch from `develop`, named after the kind of change: `feature/<short-name>`, `bugfix/<short-name>`, `docs/<short-name>` or `chore/<short-name>`.
2. Commit as you like on your branch. Pull requests are squash-merged, so your commits become one commit on `develop`.
3. The **pull request title** becomes the commit message on `develop`, so make it a short sentence describing the change, such as "Fix the page count of password-protected documents".

## Pull requests

- Target the `develop` branch.
- Fill in the pull request template, and link the issue the pull request fixes (`Fixes #123`).
- The `build` check must pass. It runs the full build and the tests on Linux and macOS. If your change is specific to Windows, mention how you tested it, since Windows is currently not part of CI.
- Keep a pull request focused on one change; smaller pull requests get reviewed faster.

## For maintainers

- `develop` is the integration branch and the default branch; every change reaches it through a pull request.
- `master` holds the released versions. A release is merged from `develop` into `master` with a merge commit (not a squash), and tagged `vX.Y.Z`.
- Both branches are protected by a ruleset: no force-push, no deletion, changes through pull requests, and the `build` check required. Repository admins can bypass it.
