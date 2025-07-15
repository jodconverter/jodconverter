description = "Spring Boot integration module of the Java OpenDocument Converter (JODConverter) project."

extra["moduleName"] = "JODConverter Spring Boot Starter"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    compileOnly(project(":jodconverter-local"))
    compileOnly(project(":jodconverter-remote"))
    annotationProcessor(libs.spring.boot.configuration.processor)

    implementation(libs.spring.boot.starter)

    testImplementation(project(":jodconverter-local"))
    testImplementation(project(":jodconverter-remote"))

    testImplementation(libs.wiremock)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.javax.annotations)
}
