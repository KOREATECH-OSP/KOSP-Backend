plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":infra:rabbitmq"))
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.jpa)
    
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    
    runtimeOnly(libs.postgresql)
    
    testImplementation(libs.spring.boot.starter.test)
}

tasks {
    bootJar { enabled = true }
    jar { enabled = false }
}
