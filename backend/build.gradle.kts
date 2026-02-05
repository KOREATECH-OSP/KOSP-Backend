plugins {
	jacoco
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
}

dependencies {
    implementation(projects.common)
    implementation(projects.rabbitmq)
    implementation(projects.flyway)

    implementation(libs.spring.boot.devtools)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.aop)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.session.data.redis)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.spring.boot.starter.webflux) // GitHub API Client
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.boot.starter.batch)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.postgresql)

    implementation(libs.jjwt.api)
    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.boot.starter.oauth2.client)

    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.thymeleaf.extras.springsecurity6)

    implementation(libs.awssdk.ses)
    implementation(libs.awssdk.s3)

    implementation(libs.jsoup)  // HTML 파싱용

    implementation("com.github.maricn:logback-slack-appender:1.6.1")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    implementation(libs.rsql.jpa.spring.boot.starter)

    implementation(libs.flyway.database.postgresql)

    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2database)

    compileOnly(libs.lombok)

	annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation("io.projectreactor:reactor-test")
	testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    bootJar { enabled = true }
    jar { enabled = true }
}

tasks.clean {
    delete(file("src/main/generated"))
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

jacoco {
	toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
		html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
	}
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude(
					// DTOs and Requests/Responses
					"**/dto/**",
					"**/request/**",
					"**/response/**",
					// Entities and Models (simple data classes)
					"**/model/**",
					"**/document/**",
					// Configuration classes
					"**/config/**",
					"**/global/config/**",
					// Deprecated code
					"**/deprecated/**",
					// API interfaces (Swagger)
					"**/api/**",
					// Controllers (tested via integration tests)
					"**/controller/**",
					// Event classes
					"**/event/**",
					"**/eventlistener/**",
					// Exceptions
					"**/exception/**",
					// Initializers and batch jobs
					"**/initializer/**",
					"**/batch/**",
					// Infrastructure (external API clients)
					"**/infra/**",
					"**/client/**",
					// Application entry point
					"**/KospApplication*"
				)
			}
		})
	)
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			limit {
				minimum = "0.90".toBigDecimal()
			}
		}
	}
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude(
					"**/dto/**",
					"**/request/**",
					"**/response/**",
					"**/model/**",
					"**/document/**",
					"**/config/**",
					"**/global/config/**",
					"**/deprecated/**",
					"**/api/**",
					"**/controller/**",
					"**/event/**",
					"**/eventlistener/**",
					"**/exception/**",
					"**/initializer/**",
					"**/batch/**",
					"**/infra/**",
					"**/client/**",
					"**/KospApplication*"
				)
			}
		})
	)
}
