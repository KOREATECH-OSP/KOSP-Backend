plugins {
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(projects.common)
    implementation(projects.rabbitmq)

    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.data.mongodb)

    implementation(libs.spring.boot.starter.webflux)

    implementation(libs.spring.boot.starter.batch)
    
    implementation(libs.spring.boot.starter.amqp)

    implementation(libs.spring.security.crypto)

    implementation(libs.postgresql)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.batch.test)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.h2database)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
