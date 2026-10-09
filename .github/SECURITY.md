# Security policy

## Supported versions

Security fixes are made on the latest release only.

## Reporting a vulnerability

Please do not report security vulnerabilities in public issues.

Report them privately through GitHub instead: [open a security advisory](https://github.com/jodconverter/jodconverter/security/advisories/new), or open the **Security** tab of the repository and click **Report a vulnerability**. You will get an answer as soon as possible, and the fix will be credited to you unless you prefer otherwise.

## Verifying a release

The artifacts on [Maven Central](https://central.sonatype.com/namespace/org.jodconverter) and the archives of the command line tool attached to a [GitHub release](https://github.com/jodconverter/jodconverter/releases) are signed with the same OpenPGP key. Each archive has its signature next to it:

```bash
gpg --verify jodconverter-cli-X.Y.Z.zip.asc jodconverter-cli-X.Y.Z.zip
```
