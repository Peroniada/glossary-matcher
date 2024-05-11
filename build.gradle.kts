plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotlinx_io_version: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:$kotlinx_io_version")
    implementation("org.jetbrains.kotlinx:kotlinx-io-bytestring:$kotlinx_io_version")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}