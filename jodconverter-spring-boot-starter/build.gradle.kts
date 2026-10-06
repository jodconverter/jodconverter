description = "Spring Boot integration module of the Java OpenDocument Converter (JODConverter) project."

extra["moduleName"] = "JODConverter Spring Boot Starter"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    // The Spring Boot BOM manages the versions of the Spring Boot dependencies of the starter.
    implementation(platform(libs.spring.boot.dependencies))

    compileOnly(project(":jodconverter-local"))
    compileOnly(project(":jodconverter-remote"))
    annotationProcessor(libs.spring.boot.configuration.processor)

    implementation(libs.spring.boot.starter)
    // The health indicator is only configured when the application has the actuator.
    compileOnly(libs.spring.boot.starter.actuator)

    testImplementation(project(":jodconverter-local"))
    testImplementation(project(":jodconverter-remote"))

    testImplementation(libs.wiremock)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.actuator)
    testImplementation(libs.jakarta.annotations)
}
