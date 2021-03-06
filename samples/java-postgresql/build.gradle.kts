plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.sourcemuse.mongo")
    maven // Needed for Jitpack
}

baseProject()

repositories {
    jcenter()
}

dependencies {
    implementation(project(":modules:core"))
    implementation(project(":modules:driver-jdbc"))
    implementation("org.postgresql:postgresql:42.1.4")
}