import org.apache.tools.ant.taskdefs.condition.Os

description =
    "Module required in order to process local conversions for the Java OpenDocument Converter (JODConverter) project."

extra["moduleName"] = "JODConverter Local"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    api(project(":jodconverter-core"))

    // We cannot use LibreOffice libraries by default.
    // See:
    // https://github.com/jodconverter/jodconverter/issues/113
    if (project.hasProperty("useLibreOffice")) {
        api(libs.libreoffice)
    } else {
        api(libs.oo.juh)
        api(libs.oo.jurt)
        api(libs.oo.ridl)
        api(libs.oo.unoil)
    }

    implementation(libs.slf4j.api)

    testImplementation(libs.slf4j.log4j)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.spring.test)
    testImplementation(project(":jodconverter-core", configuration = "tests"))
}

// Integration test filtering
tasks.named<Test>("integrationTest").configure {
    filter {
        excludeTestsMatching("*.Stress*")
        excludeTestsMatching("*.Performance*")
        excludeTestsMatching("*.Sandbox*")

        // Exclude filter tests on macos. LibreOffice seems to crash frequently on macos. See:
        // https://ask.libreoffice.org/t/libreoffice-crashing-on-change-window-focus-macos-ventura/83415
        // https://ask.libreoffice.org/t/on-mac-libreoffice-keeps-crashing/74751/36?page=2
        //
        // Ignore those tests for now until LO is more stable on macos.
        if (Os.isFamily(Os.FAMILY_MAC)) {
            excludeTestsMatching("org.jodconverter.local.filter.*")
        }
    }
}
