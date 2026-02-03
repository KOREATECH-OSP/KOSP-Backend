# Stage 1: Build
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY gradle.properties .
COPY gradle/libs.versions.toml gradle/

# Copy all module build files
COPY build.gradle.kts .
COPY common/build.gradle.kts common/
COPY backend/build.gradle.kts backend/
COPY harvester/build.gradle.kts harvester/
COPY challenge-service/build.gradle.kts challenge-service/
COPY rabbitmq/build.gradle.kts rabbitmq/
COPY flyway/build.gradle.kts flyway/

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY common/src common/src
COPY challenge-service/src challenge-service/src
COPY rabbitmq/src rabbitmq/src
COPY flyway/src flyway/src

# Build the application
RUN gradle :challenge-service:build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from build stage
COPY --from=build /app/challenge-service/build/libs/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]
