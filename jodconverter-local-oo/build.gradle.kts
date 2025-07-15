description =
    "Module required in order to process local conversions for the Java OpenDocument Converter (JODConverter) project, using the OpenOffice libraries."

extra["moduleName"] = "JODConverter Local - OO"
extra["moduleDescription"] = description

plugins {
    id("library-conventions")
}

dependencies {
    api(project(":jodconverter-local")) {
        exclude(group = "org.libreoffice")
    }
    api(libs.oo.juh)
    api(libs.oo.jurt)
    api(libs.oo.ridl)
    api(libs.oo.unoil)
}
