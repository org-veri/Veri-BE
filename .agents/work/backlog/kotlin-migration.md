# Plan: Kotlin Migration Strategy

**Status**: Draft
**Date**: 2025-12-21
**Goal**: Gradual migration from Java 25 to Kotlin to leverage strict null-safety and concise syntax while maintaining interoperability.

## Phase 1: Infrastructure & Configuration
- [ ] **Gradle Setup**:
    - Add Kotlin JVM plugin: `kotlin("jvm") version "2.1.0"` (or latest).
    - Add Spring Kotlin support: `kotlin("plugin.spring")` (automatically opens classes/methods for Spring proxies).
    - Add JPA Kotlin support: `kotlin("plugin.jpa")` (generates no-arg constructors).
- [ ] **Dependency Management**:
    - Add `implementation("com.fasterxml.jackson.module:jackson-module-kotlin")` for JSON serialization.
    - Add `implementation("org.jetbrains.kotlin:kotlin-reflect")`.

## Phase 2: "Test First" Migration (Low Risk)
Start writing tests in Kotlin. This allows the team to learn syntax without risking production code.

- [ ] **Test Framework**:
    - Introduce **Kotest** or use **JUnit 5 + MockK** (Kotlin-native mocking library).
    - *Recommendation*: Stick to JUnit 5 but switch AssertJ to **Kotest Assertions** or **Strikt** for better DSL.
- [ ] **Action**:
    - Write *new* tests in `src/test/kotlin`.
    - Convert `IntegrationTestSupport` to Kotlin to utilize Default Arguments (eliminating the need for Builder patterns in tests).

## Phase 3: DTOs & VOs (High ROI)
Convert boilerplate-heavy Java classes to Kotlin Data Classes.

- [ ] **Migration Targets**:
    - Request/Response DTOs.
    - Value Objects (Embeddables).
- [ ] **Benefit**: Eliminate getters, setters, `equals()`, `hashCode()`, and `toString()`.

## Phase 4: Business Logic & Entities
- [ ] **Entities**:
    - *Caution*: JPA Entities in Kotlin require care.
    - Use `plugin.jpa` for no-arg constructors.
    - Avoid `data class` for Entities (issues with `equals`/`hashCode` and Lazy Loading). Use standard `class`.
- [ ] **Services/Controllers**:
    - Convert incrementally.
    - Utilize Kotlin's concise syntax for dependency injection (primary constructor).

## Compatibility Check
- **Java 25 vs Kotlin**: Kotlin runs on the JVM. Ensure the Kotlin compiler `jvmTarget` is set to `21` (LTS) or aligns with the runtime. Some Java 25 preview features might not have direct Kotlin equivalents yet, but interoperability is generally seamless.

## History
- **2025-12-21**: Moved plan into **.agents/work** structure.
