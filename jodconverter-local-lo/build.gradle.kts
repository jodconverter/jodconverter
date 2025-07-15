description =
    "Module required in order to process local conversions for the Java OpenDocument Converter (JODConverter) project, using the LibreOffice libraries."

extra["moduleName"] = "JODConverter Local - LO"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
    id("publish-conventions")
}

dependencies {
    api(project(":jodconverter-local")) {
        exclude(group = "org.openoffice")
    }
    api(libs.libreoffice)
}