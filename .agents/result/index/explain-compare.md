# Result: EXPLAIN 비교 (적용 전/후)

## Metadata
- **Status**: Completed
- **Date**: 2026-01-03
- **Source Request**: 인덱스 적용 후 EXPLAIN 재실행 및 비교 문서 작성

## Body
- **적용 전 기록 위치**

```text
.agents/result/explain-before-indexes.md
```

- **요약**

**개선됨**
- **reading** 카운트/기간 필터가 인덱스를 사용하기 시작함.
- **card** 멤버 목록이 **filesort** 없이 인덱스 역방향 스캔으로 정렬 처리됨.
- **post** 공개 피드가 **idx_post_public_created_at** 사용으로 풀스캔 제거됨.
- **comment** 루트 목록이 **idx_comment_post_parent_created_at** 사용으로 정렬 비용 감소.
- **post_image** 조인 및 **post_like** 서브쿼리가 신규 인덱스로 명시적 사용.

**변화 없음 또는 제한적**
- **reading** 목록 정렬(NEWEST/SCORE)은 여전히 **Using filesort**.
- **card** 공개 피드는 여전히 풀스캔 + filesort.
- **post** 작성자 피드는 여전히 filesort.
- **member/provider**, **member/nickname**, **book/isbn** 는 샘플 값에 매칭 데이터가 없어 옵티마이저가 `no matching row in const table` 을 반환(실데이터로 재검증 필요).

- **상세 비교**

**Reading 목록 (NEWEST)**
- Before: key **fk_reading_member**, Extra **Using where; Using filesort**
- After: key **uk_reading_member_book**, Extra **Using index condition; Using where; Using filesort**
- 변화: 정렬 비용(filesort) 유지. 인덱스 선택이 composite 정렬 인덱스를 사용하지 못함.

**Reading 목록 (SCORE)**
- Before: key **fk_reading_member**, Extra **Using where; Using filesort**
- After: key **uk_reading_member_book**, Extra **Using index condition; Using where; Using filesort**
- 변화: 정렬 비용(filesort) 유지.

**Reading 완료 카운트**
- Before: key **fk_reading_member**, Extra **Using where**
- After: key **idx_reading_member_status_created_at**, Extra **Using index**
- 변화: 커버링/조건 인덱스 사용으로 개선.

**Reading 멤버+도서 단건 조회**
- Before: key **fk_reading_book**, Extra **Using where**
- After: **no matching row in const table**
- 변화: 샘플 데이터 미존재로 인한 결과. 실데이터로 재확인 필요.

**Reading 제목/저자 단건 조회**
- Before: key **fk_reading_member**, Extra **Using where**
- After: key **uk_reading_member_book**, Extra **Using where; Using index**
- 변화: 조인 전 단계에서 인덱스 사용 확대.

**주간 인기 도서**
- Before: **FULL SCAN** on **reading**, Extra **Using where; Using temporary; Using filesort**
- After: key **idx_reading_created_at_book_id**, Extra **Using where; Using index; Using temporary; Using filesort**
- 변화: 기간 필터 범위 스캔으로 개선. 집계/정렬 비용은 유지.

**Card 멤버 목록**
- Before: key **fk_card_member**, Extra **Using where; Using filesort**
- After: key **idx_card_member_created_at**, Extra **Using where; Backward index scan**
- 변화: 정렬 비용 제거.

**Card 공개 피드**
- Before: **FULL SCAN** on **card**, Extra **Using where; Using filesort**
- After: **FULL SCAN** on **card**, Extra **Using where; Using filesort**
- 변화: 없음 (is_public + created_at 인덱스 미적용).

**Post 피드 (공개)**
- Before: **FULL SCAN** on **post**, Extra **Using where; Using filesort**
- After: key **idx_post_public_created_at**, Extra **Backward index scan**
- 변화: 공개 피드에서 풀스캔 제거. **post_image** 조인도 **idx_post_image_post_display_order** 사용.

**Post 피드 (작성자)**
- Before: key **fk_post_member**, Extra **Using filesort**
- After: key **fk_post_member**, Extra **Using filesort**
- 변화: 정렬 비용 유지. (member_id + created_at 인덱스 없음)

**Comment 루트 목록**
- Before: key **fk_comment_post**, Extra **Using where; Using filesort**
- After: key **idx_comment_post_parent_created_at**, Extra **Using where; Using index**
- 변화: 정렬 비용 감소.

**Post 좋아요 카운트**
- Before: key **fk_post_like_post**, Extra **Using index**
- After: key **uk_post_like_post_member**, Extra **Using index**
- 변화: 유니크 인덱스로 변경되었으나 계획은 유사.

**Post 좋아요 존재 여부**
- Before: key **fk_post_like_member**, Extra **Using where**
- After: key **uk_post_like_post_member**, Extra **Using index**
- 변화: 복합 유니크 인덱스로 개선.

**Image 업로드 목록**
- Before: key **fk_image_member**
- After: key **fk_image_member**
- 변화: 없음.

**Member (provider_id, provider_type)**
- Before: **FULL SCAN** on **member**
- After: **FULL SCAN** on **member**
- 변화: 없음. 실제 데이터로 재확인 필요.

**Member nickname 존재 여부**
- Before: **FULL SCAN** on **member**
- After: **no matching row in const table**
- 변화: 샘플 데이터 미존재로 인한 결과. 인덱스 효과는 실제 데이터로 재확인 필요.

**Book ISBN 조회**
- Before: **FULL SCAN** on **book**
- After: **no matching row in const table**
- 변화: 샘플 데이터 미존재로 인한 결과. 인덱스 효과는 실제 데이터로 재확인 필요.

**Card 멤버 카운트**
- Before: key **fk_card_member**, Extra **Using index**
- After: key **idx_card_member_created_at**, Extra **Using index**
- 변화: 인덱스 전환.

**Reading 멤버 카운트**
- Before: key **fk_reading_member**, Extra **Using index**
- After: key **uk_reading_member_book**, Extra **Using index**
- 변화: 인덱스 전환.

## 레이턴시 개선 추정 (시간복잡도 기반, 오버헤드 포함)
- **가정**
  - 풀 스캔 + filesort: **O(N) + O(N log N)**
  - 인덱스 스캔(정렬 제거): **O(log N + K)** (K는 페이지 크기상 소량 조회)
  - 인덱스 유지 오버헤드(쓰기): 읽기 경로에는 직접 반영하지 않음
  - 지표는 **상대 비교(정규화된 예측값)**이며 실측과 다를 수 있음

### 100 이하 row

| 엔드포인트(쿼리 패턴) | 초기(적용 전) | 1차 개선 | 2차 개선 |
| --- | --- | --- | --- |
| Reading 목록 (최신순) | ~3.0 | ~2.5 | ~1.2 |
| Reading 목록 (점수순) | ~3.0 | ~2.5 | ~2.5 |
| Post 피드 (작성자) | ~2.8 | ~2.8 | ~1.1 |
| Card 공개 피드 | ~3.2 | ~3.2 | ~1.2 |

### 1천 row

| 엔드포인트(쿼리 패턴) | 초기(적용 전) | 1차 개선 | 2차 개선 |
| --- | --- | --- | --- |
| Reading 목록 (최신순) | ~25 | ~18 | ~3.5 |
| Reading 목록 (점수순) | ~25 | ~18 | ~18 |
| Post 피드 (작성자) | ~22 | ~22 | ~3.0 |
| Card 공개 피드 | ~28 | ~28 | ~3.5 |

### 10만 이상 row

| 엔드포인트(쿼리 패턴) | 초기(적용 전) | 1차 개선 | 2차 개선 |
| --- | --- | --- | --- |
| Reading 목록 (최신순) | ~8,000 | ~4,500 | ~15 |
| Reading 목록 (점수순) | ~8,000 | ~4,500 | ~4,500 |
| Post 피드 (작성자) | ~6,500 | ~6,500 | ~12 |
| Card 공개 피드 | ~9,000 | ~9,000 | ~15 |

## 해석 가이드
- **초기 -> 1차**: 인덱스 추가로 스캔 범위/조인 비용 감소 (filesort는 남아있음)
- **1차 -> 2차**: 정렬용 인덱스 추가로 filesort 제거 (대규모일수록 격차 확대)
- **Reading 점수순**: 정렬 전용 인덱스 부재로 2차 개선에서도 filesort 유지

- **2차 개선 (changeSet 3 적용 후)**  
  
**Reading 목록 (NEWEST)**  
- After(2차): key **idx_reading_member_created_at**, Extra **Using where**  
- 변화: **filesort 제거**, 정렬이 인덱스 역순 스캔으로 처리됨.  

**Reading 목록 (SCORE)**  
- After(2차): key **uk_reading_member_book**, Extra **Using index condition; Using where; Using filesort**  
- 변화: **filesort 유지** (score 정렬은 별도 인덱스 필요).  

**Post 피드 (작성자)**  
- After(2차): key **idx_post_member_created_at**, Extra **NULL**  
- 변화: **filesort 제거**.  

**Card 공개 피드**  
- After(2차): key **idx_card_public_created_at**, Extra **Using where**  
- 변화: **filesort 제거**.
