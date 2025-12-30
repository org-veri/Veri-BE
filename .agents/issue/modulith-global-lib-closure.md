# Issue: Modulith Global/Lib Closure Violations

**Severity**: Low
**Status**: Open
**Date**: 2025-12-30

## Description
Attempting to switch **global** and **lib** modules to **CLOSED** triggered Modulith violations, including a **cycle** between **global** and **lib** and multiple non-exposed type usages from other modules (e.g., **auth** depending on **global** DTOs and **lib** responses).

## Affected Files
- **core/core-api/src/main/java/org/veri/be/global/package-info.java**
- **core/core-api/src/main/java/org/veri/be/lib/package-info.java**
- **core/core-api/src/main/java/org/veri/be/global/auth/**
- **core/core-api/src/main/java/org/veri/be/lib/auth/**
- **core/core-api/src/main/java/org/veri/be/auth/**

## Findings
- **Cycle**: `global` -> `lib` -> `global` via **AuthErrorInfo**, **MemberGuard**, and **JwtService/JwtFilter** usage.
- **Non-exposed types**: `auth` and other modules rely on **global** DTOs and **lib** response/exception classes, which are not exposed when **global/lib** are **CLOSED**.
- **Current mitigation**: global/lib kept **OPEN** to keep Modulith verification passing.

## Recommendation
- Introduce **named interfaces** (`@NamedInterface`) to explicitly expose shared API packages.
- Refactor **JwtService/JwtFilter** to avoid `lib` -> `global` dependency where possible.
- Gradually migrate `auth` to depend on exposed interfaces or module-specific DTOs.
- Reattempt **CLOSED** transition after each refactor step.
