plugins {
	java
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

group = "kr.ac.koreatech.sw"

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
    implementation(libs.spring.boot.devtools)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.aop)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.boot.starter.batch)
    implementation(libs.mysql.connector.j)

    implementation(libs.jjwt.api)
    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.boot.starter.oauth2.client)

    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.thymeleaf.extras.springsecurity6)

    implementation(libs.awssdk.ses)

    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    runtimeOnly(libs.mysql.connector.j)
    runtimeOnly(libs.h2database)

    compileOnly(libs.lombok)

	annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
	testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.clean {
    delete(file("src/main/generated"))
}

tasks.withType<Test> {
	useJUnitPlatform()
}
