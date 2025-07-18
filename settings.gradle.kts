rootProject.name = "jodconverter"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.10.0")
}

includeBuild("build-logic")

include(
    "jodconverter-core",
    "jodconverter-local",
    "jodconverter-local-lo",
    "jodconverter-local-oo",
    "jodconverter-remote",
    "jodconverter-cli",
    "jodconverter-spring",
    "jodconverter-spring-boot-starter"
)
