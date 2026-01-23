plugins {
    java
    alias(libs.plugins.org.springframework.boot) apply false
    alias(libs.plugins.io.spring.dependency.management) apply false
}

allprojects {
    group = "io.swkoreatech.kosp"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

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
}
