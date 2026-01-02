# Result: Java 25 개선 제안 리스트

**Status**: Completed
**Date**: 2026-01-02
**Source Request**: 이 프로젝트에서 java25기능을 활용해서 개선 가능한 부분들을 찾아서 제안 리스트를 문서로 작성해줘.

## 제안 목록

- **제안 1: Scoped Values로 인증 컨텍스트 전파 개선**
  - **Java 25 기능**: **Scoped Values** (preview)
  - **개선 내용**: **ThreadLocal** 기반의 멤버 컨텍스트를 **ScopedValue**로 전환해 요청 범위에서 안전하게 전파하고, 가상 스레드 전환 시 컨텍스트 누락 가능성을 줄입니다.
  - **효과**: 컨텍스트 누수 방지, 가상 스레드 친화성 강화, 테스트 격리 개선.
  - **관련 파일**
```
core/core-api/src/main/java/org/veri/be/global/auth/context/MemberContext.java
core/core-api/src/main/java/org/veri/be/global/auth/context/ThreadLocalCurrentMemberAccessor.java
```

- **제안 2: Structured Concurrency로 OCR 처리 흐름 단순화 및 실패 격리**
  - **Java 25 기능**: **Structured Concurrency** (preview)
  - **개선 내용**: 원본 OCR과 전처리 OCR 요청을 **StructuredTaskScope**로 병렬 실행하고, 먼저 성공하는 결과를 채택하도록 리팩터링합니다. 실패한 작업은 스코프 종료 시 자동 정리되도록 설계합니다.
  - **효과**: 병렬화로 처리 시간 단축, 실패 전파의 단순화, 리소스 정리 일관성 향상.
  - **관련 파일**
```
core/core-api/src/main/java/org/veri/be/domain/image/service/MistralOcrService.java
```

- **제안 3: OCR 전용 Executor를 가상 스레드 기반으로 전환**
  - **Java 25 기능**: **Virtual Threads** (JDK 표준)
  - **개선 내용**: **ThreadPoolTaskExecutor** 대신 `Executors.newVirtualThreadPerTaskExecutor()` 기반의 Executor를 사용해 I/O 중심 OCR 요청의 동시성을 높입니다.
  - **효과**: 큐 대기 감소, 스레드 관리 비용 절감, 확장성 개선.
  - **관련 파일**
```
core/core-api/src/main/java/org/veri/be/domain/image/config/OcrConfig.java
```

- **제안 4: 요청 컨텍스트 전파와 가상 스레드 결합 검증**
  - **Java 25 기능**: **Scoped Values + Virtual Threads** 조합
  - **개선 내용**: 인증 컨텍스트를 ScopedValue로 전환한 뒤, 가상 스레드 작업(예: OCR)에서 컨텍스트 접근이 필요한 구간이 있는지 확인하고 필요한 경우 스코프 내에서 실행하도록 정리합니다.
  - **효과**: 보안/로깅 컨텍스트 일관성 강화, 디버깅 용이성 향상.
  - **관련 파일**
```
core/core-api/src/main/java/org/veri/be/domain/image/service/MistralOcrService.java
core/core-api/src/main/java/org/veri/be/global/auth/context/MemberContext.java
```

## 적용 전 참고 사항

- **Preview 기능 활성화**가 필요할 수 있습니다. Gradle 빌드 설정에서 **`--enable-preview`** 적용 범위와 테스트 실행 정책을 먼저 정리하세요.
- **Spring Boot 4 + 가상 스레드 설정**이 이미 활성화되어 있으므로, 전용 Executor 전환 시 기존 동작과의 호환성 테스트가 필요합니다.
