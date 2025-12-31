rootProject.name = "Veri-BE"

pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
            }
        }
    }
}

include(
    "clients:client-aws",
    "clients:client-ocr",
    "clients:client-search",
    "core:core-api",
    "core:core-app",
    "core:core-domain",  // v2.1: Pure domain module (enums merged from core-enum)
    "storage:db-core",
    "support:common",
    "support:logging",
    "support:monitoring",
    "tests"
)
