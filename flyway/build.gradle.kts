plugins {
    `java-library`
}

dependencies {
    api(libs.flyway.database.postgresql)
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)
}
