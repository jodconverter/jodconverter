description = "Client command line tool module of the Java OpenDocument Converter (JODConverter) project."

// Custom project metadata (optional, useful for publishing)
ext["moduleName"] = "JODConverter Cli"
ext["moduleDescription"] = description!!

plugins {
    id("java-conventions")

    // Create an executable for the client module
    application
}

dependencies {
    implementation(project(":jodconverter-local"))
    implementation(project(":jodconverter-remote"))

    implementation(libs.commons.cli)
    implementation(libs.commons.io)
    implementation(libs.gson)
    implementation(libs.snakeyaml.engine)

    runtimeOnly(libs.bundles.log4j) // Runtime so it is included in the distribution

    testImplementation(libs.spring.test)
    testImplementation(libs.wiremock)
}

application {
    mainClass.set("org.jodconverter.cli.Convert")

    // Copy the conf folder into the distribution
    applicationDistribution.from("conf") {
        into("conf")
    }

    // Copy the README
    applicationDistribution.from("README.txt") {
        into("")
    }

    // The license and the copyright notice, at the root of the distribution
    applicationDistribution.from(rootProject.layout.projectDirectory.files("LICENSE", "NOTICE")) {
        into("")
    }

    // use the log4j2.xml from the configuration directory
    applicationDefaultJvmArgs = listOf("-Dlog4j2.configurationFile=MY_APP_HOME/conf/log4j2.xml")
}

// Customize start scripts to replace MY_APP_HOME with APP_HOME.
// The unix script does not expand variables in DEFAULT_JVM_OPTS, which is single-quoted: the
// quotes are closed around "$APP_HOME" so it is expanded when DEFAULT_JVM_OPTS is assigned.
tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        unixScript.writeText(unixScript.readText().replace("MY_APP_HOME", "'\"\$APP_HOME\"'"))
        windowsScript.writeText(windowsScript.readText().replace("MY_APP_HOME", "%APP_HOME%"))
    }
}
