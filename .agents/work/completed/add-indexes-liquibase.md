# Plan: Add Recommended Indexes to Liquibase

## Metadata
- **Status**: Completed
- **Date**: 2026-01-03
- **Goal**: Add recommended read-path indexes to Liquibase changelog SQL.

## Steps
- [x] Inspect current Liquibase changelog structure and baseline schema.
- [x] Draft index/unique constraint SQL for ROI targets.
- [x] Wire new SQL file into changelog.
- [x] Review and document changes.

## Review
- **Summary**: Added a new Liquibase changeSet and SQL file with ROI-focused indexes and unique constraints.
- **Findings**: Baseline schema defines few secondary indexes; new changeSet addresses high-frequency filters and joins.
- **Action Items**: Validate existing data for uniqueness on **book.isbn**, **member.nickname**, and **reading (member_id, book_id)** before applying in production.

## History
2026-01-03, Add Recommended Indexes to Liquibase, Added new index changeSet and SQL file, Modified Files: .deploy/db/changelog.yaml, .deploy/db/changelog/changes/2_add_indexes.sql, .agents/work/add-indexes-liquibase.md
