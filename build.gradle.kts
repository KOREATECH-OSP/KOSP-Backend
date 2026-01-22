plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

group = "io.swkoreatech"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Core
    implementation(libs.spring.boot.starter.actuator)
    
    // Data
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.data.mongodb)
    
    // HTTP Client (GitHub API)
    implementation(libs.spring.boot.starter.webflux)
    
    // Batch
    implementation(libs.spring.boot.starter.batch)
    
    // Security (for encryption)
    implementation(libs.spring.security.crypto)
    
    // Database
    runtimeOnly(libs.mysql.connector.j)
    
    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    
    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.batch.test)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.h2database)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
