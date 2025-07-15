plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // Follow https://github.com/gradle/gradle/issues/15383 to fix the way we access "libs" in precompiled-scripts.
    // The following dependency is required in order to make the libs available in precompiled scripts.
    //println("from build-conventions build script: ${libs.versions.oie.get()}")
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // Proguard
    implementation(libs.spotless)
    implementation(libs.nebula.integtest)
    implementation(libs.maven.publish)
}