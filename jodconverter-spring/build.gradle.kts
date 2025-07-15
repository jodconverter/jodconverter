description = "Spring integration module of the Java OpenDocument Converter (JODConverter) project."

extra["moduleName"] = "JODConverter Spring"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    implementation(project(":jodconverter-local"))

    implementation(libs.slf4j.log4j)
    implementation(libs.spring.core)
    implementation(libs.spring.context)
    implementation(libs.javax.annotations)

    testImplementation(libs.spring.test)
    testImplementation(libs.slf4j.log4j)
    testImplementation(libs.mockito.inline)
}
