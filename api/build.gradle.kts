plugins {
    id("proxy-build")
    id("proxy-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://maven.jwall64.dev/")
    mavenLocal()
}

dependencies {

    api("dev.jwall64:midnightcfg-api:2.0.0-SNAPSHOT")
    api("dev.jwall64:midnightcfg-codec-json:2.0.0-SNAPSHOT")
    api("dev.jwall64:midnightlib:1.3.1-SNAPSHOT")

    api("dev.jwall64:midnightcore-common:2.0.0-SNAPSHOT")

    api("io.netty:netty-codec:4.1.105.Final")
    api("io.netty:netty-buffer:4.1.105.Final")

    api("org.slf4j:slf4j-api:2.0.9")

    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.test {
    useJUnitPlatform()
}

