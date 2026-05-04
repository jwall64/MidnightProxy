import buildlogic.Utils

plugins {
    id("build.library")
}

Utils.setupResources(project, rootProject, "plugin.json")

dependencies {
    compileOnly(libs.jetbrains.annotations)
    implementation(project(":api"))
}
