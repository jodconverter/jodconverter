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

    testRuntimeOnly(libs.bundles.log4j)
}

// --- test setup -----------------------------------------------------------

// Configuration group used to manage test dependencies
// (when test classes depend on test classes from another project)
val tests = configurations.create("tests")

val testJar = tasks.register<Jar>("testJar") {
    archiveClassifier.set("test")
    from(sourceSets["test"].output)
}

artifacts {
    add(tests.name, testJar)
}
