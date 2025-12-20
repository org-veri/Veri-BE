# API Test Scenarios Coverage Checklist

기준: `docs/api-test-scenarios.md` 시나리오별 현재 테스트 커버리지 점검 결과.
- [x] = 테스트로 명시적으로 검증됨
- [ ] = 미검증/누락

## Common Domain

### AuthController (`/api/v1/auth`)
- [x] POST /auth/reissue | Stored refresh 토큰으로 재발급
- [x] POST /auth/reissue | 만료 혹은 위조된 refresh 토큰
- [x] POST /auth/reissue | refreshToken 누락/NULL
- [x] POST /auth/reissue | 탈퇴 등으로 존재하지 않는 회원 토큰
- [x] POST /auth/reissue | 블랙리스트에 등록된 refresh 토큰
- [x] POST /auth/logout | 정상 로그아웃
- [x] POST /auth/logout | attribute 에 토큰 없음
- [x] POST /auth/logout | 이미 블랙리스트 처리된 토큰으로 재요청
- [x] POST /auth/logout | 만료된 토큰
- [x] POST /auth/logout | refresh 토큰이 저장소에 없음

### ImageController (`/api/.../images`)
- [x] POST /v0/images/ocr | 정상 OCR + 저장
- [x] POST /v0/images/ocr | OCR 서비스 예외
- [x] POST /v0/images/ocr | 권한 없음
- [x] POST /v1/images/ocr | 동일 URL 재업로드
- [x] POST /v1/images/ocr | URL 파라미터 누락
- [x] GET /v0/images | 기본 페이지네이션
- [x] GET /v0/images | 마지막 페이지+1 조회
- [x] GET /v0/images | page/size 유효성 실패
- [x] GET /v0/images | 업로드 이력 없는 회원
- [x] GET /v0/images | 인증 없이 목록 조회

## Personal Domain

### MemberController (`/api/v1/members`)
- [x] GET /me | 정상 내 정보 조회
- [x] GET /me | 토큰 없이 호출
- [x] PATCH /me/info | 닉네임/프로필 모두 수정
- [x] PATCH /me/info | 기존 닉네임과 동일한 값 요청
- [x] PATCH /me/info | 닉네임 중복
- [x] PATCH /me/info | 필수 필드 검증 실패
- [x] PATCH /me/info | 인증 없이 수정 시도
- [x] GET /nickname/exists | 닉네임 존재 여부 true
- [x] GET /nickname/exists | 파라미터 누락

### BookshelfController (`/api/v2/bookshelf`)
- [x] GET /my | 기본 조회
- [x] GET /my | 상태/정렬 필터링
- [x] GET /my | 잘못된 sortType
- [x] GET /my | page 최소 위반
- [x] GET /my | 인증 없이 접근
- [x] POST / (addBook) | 새 도서 + 책장 추가
- [x] POST / | 동일 도서 중복 추가
- [x] POST / | 잘못된 ISBN 등 유효성
- [x] POST / | 인증 없이 추가 시도
- [x] GET /search | 키워드 검색 성공
- [x] GET /search | page/size 범위 위반
- [x] GET /my/count | 완독 수 존재
- [x] GET /my/count | 완독 없음
- [x] GET /my/search | 제목/저자로 조회 성공
- [x] GET /my/search | 미존재 도서
- [x] GET /my/count·/my/search | 인증 없이 접근
- [x] PATCH /{id}/modify | 점수/기간 모두 수정
- [x] PATCH /{id}/modify | 점수 0.5 단위 위반
- [x] PATCH /{id}/modify | startedAt/endedAt 미래 or 역전
- [x] PATCH /{id}/modify | 타인 독서 수정 시도
- [x] PATCH /{id}/rate | 정상 별점 등록
- [x] PATCH /{id}/rate | 범위 위반
- [x] PATCH /{id}/rate | null 로 점수 제거
- [x] PATCH /{id}/status/start | 독서 시작
- [x] PATCH /{id}/status/start | 이미 DONE 상태
- [x] PATCH /{id}/status/over | 독서 완료
- [x] PATCH /{id}/status/over | startedAt null 인 상태
- [x] PATCH /{id}/visibility | 공개로 전환
- [x] PATCH /{id}/visibility | 비공개 전환 시 카드 동기화
- [x] PATCH /{id}/visibility | 타인 독서 visibility
- [x] DELETE /{id} | 정상 삭제
- [x] DELETE /{id} | 존재하지 않는 ID
- [x] PATCH·DELETE 보호 엔드포인트 | 인증 헤더 누락

### CardController (`/api/v1/cards`)
- [x] POST / | 공개 독서에 카드 생성
- [x] POST / | 비공개 독서에 카드 생성
- [x] POST / | 존재하지 않는 readingId
- [x] POST / | content/image 누락
- [x] GET /my/count | 정상 조회
- [x] GET /my | 정렬/페이지 정상
- [x] GET /my | 정렬 파라미터 오류
- [x] GET /my | size/page 최소 위반
- [x] POST/GET 보호된 카드 API | 인증 없이 호출
- [x] GET /{cardId} | 공개 카드 조회
- [x] GET /{cardId} | 비공개 카드 접근
- [x] PATCH /{cardId} | 소유 카드 수정
- [x] PATCH /{cardId} | 유효성 실패
- [x] PATCH /{cardId} | 타인 카드 수정 시도
- [x] DELETE /{cardId} | 정상 삭제
- [x] DELETE /{cardId} | 비소유 카드 삭제
- [x] PATCH·DELETE /{cardId} | 인증 없이 접근
- [x] POST /image | presigned URL 발급
- [x] POST /image | 용량 초과
- [x] POST /image | 이미지 타입 아님
- [x] POST /image/ocr | OCR 업로드 presigned URL

### CardControllerV2 (`/api/v2/cards`)
- [x] POST /image | presigned POST form 발급
- [x] POST /image | 허용 용량 초과 파일 업로드 시도 (Form 필드 제약 검증)

## Social Domain

### SocialReadingController (`/api/v2/bookshelf`)
- [x] GET /popular | 인기 도서 10개 조회
- [x] GET /popular | 최근 일주일 내 추가 도서 없음
- [x] GET /{readingId} | 공개 독서 상세
- [x] GET /{readingId} | 비공개 + 소유자 조회
- [x] GET /{readingId} | 비공개 + 타인 접근
- [x] GET /{readingId} | 비공개 독서 + 비로그인
- [x] GET /{readingId} | 존재하지 않는 ID

### SocialCardController (`/api/v1/cards`)
- [x] GET / | 전체 공개 카드 feed 최신순
- [x] GET / | 정렬 파라미터 오류
- [x] GET / | page 초과
- [x] PATCH /{cardId}/visibility | 공개 → 비공개
- [x] PATCH /{cardId}/visibility | 비공개 → 공개
- [x] PATCH /{cardId}/visibility | 비공개 독서에 속한 카드 공개 시도
- [x] PATCH /{cardId}/visibility | 타인 카드
- [x] PATCH /{cardId}/visibility | 인증 없이 요청

### PostController (`/api/v1/posts`)
- [x] POST / | 게시글 작성
- [x] POST / | 이미지 11장 이상
- [x] POST / | bookId null
- [x] POST / | 인증 없이 작성 시도
- [x] GET /my | 나의 게시글 목록
- [x] GET /my | 게시글 없음
- [x] GET /my | 인증 없이 접근
- [x] GET / | 전체 feed 최신순
- [x] GET / | 정렬 파라미터 오류
- [x] GET / | page=0
- [x] GET /{postId} | 상세 조회 성공
- [x] GET /{postId} | 존재하지 않는 게시글
- [x] GET /{postId} | 인증 없이 상세 조회
- [x] DELETE /{postId} | 소유 게시글 삭제
- [x] DELETE /{postId} | 타인 게시글 삭제 시도
- [x] DELETE /{postId} | 인증 없이 삭제 시도
- [x] POST /{postId}/publish | 공개 전환
- [x] POST /{postId}/unpublish | 비공개 전환
- [x] POST /{postId}/(un)publish | 인증 없이 호출
- [x] POST /like/{postId} | 최초 좋아요
- [x] POST /like/{postId} | 중복 좋아요
- [x] POST /unlike/{postId} | 좋아요 취소
- [x] POST /unlike/{postId} | 좋아요 안한 상태에서 취소
- [x] POST /(un)like/{postId} | 인증 없이 호출
- [x] POST /image | presigned URL 발급
- [x] POST /image | 용량 초과
- [x] POST /image | 이미지 타입 아님

### CommentController (`/api/v1/comments`)
- [x] POST / | 댓글 작성 성공
- [x] POST / | 게시글 미존재
- [x] POST / | content 누락
- [x] POST / | 인증 없이 작성 시도
- [x] POST /reply | 대댓글 성공
- [x] POST /reply | 삭제된/존재하지 않는 부모
- [x] POST /reply | 인증 없이 작성
- [x] PATCH /{commentId} | 본인 댓글 수정
- [x] PATCH /{commentId} | 타인 댓글 수정 시도
- [x] PATCH /{commentId} | 인증 없이 수정 시도
- [x] DELETE /{commentId} | 소유 댓글 삭제(soft delete)
- [x] DELETE /{commentId} | 이미 삭제된 댓글 재삭제
- [x] DELETE /{commentId} | 인증 없이 삭제 시도