import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.serialization)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "org.timestamp"
version = "2.0.0"

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.jpa)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.kotlin.reflect)
    implementation(libs.postgresql)
    implementation(libs.firebase.admin)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.graphhopper.core)
    implementation(project(":lib"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit5)
    testRuntimeOnly(libs.junit5.platform.launch)
}

tasks.named<BootJar>("bootJar") {
    archiveFileName.set("timestamp.jar")
}

springBoot {
    mainClass.set("org.timestamp.backend.BackendApplicationKt")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}