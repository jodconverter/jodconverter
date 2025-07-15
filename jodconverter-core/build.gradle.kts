description =
    "Core JODConverter abstractions, used by JODConverter implementations, such as JODConverter Local or JODConverter Remote, used to convert office documents using LibreOffice or Apache OpenOffice."

extra["moduleName"] = "JODConverter Core"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.slf4j.api)

    testImplementation(libs.mockito.inline)
    testImplementation(libs.spring.test)

    testRuntimeOnly(libs.slf4j.log4j)
}

// --- test setup -----------------------------------------------------------

// Configuration group used to manage test dependencies
// (when test classes depend on test classes from another project)
val tests by configurations.creating

val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("test")
    from(sourceSets["test"].output)
}

artifacts {
    add("tests", testJar)
}
