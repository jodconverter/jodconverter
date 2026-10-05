description =
    "Module required in order to process remote conversions (LibreOffice Remote) for the Java OpenDocument Converter (JODConverter) project."

extra["moduleName"] = "JODConverter Remote"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
    id("publish-conventions")
}

dependencies {
    api(project(":jodconverter-core"))

    implementation(libs.slf4j.api)

    implementation(libs.httpcore)
    implementation(libs.httpclient)
    implementation(libs.httpmime)
    implementation(libs.fluent.hc)

    constraints {
        implementation(libs.commons.codec) {
            because("httpclient requests an older version")
        }
    }

    testRuntimeOnly(libs.bundles.log4j)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.spring.test)
    testImplementation(libs.wiremock)
    testImplementation(project(":jodconverter-core", configuration = "tests"))
}
