description = """
    JODConverter automates conversions between office document formats
    using LibreOffice or Apache OpenOffice. It automates all conversions
    supported by the running instance of OpenOffice/LibreOffice.
""".trimIndent()

group = "org.jodconverter"
version = project.property("version") as String

plugins {
    jacoco
    distribution
    alias(libs.plugins.dependency.check)
    alias(libs.plugins.coveralls.jacoco)
}

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

// Run with: gradlew dependencyCheckAggregate
// The NVD API key is read from the "nvdApiKey" Gradle property (~/.gradle/gradle.properties)
// or from the NVD_API_KEY environment variable.
dependencyCheck {
    nvd {
        apiKey = providers.gradleProperty("nvdApiKey")
            .orElse(providers.environmentVariable("NVD_API_KEY"))
            .orNull
    }
}

val javadocAll = tasks.register<Javadoc>("javadocAll") {
    description = "Aggregates Javadoc API documentation of all libraries."
    group = "Documentation"
}

val jacocoRootReport = tasks.register<JacocoReport>("jacocoRootReport") {
    group = "verification"
    description = "Generates an aggregate Jacoco coverage report"
}

tasks.register("setVersion") {
    group = "Release"
    description = "Sets the version (-PnewVersion=X.Y.Z or X.Y.Z-SNAPSHOT) in gradle.properties and, for a release, in the documentation."

    val newVersion = providers.gradleProperty("newVersion")
    val propertiesFile = layout.projectDirectory.file("gradle.properties")
    // The pages that show the dependency coordinates of the released version.
    val docsFiles = listOf(
        "docs/getting-started/java-library/index.md",
        "docs/getting-started/libreoffice-online.md",
        "docs/getting-started/modules.md"
    ).map { layout.projectDirectory.file(it) }

    doLast {
        val version = newVersion.orNull
            ?: throw GradleException("Give the version: ./gradlew setVersion -PnewVersion=X.Y.Z")
        require(Regex("""\d+\.\d+\.\d+(-SNAPSHOT)?""").matches(version)) {
            "Invalid version '$version': expected X.Y.Z or X.Y.Z-SNAPSHOT"
        }
        val release = !version.endsWith("-SNAPSHOT")

        var properties = propertiesFile.asFile.readText()
        properties = properties.replace(Regex("""(?m)^version\s*=.*$"""), "version = $version")
        if (release) {
            properties = properties.replace(Regex("""(?m)^releasedVersion\s*=.*$"""), "releasedVersion = $version")
        }
        propertiesFile.asFile.writeText(properties)
        println("gradle.properties: version = $version")

        if (release) {
            // Every coordinate of a JODConverter artifact, whatever version it shows: a wrong version in a
            // snippet is corrected, not kept.
            val coordinates = listOf(
                Regex("""(org\.jodconverter:jodconverter-[\w-]+:)\d+\.\d+\.\d+"""),
                Regex("""(<artifactId>jodconverter-[\w-]+</artifactId>\s*<version>)\d+\.\d+\.\d+"""),
                Regex("""(central\.sonatype\.com/artifact/org\.jodconverter/jodconverter-[\w-]+/)\d+\.\d+\.\d+""")
            )
            docsFiles.forEach { file ->
                var text = file.asFile.readText()
                var count = 0
                coordinates.forEach { regex ->
                    text = regex.replace(text) { match ->
                        if (match.value.endsWith(version)) match.value else {
                            count++
                            match.groupValues[1] + version
                        }
                    }
                }
                if (count > 0) {
                    file.asFile.writeText(text)
                }
                println("${file.asFile.toRelativeString(projectDir)}: $count coordinate(s) set to $version")
            }
        }
    }
}

tasks.register("printConfigurations") {
    group = "Help"
    doLast {
        subprojects.forEach { p ->
            println(p.path)
            p.configurations.forEach { println("  | ${it.name}") }
            println()
        }
    }
}

gradle.projectsEvaluated {

    val javaProjects by lazy {
        allprojects.filter { it.plugins.hasPlugin("java-conventions") }
    }

    val libraryProjects = subprojects.filter {
        it.plugins.hasPlugin("library-conventions")
    }

    tasks.named<Zip>("distZip") {
        description = "Create full distribution zip"
        group = "Distribution"

        val allDistZips = javaProjects.mapNotNull { it.tasks.findByName("distZip") as? Zip }
        dependsOn(allDistZips)

        archiveBaseName.set(project.name)
        from(allDistZips.map { it.archiveFile.map { f -> f.asFile } })
    }

    // Sends the aggregate report to Coveralls. The plugin looks each source file of the report up in
    // these directories and sends its path relative to the repository root, which is what lets
    // Coveralls show the sources of a multi-module build (the GitHub action sends the bare package
    // paths of the JaCoCo report, which Coveralls cannot find).
    coverallsJacoco {
        reportPath = "build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml"
        reportSourceSets = javaProjects.map { it.layout.projectDirectory.dir("src/main/java").asFile }
        // -PcoverallsDryRun writes the request to build/coveralls/request.json instead of sending it.
        if (providers.gradleProperty("coverallsDryRun").isPresent) {
            dryRun = true
            coverallsRequest =
                layout.buildDirectory.file("coveralls/request.json").get().asFile.also { it.parentFile.mkdirs() }
        }
    }
    tasks.named("coverallsJacoco") {
        // The plugin expects the standard jacocoTestReport; it gets the aggregate one.
        dependsOn(jacocoRootReport)
    }

    javadocAll.configure {

        dependsOn(libraryProjects.mapNotNull { it.tasks.findByName("classes") })

        val allSources = files(libraryProjects.flatMap {
            it.extensions.getByType<JavaPluginExtension>()
                .sourceSets.getByName("main")
                .allJava
        })

        val allClasspaths = files(libraryProjects.flatMap {
            it.extensions.getByType<JavaPluginExtension>()
                .sourceSets.getByName("main")
                .compileClasspath
        })

        source(allSources)
        classpath = allClasspaths

        destinationDir = layout.buildDirectory.dir("docs/javadoc").get().asFile

        val charset = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            windowTitle = "JODConverter API Documentation"
            docTitle = "JODConverter $version API Documentation"
            header = "JODConverter $version API"
            bottom =
                "Copyright © 2022 - present; <a href=\"https://github.com/jodconverter/jodconverter\">JODConverter</a>. All rights reserved."
            charSet = charset
            docEncoding = charset
            encoding = charset
            memberLevel = JavadocMemberLevel.PROTECTED
            source = libs.versions.java.get()
            links(
                "https://docs.oracle.com/en/java/javase/17/docs/api/",
                "https://api.libreoffice.org/docs/java/ref/",
                "https://commons.apache.org/proper/commons-lang/javadocs/api-release/",
                "https://docs.spring.io/spring-boot/${libs.versions.spring.boot.get()}/api/java/"
            )
            addBooleanOption("Xdoclint:none")
        }

        inputs.files(allSources)
        outputs.dir(layout.buildDirectory.dir("docs/javadoc"))
    }


    // The jacocoRootReport must be registered here since it depends on all subprojects
    // having a test task, so the subprojects must be fully configured to find them.
    jacocoRootReport.configure {

        dependsOn(javaProjects.flatMap {
            listOf(
                it.tasks.named("test"),
                it.tasks.named("integrationTest")
            )
        })

        //val classDirs = files(projects.flatMap {
        //    listOf(
        //        it.layout.buildDirectory.dir("classes/java/main"),
        //        it.layout.buildDirectory.dir("classes/kotlin/main")
        //    )
        //})
        val classDirs = files(javaProjects.mapNotNull {
            it.extensions.findByType<SourceSetContainer>()?.getByName("main")?.output?.classesDirs
        })

        val sourceDirs = files(javaProjects.flatMap {
            listOf(
                it.layout.projectDirectory.dir("src/main/java"),
                //it.layout.projectDirectory.dir("src/main/kotlin")
            )
        })

        val execData = files(javaProjects.flatMap { project ->
            listOf(
                project.layout.buildDirectory.file("jacoco/test.exec").get().asFile,
                project.layout.buildDirectory.file("jacoco/integrationTest.exec").get().asFile
            ).filter { it.exists() }
        })

        sourceDirectories.setFrom(sourceDirs)
        classDirectories.setFrom(classDirs)
        executionData.setFrom(execData)

        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoRootHtml"))
        }
    }
}

