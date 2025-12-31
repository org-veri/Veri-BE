// v2.1: Pure Domain Module - Zero Spring/JPA dependencies
// This module contains only domain models, value objects, and domain interfaces

dependencies {
    // Kotlin stdlib
    implementation(kotlin("stdlib"))

    // Validation for Value Objects (self-validating)
    implementation("jakarta.validation:jakarta.validation-api")

    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core")
}
