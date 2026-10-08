# Releasing

How a version of **JODConverter** is released. The steps need write access to the repository, the Maven Central
credentials and the signing key, which live in the repository secrets; the rest is done by the `release` workflow.

## Before the release

1. **Labels.** Every pull request merged and every issue closed since the last tag has a label (`enhancement`, `bug`,
    `documentation`, `dependencies`): the release notes that GitHub generates are grouped by label
    (`.github/release.yml`).
2. **Release notes page.** Write `docs/release-notes/release-notes-X.Y.Z.md` and link it from
    `docs/release-notes/index.md` and the navigation in `zensical.toml`. For a major or minor version, the migration
    guide `docs/migration-guides/migration-guide-X.Y.Z.md` exists and is linked the same way.
3. **CHANGELOG.md.** Add the `X.Y.Z` block, one line per pull request with its number, from:
    ```bash
    gh pr list --state merged --base develop --search "merged:>YYYY-MM-DD" --json number,title --jq '.[] | "- \(.title) (#\(.number))"'
    ```
4. **Version.** Set the version everywhere at once:
    ```bash
    ./gradlew setVersion -PnewVersion=X.Y.Z
    ```
    The task writes `version` in `gradle.properties` and, for a release version, replaces the previous released
    version in the pages of the documentation that show dependency coordinates.
5. **Full build and integration tests, locally.** The continuous integration runs them on Linux only; a release
    deserves a run with the office installation of another platform:
    ```bash
    ./gradlew clean build integrationTest
    ```
6. **Release commit.** On a `release/X.Y.Z` branch, commit with the message `Release X.Y.Z` and open its pull
    request to `develop`, which only takes pull requests. Once it is merged, open the pull request from `develop` to
    `master`, let the checks pass, merge it with a merge commit.

## The release

7. **Tag.** On `master`, after the merge:
    ```bash
    git checkout master && git pull && git tag -a vX.Y.Z -m "Release X.Y.Z" && git push origin vX.Y.Z
    ```
    The `release` workflow then builds the tag, publishes every module on Maven Central (signed, released
    automatically once the portal validates them) and creates the GitHub release `vX.Y.Z` with the generated notes
    and the command line tool archives. The `deploy-docs` workflow, run by the push to `master`, publishes the
    documentation of the version with the `latest` alias.
8. **Check** the [release](https://github.com/jodconverter/jodconverter/releases), the artifacts on
    [Maven Central](https://central.sonatype.com/search?q=g:org.jodconverter) (they appear within a few hours) and the
    [documentation](https://jodconverter.github.io/jodconverter/latest/).
9. **Announce** on Gitter:
    ```
    JODConverter X.Y.Z is out!
    * Release Notes: https://jodconverter.github.io/jodconverter/latest/release-notes/release-notes-X.Y.Z/
    * Migration Guide: https://jodconverter.github.io/jodconverter/latest/migration-guides/migration-guide-X.Y.Z/
    ```

## After the release

10. **Next snapshot.** On `develop`, merge `master` if the release pull request was not a fast-forward, then:
    ```bash
    ./gradlew setVersion -PnewVersion=X.Y.Z+1-SNAPSHOT
    ```
    Commit `Post release X.Y.Z` and push.
11. **Milestone.** Close the milestone of the release and create the next one.

## Secrets of the release workflow

| Secret                   | Content                                                                  |
| ------------------------ | ------------------------------------------------------------------------ |
| `MAVEN_CENTRAL_USERNAME` | The user name of the Maven Central portal token.                         |
| `MAVEN_CENTRAL_PASSWORD` | The password of the Maven Central portal token.                          |
| `SIGNING_KEY`            | The armored private GPG key (`gpg --armor --export-secret-keys KEY_ID`). |
| `SIGNING_KEY_ID`         | The last 8 characters of the key id.                                     |
| `SIGNING_PASSWORD`       | The passphrase of the key.                                               |

The `build` workflow uses one more secret, `COVERALLS_REPO_TOKEN`, the repository token shown on the Coveralls page of
the project, to send the coverage of the Linux job.

The secrets are checked without a release by running the `release` workflow by hand from the Actions tab (the
"Run workflow" button, on any branch): it builds, signs the publications with the key of the secrets and checks the
Maven Central credentials against the portal, without publishing anything.

### The signing key

The key must not be expired when the portal validates the signatures. `gpg --list-secret-keys --keyid-format long`
shows the expiry; `gpg --edit-key KEY_ID` with the `expire` command (then `save`) extends it, for the primary key and,
after `key 1`, for the subkey. The public key must be known by the key servers the portal queries, with the current
expiry:

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
gpg --keyserver keys.openpgp.org --send-keys KEY_ID
```

The `SIGNING_KEY` secret is set without pasting the key anywhere:

```bash
gpg --armor --export-secret-keys KEY_ID | gh secret set SIGNING_KEY --repo jodconverter/jodconverter
```

A release can still be done from a workstation with the same values in `~/.gradle/gradle.properties`
(`mavenCentralUsername`, `mavenCentralPassword`, `signing.keyId`, `signing.password`, `signing.secretKeyRingFile`) and
`./gradlew publishToMavenCentral`, then the GitHub release created by hand from the tag.
