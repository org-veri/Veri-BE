# Issue: Hardcoded Security Credentials

**Severity**: Critical
**Status**: Closed
**Date**: 2025-12-21

## Description
Hardcoded sensitive credentials (AWS Access Keys, Secret Keys) were found in the application property files. This poses a significant security risk, especially if these files are committed to version control.

## Affected Files
*   `src/main/resources/application-local.yml`
*   `src/main/resources/application-prod.yml`

## Findings
*   AWS `access-key` is directly embedded in the configuration.
*   Naver client secrets (inferred from context) may also be present.

## Recommendation
1.  **Remove Secrets**: Delete the hardcoded values immediately from `application-prod.yml` and `application-local.yml`.
2.  **Use Environment Variables**: Replace values with placeholders like `${AWS_ACCESS_KEY}` and inject them via environment variables or a secure vault at runtime.
3.  **Rotate Keys**: If these files were committed to git, the exposed keys must be considered compromised and rotated immediately.
