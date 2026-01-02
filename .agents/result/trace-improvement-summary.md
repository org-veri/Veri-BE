# Result: Bookshelf API Trace 개선 전후 요약

**Status**: Completed
**Date**: 2026-01-03
**Source Request**: 개선 전후 트레이스(before.json/after.json) 기반 요약

## 분석 대상
- **API**: `/api/v2/bookshelf/my`
- **비교 파일**:
```
before.json
after.json
```

## 핵심 개선 사항
- **Member 조회 중복 제거**: 개선 전 2회 조회가 개선 후 0회로 감소.
- **커넥션 획득 횟수 축소**: 3회에서 1회로 감소.
- **불필요한 스팬 감소**: 총 스팬 수 15개에서 9개로 감소.

## 수치 비교 요약
- **요청 처리 시간**: 309.816ms -> 135.334ms (약 56.3% 감소)
- **query 스팬 수**: 4 -> 2
- **connection 스팬 수**: 3 -> 1
- **result-set 스팬 수**: 4 -> 2
- **member 조회 쿼리 수**: 2 -> 0
- **reading 조회 쿼리 수**: 2 -> 2 (변화 없음)

## SQL 비교
- **개선 전**:
```
select m1_0.member_id,m1_0.created_at,m1_0.email,m1_0.nickname,m1_0.image,m1_0.provider_id,m1_0.provider_type,m1_0.updated_at from member m1_0 where m1_0.member_id=?
select r1_0.book_id,r1_0.id,b1_0.title,b1_0.author,b1_0.image,r1_0.score,r1_0.started_at,r1_0.status,r1_0.is_public from reading r1_0 join book b1_0 on b1_0.book_id=r1_0.book_id where r1_0.member_id=? and r1_0.status in (?,?,?) order by r1_0.created_at desc limit ?
select count(*) from reading r1_0 where r1_0.member_id=? and r1_0.status in (?,?,?)
select m1_0.member_id,m1_0.created_at,m1_0.email,m1_0.nickname,m1_0.image,m1_0.provider_id,m1_0.provider_type,m1_0.updated_at from member m1_0 where m1_0.member_id=?
```

- **개선 후**:
```
select r1_0.book_id,r1_0.id,b1_0.title,b1_0.author,b1_0.image,r1_0.score,r1_0.started_at,r1_0.status,r1_0.is_public from reading r1_0 join book b1_0 on b1_0.book_id=r1_0.book_id where r1_0.member_id=? and r1_0.status in (?,?,?) order by r1_0.created_at desc limit ?
select count(*) from reading r1_0 where r1_0.member_id=? and r1_0.status in (?,?,?)
```

## 결론
- **개선 전**에는 동일 요청 내 **member 조회 쿼리 2회**가 발생했고, 이에 따라 **커넥션 획득 3회**가 발생했다.
- **개선 후**에는 **member 조회가 제거**되어 **reading 쿼리 2개만 수행**, 커넥션 획득 횟수도 **1회로 축소**되었다.
- 결과적으로 **요청 처리 시간 약 56.3% 감소**라는 정량적 개선이 확인된다.
