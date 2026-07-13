plugins {
    id("org.gradle.toolchains.foojay-resolver") version "1.0.0"
}

toolchainManagement {
    jvm {
        javaRepositories {
            repository("foojay") {
                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
            }
        }
    }
}

rootProject.name = "KOSP"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("common")
include("backend")
include("harvester")
include("rabbitmq")
include("challenge-service")
include("flyway")
