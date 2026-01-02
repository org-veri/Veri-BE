# Plan: RestClient Migration for Naver Search

**Status**: Completed  
**Date**: 2025-12-27  
**Goal**: Replace RestTemplate usage in Naver Book Search client with RestClient and describe deeper Jackson customization scan scope.

## Steps
- [x] Review **NaverConfig** and **NaverBookSearchClient** RestTemplate usage
- [x] Update configuration to provide **RestClient**
- [x] Update client implementation and tests to use **RestClient**
- [x] Summarize changes and response to requested deeper Jackson scan scope

## History
2025-12-27 - RestClient Migration for Naver Search - Replaced RestTemplate with RestClient in Naver config/client and updated unit tests. Modified Files: clients/client-search/src/main/java/org/veri/be/domain/book/config/NaverConfig.java, clients/client-search/src/main/java/org/veri/be/domain/book/client/NaverBookSearchClient.java, tests/src/test/java/org/veri/be/unit/book/NaverBookSearchClientTest.java, .agents/work/restclient-migration-naver.md


## Review
**Source File**:
```
.agents/review/restclient-migration-naver.md
```

### Review Content

# Logic Review - 2025-12-27

**Reviewer**: Codex  
**Scope**: RestClient migration for Naver Book Search client

## Summary
- **RestTemplate** bean removed and **RestClient** bean introduced for Naver config.
- **NaverBookSearchClient** switched to **RestClient** with manual response handling to preserve 4xx behavior.
- Unit tests updated to mock **RestClient** chain calls.

## Findings
- No behavioral regression expected: 5xx and null bodies still map to **NaverClientException**.

## Action Items
- None.
