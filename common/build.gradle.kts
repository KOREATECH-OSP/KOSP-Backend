plugins {
    `java-library`
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    api(libs.spring.boot.starter.data.jpa)
    api(libs.spring.boot.starter.data.mongodb)
    api(libs.spring.boot.starter.data.redis)
    api(libs.spring.boot.starter.validation)
    api(libs.spring.boot.starter.security)
    api(libs.spring.security.crypto)

    api(libs.lombok)
    annotationProcessor(libs.lombok)
}
