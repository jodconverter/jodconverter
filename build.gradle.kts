description = """
    JODConverter automates conversions between office document formats
    using LibreOffice or Apache OpenOffice. It automates all conversions
    supported by the running instance of OpenOffice/LibreOffice.
""".trimIndent()

group = "org.jodconverter"
version = "4.4.10-SNAPSHOT"

plugins {
    jacoco
    distribution
    alias(libs.plugins.coveralls)
}

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

val javadocAll by tasks.registering(Javadoc::class) {
    description = "Aggregates Javadoc API documentation of all libraries."
    group = "Documentation"
}

val jacocoRootReport by tasks.registering(JacocoReport::class) {
    group = "verification"
    description = "Generates an aggregate Jacoco coverage report"
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

    coveralls {
        sourceDirs = libraryProjects.flatMap {
            it.extensions.getByType<JavaPluginExtension>()
                .sourceSets.getByName("main")
                .allSource.srcDirs
        }.map { it.absolutePath }
        jacocoReportPath = layout.buildDirectory
            .file("reports/jacoco/jacocoRootReport/jacocoRootReport.xml")
            .get()
            .asFile
            .absolutePath
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

        setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)

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
            source = "8"
            links(
                "https://docs.oracle.com/javase/8/docs/api/",
                "https://api.libreoffice.org/docs/java/ref/",
                "https://commons.apache.org/proper/commons-lang/javadocs/api-release/",
                "https://docs.spring.io/spring-boot/docs/${libs.versions.spring.boot.get()}/api/"
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

