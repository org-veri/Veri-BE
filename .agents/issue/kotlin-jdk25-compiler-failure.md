# Issue: Kotlin Compiler Fails on JDK 25

**Severity**: High
**Status**: Open
**Date**: 2025-12-30

## Description
- **Problem**: Kotlin test compilation fails when Gradle runs under **JDK 25**.
- **Impact**: **:tests:compileTestKotlin** cannot complete, which blocks Modulith verification tests.

## Affected Files
- **Build configuration**:
  ```
  build.gradle.kts
  gradle.properties
  tests/build.gradle.kts
  ```

## Findings
- **Command**:
  ```
  ./gradlew :tests:test --tests "org.veri.be.modulith.ModulithArchitectureTest"
  ```
- **Error**:
  ```
  e: java.lang.IllegalArgumentException: 25
  ...
  Execution failed for task ':tests:compileTestKotlin'.
  ```
- **Observation**: Kotlin compiler does not recognize **JavaVersion 25**, even with toolchain and validation overrides.

## Recommendation
- **Short-term**: Run Gradle with **JDK 21** (Kotlin-compatible) and keep Java compilation on **JDK 25** via toolchains.
- **Long-term**: Track Kotlin plugin support for **JDK 25**, then remove the workaround once compatible.
