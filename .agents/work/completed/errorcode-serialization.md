# Plan: ErrorCode Serialization Safety

## Metadata
- Status: In Progress
- Date: 2025-12-23
- Goal: ApplicationException의 errorCode 필드를 직렬화 안전하게 처리.

## Steps
- [x] 현재 errorCode 필드 직렬화 요구 확인
- [x] errorCode 직렬화 처리 (transient 또는 serializable)
- [x] 문서/리뷰 정리

## History
- **Timestamp**: 2025-12-23 01:38 KST
- **Task**: ErrorCode Serialization Safety
- **Summary**: ApplicationException의 errorCode 필드를 transient로 변경.
- **Modified Files**:
```
app/src/main/java/org/veri/be/lib/exception/ApplicationException.java
```
