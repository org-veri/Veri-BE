---
name: sc:test-checkpoint
description: Analyzes untested code changes since last test checkpoint and generates comprehensive tests following strict conventions
category: testing
personas: []
---

# Test Checkpoint Agent

🧪 **Mission**: Ensure 100% test coverage for all code changes since the last `test.` commit checkpoint, following the strictest test conventions defined in `docs/test-convention.md`.

## Workflow

### Phase 1: Scope Analysis 🔍

1. **Execute Diff Script**
   ```bash
   bash .claude/scripts/get_target_diff.sh
   ```
   - Identify the last `test.` commit
   - Extract all changed files since that checkpoint
   - Categorize changes by layer (entity, repository, service, controller)

2. **Change Classification**
   - **Entities**: Check for new fields, constraints, relationships, cascade rules
   - **Repositories**: Identify custom query methods, JPQL, QueryDSL
   - **Services**: Find new business logic, state transitions, external calls
   - **Controllers**: Detect new endpoints, validation rules, auth requirements
   - **DTOs/Converters**: Note new mapping logic

3. **Confidence Check**
   - Run `@confidence-check` on understanding the changes
   - Required confidence: ≥0.90 before proceeding
   - If <0.90, request clarification from user

### Phase 2: Test Strategy Planning 📋

Based on `docs/test-convention.md`, determine test requirements:

#### For Entity Changes
- **@DataJpaTest** persistence tests:
  - NOT NULL violations (flush timing)
  - UNIQUE constraint checks
  - Cascade operations (persist, remove)
  - OrphanRemoval behavior
  - Relationship integrity

#### For Repository Changes
- **Custom Query Tests**:
  - Filter accuracy (boundary values)
  - Sorting (including tie-breakers)
  - Pagination (edge cases)
  - Join correctness (no duplicates)
  - N+1 query prevention (count SQL queries)

#### For Service Changes
- **Integration Tests** (extending `IntegrationTestSupport`):
  - Success scenarios
  - Business rule violations
  - Authorization checks (multiple mock members if needed)
  - Edge cases (null, empty, boundary values)
  - State transitions
  - External service failures (mocked)

#### For Controller Changes
- **@WebMvcTest** slice tests:
  - Request mapping (URL, method, params)
  - Input validation (@Valid failures → 400)
  - Success responses (status codes, JSON structure)
  - Exception mapping (404, 409, 403, 401)
  - Auth/permission failures

### Phase 3: Test Generation ✍️

**Critical Rules** (from `docs/test-convention.md`):

1. **Structure**
   - Use `@Nested` + `@DisplayName` for readability
   - HTTP method + path for controller nested roots
   - "Situation → Expected Result" for test names
   - Clear Given-When-Then sections

2. **F.I.R.S.T Principles**
   - **Fast**: POJO tests <0.1s, avoid unnecessary @SpringBootTest
   - **Independent**: Each test isolated, no order dependency
   - **Repeatable**: Use `Clock` bean, not `LocalDateTime.now()`
   - **Self-validating**: Assert everything, no manual log checking
   - **Timely**: Cover all changed code

3. **Maintainability**
   - Test behavior, not implementation
   - Avoid over-mocking (only external systems)
   - Use fixture builders (not raw `new` constructors)
   - Custom assertions for domain exceptions

4. **Coverage Requirements**
   - Minimum per endpoint: success, validation fail, exception, auth fail
   - Parameterized tests for repetitive branches
   - Boundary value analysis for all inputs

5. **Integration Test Pattern**
   ```java
   @Transactional
   class SomeServiceIntegrationTest extends IntegrationTestSupport {
       // Uses pre-configured mockMember from IntegrationTestSupport
       // Add more members for authorization testing if needed
       // No mocking by default - real DB operations
   }
   ```

6. **Controller Test Pattern**
   ```java
   @WebMvcTest(SomeController.class)
   class SomeControllerTest {
       @MockBean
       private SomeService someService;

       @Nested
       @DisplayName("POST /api/v1/endpoint")
       class CreateEndpoint {
           @Test
           @DisplayName("유효한 요청 → 201 Created")
           void validRequest_returns201() { }

           @Test
           @DisplayName("필수 필드 누락 → 400 Bad Request")
           void missingField_returns400() { }

           @Test
           @DisplayName("인증 없음 → 401 Unauthorized")
           void noAuth_returns401() { }
       }
   }
   ```

### Phase 4: Test Implementation 🔨

For each identified gap:

1. **Create Test File** (if not exists)
   - Follow package structure: `test/java/org/veri/be/{integration|slice|unit}/{domain}/`
   - Name: `{ClassName}Test.java` or `{ClassName}IntegrationTest.java`

2. **Write Tests** using parallel tool calls where possible:
   - Group related tests in single @Nested classes
   - Use meaningful fixture data (avoid "test1", "test2")
   - Include edge cases from `docs/api-test-scenarios.md` if applicable

3. **Add Test Utilities** (if needed):
   - Fixture builders in `test/java/org/veri/be/support/fixture/`
   - Custom assertions in `test/java/org/veri/be/support/assertion/`

### Phase 5: Self-Review 🔎

Execute strict review checklist:

```markdown
**Audit Checklist**

Structure & Patterns:
- [ ] All tests use @Nested + @DisplayName
- [ ] Given-When-Then clearly separated
- [ ] Fixture builders used (no raw constructors)
- [ ] No duplicate setup code

Coverage & Branches:
- [ ] Minimum 3 cases per endpoint (success, validation, auth)
- [ ] Parameterized tests for repetitive logic
- [ ] Boundary values tested

Domain & Exception:
- [ ] Exception type + error code verified
- [ ] State changes verified (not just mocks)
- [ ] Repository save() calls verified

Layer-Specific:
- [ ] @DataJpaTest: flush timing, constraints, cascades
- [ ] @WebMvcTest: status codes, JSON structure, error format
- [ ] Integration: real DB operations, minimal mocking

Performance:
- [ ] N+1 queries prevented (count SQL where applicable)
- [ ] OSIV disabled: DTOs returned, no lazy loading

Independence:
- [ ] No test order dependencies
- [ ] ThreadLocal cleaned up (@AfterEach)
- [ ] Transactions rolled back
```

### Phase 6: Execution & Verification ✅

1. **Run Tests**
   ```bash
   ./gradlew test --tests "*{NewTestClass}"
   ```
   - All new tests must pass
   - No existing tests should break

2. **Check Coverage** (if Jacoco configured)
   ```bash
   ./gradlew test jacocoTestReport
   ```
   - Verify changed classes have adequate coverage

3. **Fix Failures**
   - Iterate on test implementation
   - Update production code if bugs found
   - Never skip or ignore tests

### Phase 7: Create Test Checkpoint 📌

1. **Stage Test Files**
   ```bash
   git add src/test/java/**/*Test.java
   git add src/test/resources/**
   ```

2. **Create Checkpoint Commit**
   ```bash
   git commit -m "test. [domain] comprehensive test coverage

   - Added {X} persistence tests for {Entity} constraints/relationships
   - Added {Y} integration tests for {Service} business logic
   - Added {Z} controller tests for {Controller} endpoints
   - Verified N+1 prevention, auth flows, edge cases

   Coverage: {brief summary of what's now tested}

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
   ```

3. **Verify Checkpoint**
   ```bash
   bash .claude/scripts/get_target_diff.sh
   ```
   - Should show no diff (or only non-test changes)

## Output Format

Provide concise status updates:

```
🔍 Phase 1: Scope Analysis
📊 Changes detected:
  - 3 entity modifications (Member, Reading, Card)
  - 2 new repository methods (ReadingRepository)
  - 5 service changes (BookshelfService, CardCommandService)
  - 1 new controller endpoint (POST /api/v1/cards/visibility)

📋 Phase 2: Test Strategy
Required tests:
  - Persistence: 8 tests (constraints, cascades)
  - Integration: 12 tests (business logic, auth)
  - Controller: 4 tests (new endpoint coverage)

✍️ Phase 3-4: Test Generation
[Creating tests with parallel writes...]

🔎 Phase 5: Self-Review
✅ All checklist items passed
⚠️ Minor: Added missing boundary test for score validation

✅ Phase 6: Execution
All 24 tests passing ✓

📌 Phase 7: Checkpoint Created
Commit: test. [book/card] comprehensive test coverage
Hash: abc123f
```

## Important Notes

1. **Never Skip Tests**: If confidence is low or requirements unclear, ask user
2. **Strict Convention**: Zero tolerance for deviations from `docs/test-convention.md`
3. **Production Quality**: Tests are production code, apply same standards
4. **Incremental Checkpoints**: Don't batch multiple features, checkpoint after each logical unit

## Error Handling

- **No diff script**: Manually analyze `git log` and recent commits
- **No test convention doc**: Use industry-standard Spring Boot test practices
- **Test failures**: Debug and fix, never commit failing tests
- **Merge conflicts**: Resolve tests first, ensure green suite

---

**Activation**: Run `/sc:test-checkpoint` after implementing any feature to ensure test coverage and create a checkpoint for the next iteration.
