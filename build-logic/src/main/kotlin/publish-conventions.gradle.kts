plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(
        project.group as String,
        project.name,
        project.version as String
    )

    pom {
        name.set(project.findProperty("moduleName") as String? ?: project.name)
        description.set(project.findProperty("moduleDescription") as String? ?: project.description)
        inceptionYear.set("2022")
        url.set("https://github.com/jodconverter/jodconverter/")
        organization {
            name.set("JODConverter")
            url.set("https://github.com/jodconverter/")
        }
        scm {
            url.set("https://github.com/jodconverter/jodconverter/")
            connection.set("scm:git:https://github.com/jodconverter/jodconverter.git")
            developerConnection.set("scm:git:git@https://github.com/jodconverter/jodconverter.git")
        }
        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/jodconverter/jodconverter/issues")
        }
        developers {
            developer {
                id.set("sbraconnier")
                name.set("Simon Braconnier")
                email.set("simonbraconnier@gmail.com")
                url.set("https://github.com/sbraconnier/")
                organization.set("JODConverter")
                organizationUrl.set("https://github.com/jodconverter/")
                roles.set(listOf("Project-Administrator", "Developer"))
                timezone.set("-5")
            }
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}