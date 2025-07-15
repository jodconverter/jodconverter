// Follow https://github.com/gradle/gradle/issues/15383 to fix the way we access "libs"
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Plugins

plugins {
    java
    pmd
    checkstyle
    jacoco
    id("com.diffplug.spotless") apply false
    id("com.netflix.nebula.integtest") apply false
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Dependencies

repositories {
    mavenCentral()
}

dependencies {

    implementation(platform(libs.spring.boot.dependencies))
    compileOnly(libs.checker.qual)

    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockito.core)

    // Required for Mockito to avoid NullPointerException
    // Mockito disclaimer:
    // You are seeing this disclaimer because Mockito is configured to create inlined mocks.
    testRuntimeOnly(libs.checker.qual)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Compile

val javaVersionStr = libs.versions.java.get()
val javaVersion = when (javaVersionStr) {
    "1.8" -> JavaVersion.VERSION_1_8
    "11" -> JavaVersion.VERSION_11
    "17" -> JavaVersion.VERSION_17
    "21" -> JavaVersion.VERSION_21
    else -> throw GradleException("Unsupported Java version in libs.versions.toml: $javaVersionStr")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion.majorVersion)
    }
}

if (JavaVersion.current() < javaVersion) {
    throw GradleException("This build must be run with at least Java $javaVersion.")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
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
tasks.named<Checkstyle>("checkstyleTest").configure { isEnabled = false }
tasks.named<Checkstyle>("checkstyleIntegTest").configure { isEnabled = false }

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
        // Since we are running Spotless on JVM 8 (must support JVM 8), we are limited to google-java-format 1.7.
        // Remove the version when we set the minimal JVM to 11.
        googleJavaFormat("1.7") // Java 8 compatible

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
        includeEngines("junit-jupiter", "junit-vintage")
    }
    failFast = true
    testLogging.showStandardStreams = true
}

tasks.named<Test>("integrationTest") {
    jvmArgs = defaultJvmArgs
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-vintage")
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

tasks.withType<Jar>().configureEach {
    from(rootProject.layout.projectDirectory.file("LICENSE")) {
        into("META-INF")
    }
}

tasks.named<Jar>("jar") {
    doFirst {
        manifest {
            val moduleName = if (project.hasProperty("moduleName")) {
                project.property("moduleName").toString()
            } else {
                project.name
            }

            attributes(
                mapOf(
                    "Automatic-Module-Name" to project.name.replace("-", "."),
                    "Build-Jdk-Spec" to javaVersionStr,
                    "Built-By" to "JODConverter",
                    "Bundle-License" to "https://github.com/jodconverter/jodconverter/wiki/LICENSE",
                    "Bundle-Vendor" to "JODConverter",
                    "Bundle-DocURL" to "https://github.com/jodconverter/jodconverter/wiki",
                    "Implementation-Title" to moduleName,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "JODConverter Team",
                    "Implementation-Vendor-Id" to "org.jodconverter",
                    "Implementation-Url" to "https://github.com/jodconverter/jodconverter",
                    "Specification-Title" to moduleName,
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "JODConverter Team",
                    "Provider" to "Gradle ${gradle.gradleVersion}"
                )
            )
        }
    }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Documentation

tasks.named<Javadoc>("javadoc") {
    isFailOnError = false

    (options as StandardJavadocDocletOptions).apply {
        bottom =
            "Copyright &#169; 2022 - present; <a href=\"https://github.com/jodconverter\">JODConverter</a>. All rights reserved."
        charSet = "UTF-8"
        docEncoding = "UTF-8"
        encoding = "UTF-8"
        memberLevel = JavadocMemberLevel.PROTECTED
        source = javaVersionStr

        links(
            "https://docs.oracle.com/javase/8/docs/api/",
            "https://api.libreoffice.org/docs/java/ref/",
            "https://commons.apache.org/proper/commons-lang/apidocs/",
            "https://docs.spring.io/spring-boot/docs/${libs.versions.spring.boot.get()}/api/"
        )

        addBooleanOption("Xdoclint:none", true)
    }

    doFirst {
        val moduleName = if (project.hasProperty("moduleName")) {
            project.property("moduleName").toString()
        } else {
            project.name
        }

        (options as StandardJavadocDocletOptions).apply {
            windowTitle = "$moduleName API Documentation"
            docTitle = "$moduleName ${project.version} API Documentation"
            header = "$moduleName ${project.version} API"
        }
    }
}


