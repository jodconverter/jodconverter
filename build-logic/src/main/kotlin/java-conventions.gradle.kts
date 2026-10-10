// Follow https://github.com/gradle/gradle/issues/15383 to fix the way we access "libs"
import org.gradle.accessors.dm.LibrariesForLibs

//
// Variables
//

val libs = the<LibrariesForLibs>()

val moduleName = if (project.hasProperty("moduleName")) {
    project.property("moduleName").toString()
} else {
    project.name
}

val javaVersionStr = libs.versions.java.get()
val javaVersion = when (javaVersionStr) {
    "1.8" -> JavaVersion.VERSION_1_8
    "11" -> JavaVersion.VERSION_11
    "17" -> JavaVersion.VERSION_17
    "21" -> JavaVersion.VERSION_21
    else -> throw GradleException("Unsupported Java version in libs.versions.toml: $javaVersionStr")
}

val charset = "UTF-8"

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Plugins

plugins {
    java
    pmd
    checkstyle
    jacoco
    id("com.diffplug.spotless")
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Dependencies

repositories {
    mavenCentral()
}

dependencies {

    compileOnly(libs.checker.qual)

    // Test dependencies only: unlike a BOM on implementation, it is not part of the published artifacts.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockito.core)

    // Required for Mockito to avoid NullPointerException
    // Mockito disclaimer:
    // You are seeing this disclaimer because Mockito is configured to create inlined mocks.
    testRuntimeOnly(libs.checker.qual)
    testRuntimeOnly(libs.junit.jupiter.engine)
    // Required since Gradle 9, which no longer provides it to the test tasks.
    testRuntimeOnly(libs.junit.platform.launcher)
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Compile

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion.majorVersion)
    }
}

if (JavaVersion.current() < javaVersion) {
    throw GradleException("This build must be run with at least Java $javaVersion.")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = charset
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:-options",
            "-Xlint:unchecked",
            "-Xlint:deprecation"
        )
    )
    options.isIncremental = true
    options.isFork = true
    //options.debug = true
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Pmd

pmd {
    toolVersion = libs.versions.pmd.get()
    ruleSetConfig = rootProject.resources.text.fromFile("ruleset.xml")
    isIgnoreFailures = true
    rulesMinimumPriority.set(5)
    ruleSets = listOf() // clears Gradle's default rulesets
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Checkstyle

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    config = rootProject.resources.text.fromFile("checkstyle.xml")
}

// Disable checkstyle for test code
tasks.withType<Checkstyle>()
    .matching { it.name == "checkstyleTest" || it.name == "checkstyleIntegTest" }
    .configureEach { isEnabled = false }

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Checkstyle

spotless {
    java {
        // Format code using google java format
        googleJavaFormat(libs.versions.google.java.format.get())

        // Import order
        importOrderFile("$rootDir/spotless.importorder")

        // Java Source Header File
        licenseHeaderFile("$rootDir/spotless.license.java")
    }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Test

val defaultJvmArgs = mutableListOf<String>()
// "JEP 403: Strongly Encapsulate JDK Internals" causes some tests to
// fail when they try to access internals (often via mocking libraries).
// We use `--add-opens` as a workaround for now.
//if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_16)) {
//    defaultJvmArgs.addAll(
//        listOf(
//            "--add-opens=java.base/java.io=ALL-UNNAMED",
//            "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED"
//        )
//    )
//}

tasks.named<Test>("test") {
    jvmArgs = defaultJvmArgs
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
    failFast = true
    testLogging.showStandardStreams = true
}

// Integration tests live in src/integTest. They need an office installation, so they run in their
// own task. They get the dependencies and the classes of the unit tests, since they reuse test helpers.
val integTest: SourceSet = sourceSets.create("integTest") {
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
}
configurations.named(integTest.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(integTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = integTest.output.classesDirs
    classpath = integTest.runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.named("check") {
    dependsOn(integrationTest)
}

integrationTest.configure {
    jvmArgs = defaultJvmArgs
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
    failFast = true
    testLogging.showStandardStreams = true

    systemProperty(
        "org.jodconverter.local.manager.templateProfileDir",
        project.findProperty("org.jodconverter.local.manager.templateProfileDir") ?: ""
    )
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Jacoco

tasks.named("check") {
    dependsOn("jacocoTestReport")
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Artifacts

// The license and the copyright notice go with every jar, as the license asks.
tasks.withType<Jar>().configureEach {
    from(rootProject.layout.projectDirectory.files("LICENSE", "NOTICE")) {
        into("META-INF")
    }
}

tasks.named<Jar>("jar") {
    // Resolved here, at configuration time: tasks must not access the project while they execute.
    val automaticModuleName = project.name.replace("-", ".")
    val projectVersion = project.version.toString()
    val gradleVersion = gradle.gradleVersion

    manifest {
        attributes(
            mapOf(
                "Automatic-Module-Name" to automaticModuleName,
                "Build-Jdk-Spec" to javaVersionStr,
                "Built-By" to "JODConverter",
                "Bundle-License" to "https://github.com/jodconverter/jodconverter/wiki/LICENSE",
                "Bundle-Vendor" to "JODConverter",
                "Bundle-DocURL" to "https://github.com/jodconverter/jodconverter/wiki",
                "Implementation-Title" to moduleName,
                "Implementation-Version" to projectVersion,
                "Implementation-Vendor" to "JODConverter Team",
                "Implementation-Vendor-Id" to "org.jodconverter",
                "Implementation-Url" to "https://github.com/jodconverter/jodconverter",
                "Specification-Title" to moduleName,
                "Specification-Version" to projectVersion,
                "Specification-Vendor" to "JODConverter Team",
                "Provider" to "Gradle $gradleVersion"
            )
        )
    }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Documentation

tasks.named<Javadoc>("javadoc") {
    isFailOnError = false

    (options as StandardJavadocDocletOptions).apply {
        bottom =
            "Copyright &#169; 2022 - present; <a href=\"https://github.com/jodconverter\">JODConverter</a>. All rights reserved."
        charSet = charset
        docEncoding = charset
        encoding = charset
        memberLevel = JavadocMemberLevel.PROTECTED
        source = javaVersionStr

        links(
            "https://docs.oracle.com/en/java/javase/17/docs/api/",
            "https://api.libreoffice.org/docs/java/ref/",
            "https://commons.apache.org/proper/commons-lang/apidocs/",
            "https://docs.spring.io/spring-boot/${libs.versions.spring.boot.get()}/api/java/"
        )

        addBooleanOption("Xdoclint:none", true)
    }

    // Resolved here, at configuration time: tasks must not access the project while they execute.
    val projectVersion = project.version.toString()
    (options as StandardJavadocDocletOptions).apply {
        windowTitle = "$moduleName API Documentation"
        docTitle = "$moduleName $projectVersion API Documentation"
        header = "$moduleName $projectVersion API"
    }
}


