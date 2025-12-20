# Plan: Spring Modulith Migration

**Status**: Planned
**Date**: 2025-12-21
**Goal**: Transition from a Layered Architecture to a Modular Monolith to enforce Bounded Contexts and improve system maintainability.

## Phase 1: Preparation & Dependencies
- [ ] **Add Dependencies**: Update `build.gradle.kts` to include Spring Modulith BOM and starters.
    ```kotlin
    implementation(platform("org.springframework.modulith:spring-modulith-bom:1.1.0")) // Check for latest version compatible with Boot 4.x
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    ```

## Phase 2: Structural Refactoring (Colocation)
Move classes to prioritize **Domain Cohesion** over **Layer separation**.

- [ ] **Member Module**:
    - Move `src/main/java/org/veri/be/api/personal/MemberController.java` -> `src/main/java/org/veri/be/member/MemberController.java`
    - Flatten `src/main/java/org/veri/be/domain/member/*` into `src/main/java/org/veri/be/member/`
- [ ] **Auth Module**:
    - Move `src/main/java/org/veri/be/api/common/AuthController.java` -> `src/main/java/org/veri/be/auth/AuthController.java`
    - Consolidate `domain/auth` content.
- [ ] **Card Module**:
    - Move `CardController.java` -> `org.veri.be.card`
    - Move `SocialCardController.java` -> `org.veri.be.card` (Review: Should this be in a separate `feed` module or stay in `card`?)
- [ ] **Book Module**:
    - Move `BookshelfController.java` -> `org.veri.be.book`
- [ ] **Post Module**:
    - Move `PostController.java` -> `org.veri.be.post`
- [ ] **Comment Module**:
    - Move `CommentController.java` -> `org.veri.be.comment`
- [ ] **Cleanup**: Remove empty `api` and `domain` packages after migration.

## Phase 3: Boundary Enforcement (The "Hidden" Power)
- [ ] **Hide Internals**:
    - Change `public` modifiers to `package-private` (default) for:
        - Repositories (e.g., `MemberRepository`)
        - Internal Services (those not accessed by other modules)
        - DTOs used only within the module.
    - **Keep Public**:
        - `Controller` (Web Interface)
        - `Service` (only methods required by other modules, if any)
        - `Events` (for inter-module communication)

## Phase 4: Verification
- [ ] **Architecture Test**: Create a test to verify module rules.
    ```java
    @ApplicationModuleTest
    class ModulithArchitectureTest {
        @Test
        void verifyModularity(ApplicationModules modules) {
            modules.verify(); // Checks for cyclic dependencies and allowed access
        }
    }
    ```
- [ ] **Documentation**: Run tests to generate C4 component diagrams (Living Documentation).

## History
- **2025-12-21**: Moved plan into **.agents/work** structure.
