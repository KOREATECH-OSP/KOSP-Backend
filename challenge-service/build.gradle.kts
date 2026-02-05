plugins {
    java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":rabbitmq"))
    implementation(project(":flyway"))
    
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.security)
    
    implementation(libs.postgresql)
    
    implementation(libs.logback.slack.appender)
    implementation(libs.logstash.logback.encoder)
    
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.jackson.databind)
    testRuntimeOnly(libs.h2database)
}

tasks {
    bootJar { enabled = true }
    jar { enabled = false }
    
    test {
        useJUnitPlatform()
    }
    
    bootRun {
        systemProperties = System.getProperties().toMap() as Map<String, Any>
        environment = System.getenv()
    }
}
