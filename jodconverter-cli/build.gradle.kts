description = "Client command line tool module of the Java OpenDocument Converter (JODConverter) project."

// Custom project metadata (optional, useful for publishing)
ext["moduleName"] = "JODConverter Cli"
ext["moduleDescription"] = description!!

plugins {
    id("java-conventions")

    // Create an executable for the client module
    // todo: include documentation with the distribution
    // See https://docs.gradle.org/current/userguide/application_plugin.html
    application
}

dependencies {
    implementation(project(":jodconverter-local"))
    implementation(project(":jodconverter-remote"))

    implementation(libs.commons.cli)
    implementation(libs.commons.io)
    implementation(libs.spring.core)
    implementation(libs.spring.context)

    runtimeOnly(libs.slf4j.log4j) // Runtime so it is included in the distribution

    testImplementation(libs.mockito.inline)
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

    // use the log4j.properties from the configuration directory
    applicationDefaultJvmArgs = listOf("-Dlog4j.configuration=file:MY_APP_HOME/conf/log4j.properties")
}

// Customize start scripts to replace MY_APP_HOME with APP_HOME
tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        unixScript.writeText(unixScript.readText().replace("MY_APP_HOME", "\$APP_HOME"))
        windowsScript.writeText(windowsScript.readText().replace("MY_APP_HOME", "%APP_HOME%"))
    }
}
