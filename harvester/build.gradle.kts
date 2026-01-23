plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }
