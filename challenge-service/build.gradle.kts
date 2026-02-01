plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":infra:rabbitmq"))
    implementation(project(":backend"))
    
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.security)
    
    implementation(libs.postgresql)
    
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
}

tasks {
    bootJar { enabled = true }
    jar { enabled = false }
    
    bootRun {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        environment = System.getenv()
    }
}
