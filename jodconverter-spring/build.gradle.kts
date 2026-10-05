description = "Spring integration module of the Java OpenDocument Converter (JODConverter) project."

extra["moduleName"] = "JODConverter Spring"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    implementation(project(":jodconverter-local"))

    implementation(libs.slf4j.api)
    implementation(libs.spring.core)
    implementation(libs.spring.context)

    testImplementation(libs.spring.test)
    testRuntimeOnly(libs.bundles.log4j)
    testImplementation(libs.mockito.inline)
}
