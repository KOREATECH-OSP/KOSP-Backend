plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":infra:rabbitmq"))
}

tasks {
    bootJar { enabled = false }
    jar { enabled = true }
}
