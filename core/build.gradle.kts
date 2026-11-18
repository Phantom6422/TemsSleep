plugins {
    `java-library`
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.bstats)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.gsit)
    compileOnly(libs.supervanish)
    compileOnly(libs.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.paper.api)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.processResources {
    filesMatching("config.yml") {
        expand("version" to project.version)
    }
}

java {
    withSourcesJar()
}
