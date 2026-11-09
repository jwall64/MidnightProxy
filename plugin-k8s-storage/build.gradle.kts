import buildlogic.Utils

plugins {
    id("build.library")
    id("build.shadow")
}

Utils.setupResources(project, rootProject, "plugin.json")

dependencies {
    compileOnly(libs.jetbrains.annotations)
    implementation(project(":api"))

    implementation(libs.midnight.cfg.gson)
    implementation(libs.kubernetes.client)
    implementation(libs.kubernetes.client.extended)

    shadow(libs.midnight.cfg.gson)
    shadow(libs.kubernetes.client)
    shadow(libs.kubernetes.client.extended)
}

val copyOutputTask = tasks.register<Copy>("copyOutputFiles") {

    dependsOn(tasks.jar)
    from(tasks.shadowJar.get().archiveFile)

    val output = rootDir.resolve("build").resolve("plugins")

    into(output)
}

tasks.build {
    dependsOn(copyOutputTask)
}
