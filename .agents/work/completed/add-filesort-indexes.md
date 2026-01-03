# Plan: Add Filesort Removal Indexes

## Metadata
- **Status**: Completed
- **Date**: 2026-01-03
- **Goal**: Add remaining indexes to remove filesort for key list queries.

## Steps
- [x] Update Liquibase index SQL with additional list-ordering indexes.
- [x] Document changes and close task.

## Review
- **Summary**: Added three list-ordering indexes to eliminate filesort in remaining list queries.\n- **Findings**: Indexes target **reading**, **post**, and **card** list patterns that still used filesort.\n- **Action Items**: Re-run EXPLAIN after applying these indexes to verify filesort removal.

## History
2026-01-03, Add Filesort Removal Indexes, Appended list-ordering indexes to Liquibase SQL, Modified Files: .deploy/db/changelog/changes/2_add_indexes.sql, .agents/work/add-filesort-indexes.md
