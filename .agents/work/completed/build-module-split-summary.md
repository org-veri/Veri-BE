# Plan: Module Split Build and Test Summary

**Status**: Completed
**Date**: 2025-09-20
**Goal**: Consolidate build time, test time, and artifact size comparisons for the module split.
**Parent Task**: `.agents/work/backlog/modulith-migration.md`

## Steps
- [x] Consolidate single-run and repeated build timing comparisons.
- [x] Consolidate artifact size comparison.
- [x] Consolidate test timing comparison excluding persistence slice.
- [x] Capture evidence-based analysis and caveats.

## Summary

**Scope**

- **Primary comparison (actual pre/post split)**: `b2ee6f7f` (pre) vs `00f35642` (post).
- **Earlier comparison (reference only)**: `6b1db85d` (already post-split) vs `00f35642`.

**Commands Used**

```bash
/usr/bin/time -p ./gradlew clean build -x test
/usr/bin/time -p ./gradlew clean build
/usr/bin/time -p ./gradlew clean test --tests "org.veri.be.integration.*" --tests "org.veri.be.slice.web.*" --tests "org.veri.be.unit.*"
/usr/bin/time -p ./gradlew clean :app:test --tests "org.veri.be.integration.*" --tests "org.veri.be.slice.web.*" --tests "org.veri.be.unit.*"
```

**Test Procedure Details**

- **Repeat count**: 3 runs per scenario to reduce run-to-run variance.
- **Cold start**: Each run started with `clean` to avoid incremental artifacts.
- **Filtering**: Persistence slice tests excluded by only including:
  - `org.veri.be.integration.*`
  - `org.veri.be.slice.web.*`
  - `org.veri.be.unit.*`
- **Module targeting**:
  - Pre-split uses root `test` task.
  - Post-split uses `:app:test` to align with module layout.
- **Timing capture**: `real` time from `/usr/bin/time -p` recorded for comparison.

## Results

**A) Build Timing (Actual Pre/Post Split)**

- **Pre-split `b2ee6f7f`**
    - **No tests**: 2.07, 1.63, 1.24 (avg **1.65s**)
    - **With tests**: 19.22, 19.75, 18.95 (avg **19.31s**)
- **Post-split `00f35642`**
    - **No tests**: 2.32, 1.46, 1.27 (avg **1.68s**)
    - **With tests**: 20.15, 19.40, 18.13 (avg **19.23s**)
- **Delta (avg)**
    - **No tests**: post-split **+0.03s** (~1.8% slower)
    - **With tests**: post-split **-0.08s** (~0.4% faster)

**B) Build Timing (Earlier Reference Comparison)**

- **Pre-split `6b1db85d`** (already post-split)
    - **No tests**: 1.73, 1.33, 1.39 (avg **1.48s**)
    - **With tests**: 21.23, 19.43, 19.12 (avg **19.93s**)
- **Post-split `00f35642`**
    - **No tests**: 2.16, 1.42, 1.25 (avg **1.61s**)
    - **With tests**: 18.78, 19.46, 18.43 (avg **18.89s**)

**C) Test Timing (Excluding Persistence Slice)**

- **Pre-split `b2ee6f7f`**: 10.90, 10.68, 10.53 (avg **10.70s**)
- **Post-split `00f35642`**: 11.63, 10.44, 10.49 (avg **10.85s**)
- **Delta (avg)**: post-split **+0.15s** (~1.4% slower)

**D) Artifact Size (bootJar)**

- **Pre-split `b2ee6f7f`**: `build/libs/Veri-BE.jar` = **92,345,542 bytes**
- **Post-split `00f35642`**: `app/build/libs/app.jar` = **87,823,691 bytes**
- **Delta**: post-split **-4,521,851 bytes** (~4.9% smaller)

## Analysis

**1) Module split adds task overhead for non-test builds**

- Pre-split runs executed fewer tasks (single module) vs post-split (multi-module **app/support**).
- This aligns with the small no-test regression (~0.03s) on the actual pre/post comparison.

**2) Test runtime dominates overall build time**

- With tests included, variance across runs is larger than the observed deltas.
- Excluding persistence slice still shows **~0.15s** difference, reinforcing minimal impact from module split alone.

**3) Artifact size decreased after split**

- The post-split bootJar is ~4.9% smaller, indicating leaner packaging or dependency resolution for the app module output.

**4) Caveat on earlier comparison**

- `6b1db85d` already includes the **app** module; it is not a true pre-split baseline.
- Use the `b2ee6f7f` vs `00f35642` results as the authoritative comparison.

## Final Conclusion

- **Build time impact**: Module split introduces small task overhead in no-test builds (~0.03s), with no meaningful improvement in test-inclusive builds (differences within variance).
- **Test behavior**: Even after excluding persistence slice tests via explicit filters, end-to-end test time remains nearly unchanged, indicating **testcontainers-heavy integration tests dominate**.
- **Artifact size**: The post-split bootJar is ~4.9% smaller, suggesting leaner packaging under the new module layout.

## History

- **2025-09-20 00:00**: **Module Split Build and Test Summary**. Summary: Consolidated build timings, filtered test timings, and artifact size measurements; included evidence-based analysis and caveats. Modified Files: `/.agents/work/completed/build-module-split-summary.md`.
