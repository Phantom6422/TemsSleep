plugins {
    base
}

defaultTasks("shadowJar")

version = providers.gradleProperty("buildVersion").orElse("6.0.3").get()
group = "me.mrgeneralq"

subprojects {
    version = rootProject.version
    group = rootProject.group

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://jitpack.io")
    }

    plugins.withType<JavaPlugin> {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
