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

## Update
**Date**: 2025-12-30

### Findings
- Closing **global/lib** still triggers cycles involving **auth**, **global**, and **lib** due to `TokenBlacklistStore`, `JwtFilter`, and `TokenProvider` wiring.
- **global** depends on **auth-service** via `AuthConfig`, and **lib** depends on **auth-service** via `JwtFilter`, forming **auth -> global -> lib -> auth** cycles.
- **global** also participates in a **global -> member -> global** cycle through `MemberContext`/`MemberRepository` usage.

### Recommendation
- Keep **global/lib** as **OPEN** until auth-related dependencies are refactored (e.g., move blacklist into global or extract a global-owned interface).
- Revisit module boundaries after resolving auth-global-lib dependency direction.

## Update
**Date**: 2025-12-30

### Findings
- Closing **global** still produces cycles with **lib** (global uses lib exceptions/guards; lib uses global auth context/token) and with **member** (global auth context and OAuth2 DTOs reference member types).

### Recommendation
- Keep **global** **OPEN** while **lib** is closed.
- To close **global**, refactor auth context to avoid direct **member** entity/repository usage, or move those types behind an interface owned by **member**.
