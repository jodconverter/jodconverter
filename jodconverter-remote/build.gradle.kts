description =
    "Module required in order to process remote conversions (LibreOffice Remote) for the Java OpenDocument Converter (JODConverter) project."

extra["moduleName"] = "JODConverter Remote"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    api(project(":jodconverter-core"))

    implementation(libs.slf4j.api)

    testRuntimeOnly(libs.bundles.log4j)
    testImplementation(libs.spring.test)
    testImplementation(libs.wiremock)
    testImplementation(project(":jodconverter-core", configuration = "tests"))
}
