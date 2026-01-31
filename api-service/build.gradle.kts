plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":infra:rabbitmq"))
    implementation(project(":backend"))
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.amqp)
    
    implementation(libs.postgresql)
    implementation(libs.flyway.database.postgresql)
    
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    
    testImplementation(libs.spring.boot.starter.test)
}

tasks {
    bootJar { enabled = true }
    jar { enabled = false }
}
