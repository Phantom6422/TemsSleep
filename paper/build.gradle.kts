import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.annotations)

    implementation(project(":core"))
    implementation(libs.bstats)
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.withType<ShadowJar> {
    archiveFileName.set("TemsSleep-${project.version}.jar")
    archiveClassifier.set("")
    mergeServiceFiles()

    exclude("module-info.class")
    exclude("META-INF/versions/**")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

    relocate("org.bstats", "me.mrgeneralq.core.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
