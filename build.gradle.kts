plugins {
    java
}

val basePackage = "it.poliba.sisinflab.owl"
val mainClass = "$basePackage.owltool.Main"

group = basePackage
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sourceforge.owlapi", "owlapi-distribution", "5.1.17")
    implementation("com.beust", "jcommander", "1.81")
    implementation("org.slf4j", "slf4j-nop", "1.7.30")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val fatJar = task("fatJar", type = Jar::class) {
    group = BasePlugin.BUILD_GROUP
    description = "Assembles a jar archive containing all classes, including those of dependencies."
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Main-Class"] = mainClass
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks.build {
    dependsOn(fatJar)
}

tasks.wrapper {
    gradleVersion = "7.0.2"
}
