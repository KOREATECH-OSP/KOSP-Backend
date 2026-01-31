plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks {
    bootJar { enabled = false }
    jar { enabled = true }
}
