plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.mongodb)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
