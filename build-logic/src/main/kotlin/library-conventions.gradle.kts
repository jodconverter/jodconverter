plugins {
    id("java-conventions")
    id("java-library")
    id("publish-conventions")
    id("distribution")
}

afterEvaluate {
    extensions.configure<DistributionContainer> {
        getByName("main") {
            distributionBaseName.set(project.name)
            contents {
                from(tasks.named("jar"))
                from(tasks.named("sourcesJar"))
                from(tasks.named("plainJavadocJar"))
            }
        }
    }
}