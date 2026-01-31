rootProject.name = "KOSP"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("common")
include("backend")
include("harvester")
include("infra:rabbitmq")
include("api-service")
include("challenge-service")
include("notification-service")
