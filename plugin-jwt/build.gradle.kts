import buildlogic.Utils

plugins {
    id("build.library")
}

Utils.setupResources(project, rootProject, "plugin.json")

dependencies {
    compileOnly(libs.jetbrains.annotations)
    implementation(project(":api"))
    implementation(libs.jwtutil)
}

val copyOutputTask = tasks.register<Copy>("copyOutputFiles") {

    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile)

    val output = rootDir.resolve("build").resolve("plugins")

    into(output)
}

tasks.build {
    dependsOn(copyOutputTask)
}
