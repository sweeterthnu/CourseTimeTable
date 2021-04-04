plugins {
    kotlin("jvm") version "1.4.32"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1")
    implementation("org.apache.poi:poi:5.0.0")
    implementation("org.apache.poi:poi-ooxml:5.0.0")
}
