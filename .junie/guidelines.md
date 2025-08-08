JODConverter – Project Development Guidelines

Audience: Advanced Java developers contributing to this repository.

1. Build and Configuration

- Build System: Gradle (Kotlin DSL) with a custom build-logic included via composite build. Use the provided Gradle wrapper.
- Java Toolchain: The build config enforces a minimum Java version from gradle/libs.versions.toml (java = "1.8"). The Gradle Toolchains + Foojay resolver are enabled (see settings.gradle.kts) and will provision a compatible JDK if available.
- Global Plugins/Conventions: Subprojects apply the java-conventions and (for libraries) library-conventions plugins from build-logic. These wire in Checkstyle, PMD, JaCoCo, Spotless, JUnit 5, AssertJ, Mockito, and standard test tasks (test, integrationTest).
- Building the Entire Project:
  - Windows PowerShell/CMD: .\gradlew.bat build
  - Notes:
    - If you don’t need to run integration tests (they may require LibreOffice), you can skip them: .\gradlew.bat build -x integrationTest
    - The check task depends on jacocoTestReport; JaCoCo aggregate reports are available via the jacocoRootReport task at the root.
- IDE Setup:
  - Import as a Gradle project. The custom conventions configure source sets and testing automatically. Toolchain will resolve a JDK; ensure your IDE honors Gradle’s JVM settings.

2. Testing: How It Works Here

- Framework: JUnit 5 (Jupiter) with the Vintage engine enabled for any legacy tests. Assertions via AssertJ; Mockito available for mocking.
- Tasks:
  - Unit tests: test
  - Integration tests: integrationTest (provided by the Nebula integtest plugin applied from java-conventions)
- Running Tests
  - All unit tests for all modules: .\gradlew.bat test
  - All tests including integration tests: .\gradlew.bat check (or build)
  - Skip integration tests: .\gradlew.bat test or .\gradlew.bat build -x integrationTest
  - Per module:
    - Example (core only): .\gradlew.bat :jodconverter-core:test
  - Single class or method:
    - Class: .\gradlew.bat :jodconverter-core:test --tests "org.jodconverter.core.SomeTestClass"
    - Method (JUnit 5): .\gradlew.bat :jodconverter-core:test --tests "org.jodconverter.core.SomeTestClass.methodName"
- Adding a New Unit Test (Guidelines)
  - Location: <module>/src/test/java
  - Package: Follow the module’s package structure (e.g., org.jodconverter.core for core).
  - Prefer JUnit 5 (org.junit.jupiter.api.Test) and AssertJ (org.assertj.core.api.Assertions) for consistency.
  - Keep tests isolated from external dependencies (file system, network) unless intentionally part of integration tests.
- Integration Tests and LibreOffice
  - Some modules (e.g., jodconverter-local, jodconverter-local-lo) interact with local office suites. Integration tests may launch LibreOffice/OpenOffice headless.
  - Requirements:
    - LibreOffice or Apache OpenOffice installed and reachable (soffice in PATH), or point to an installation via module-specific configuration.
    - The integrationTest task passes a system property org.jodconverter.local.manager.templateProfileDir. You can supply a value:
      .\gradlew.bat :jodconverter-local:integrationTest -Dorg.jodconverter.local.manager.templateProfileDir="C:\\path\\to\\writeable\\temp"
  - If you don’t have LO installed, do not run integrationTest tasks.

3. Demonstrated Example: Create, Run, and Remove a Simple Test

- For demonstration, we temporarily added a minimal JUnit 5 test in the core module at:
  jodconverter-core/src/test/java/org/jodconverter/core/SanityGuidelinesTest.java
  Test body:
    @Test
    void demoTestShouldPass() {
      int a = 2, b = 3;
      assertThat(a + b).isEqualTo(5);
    }
- Run just this test:
  .\gradlew.bat :jodconverter-core:test --tests "org.jodconverter.core.SanityGuidelinesTest"
- Verified outcome: The test passed locally in this environment.
- Cleanup: The file was created only to demonstrate the process and has been removed before finalizing this guidelines document, as requested.

4. Code Quality & Style

- Static Analysis:
  - Checkstyle: Configured at repository root (checkstyle.xml). Test sources have Checkstyle disabled by convention.
  - PMD: Ruleset configured via ruleset.xml; failures are non-blocking (ignoreFailures = true) but should be treated seriously locally.
- Formatting:
  - Spotless: Google Java Format 1.7 (Java 8 compatible), import order from spotless.importorder, and license header from spotless.license.java.
  - Commands:
    - Check formatting: .\gradlew.bat spotlessCheck
    - Apply formatting: .\gradlew.bat spotlessApply
- Coverage:
  - Per-module: jacocoTestReport
  - Aggregate (root): jacocoRootReport

5. Module Map & Practical Notes

- Core library: jodconverter-core — Pure Java utilities, office manager abstractions, format registries, etc. Safe to run unit tests anywhere.
- Local adapters:
  - jodconverter-local, jodconverter-local-lo, jodconverter-local-oo — Bindings to LibreOffice/OpenOffice UNO bridge. Integration tests can spawn office processes; avoid running them unless your environment is prepared.
- Remote/HTTP:
  - jodconverter-remote — Client for remote conversion services. Some tests may use WireMock; ensure ports are free.
- CLI and Spring integrations:
  - jodconverter-cli — Command-line wrapper around converters.
  - jodconverter-spring, jodconverter-spring-boot-starter — Spring integration. Spring Boot version is aligned in libs.versions.toml; current tree targets Java 8-compatible Spring Boot 2.x for baseline.

6. Common Troubleshooting

- Java version issues: The build fails early if your runtime is below the required version (currently 1.8). Prefer running via Gradle wrapper to let toolchains pick a matching JDK.
- Integration test failures on Windows: If soffice cannot start, confirm LibreOffice is installed and that your user has write permissions to the template profile directory. Provide -Dorg.jodconverter.local.manager.templateProfileDir to a writable path.
- Network/Proxy: If toolchains or dependencies fail to download, configure Gradle proxy settings in %USERPROFILE%\.gradle\gradle.properties or via GRADLE_OPTS.
- Flaky tests using timeouts: Some pools/executors use strict timeouts; rerun with --info or --stacktrace and ensure system load is reasonable.

7. Useful Commands (Windows)

- Full build without integration tests: .\gradlew.bat clean build -x integrationTest
- Run unit tests in a module: .\gradlew.bat :jodconverter-remote:test
- Run a single test class: .\gradlew.bat :jodconverter-core:test --tests "org.jodconverter.core.DocumentFormatTest"
- Generate aggregate coverage report: .\gradlew.bat jacocoRootReport
- Reformat all Java sources: .\gradlew.bat spotlessApply

Notes for Contributors
- Keep new tests JUnit 5-native unless interacting with existing Vintage-based tests.
- Prefer AssertJ for expressive assertions.
- Avoid introducing hard dependencies on external software in unit tests; place those under integrationTest.
- Check docs under docs/ for user-facing guides; mkdocs.yml drives site generation but is independent of the Java build.
