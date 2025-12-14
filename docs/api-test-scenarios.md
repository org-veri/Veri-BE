# org.veri.be.api Domain Test Scenarios

```
 1. 기본적으로 mocking 없이 통합 테스트로 진행할건데, src/test/java/org/veri/be/IntegrationTestSupport.java  를 최대한 활용해서 진행해줘. (인증 관리), 
 2. 인증되지 않은 상황에 대해서는 별도의 테스트 파일로 분리해서 인증 관련동작을 모아줘. 
 3. 인가 관련은 서비스 로직이므로 엣지케이스와 함께 처리해줘. 이때 필요하다면 mockMember를 여러 명 추가해서 서로의 인가를 테스트해도 돼. 
 4. 테스트는 도메인별 패키지로 나눌거고, 
 5. 컨트롤러는 슬라이스 테스트로 파라미터 유효성 검사, http 상태 코드, 에러타입, 코드만 검증할거야. 
 6.
```

다음 시나리오는 `src/main/java/org/veri/be/api` 하위의 모든 컨트롤러를 도메인(Common / Personal / Social)별로 나누어 정리한 것이다. 각 시나리오는 정상 흐름과 함께 입력 검증, 권한, 상태 전이, 외부 연동 오류 등 가능한 한 많은 엣지 케이스를 포함한다.

## Common Domain

### AuthController (`/api/v1/auth`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| POST /auth/reissue | Stored refresh 토큰으로 재발급 | DB에 유효(refresh 만료 전, 블랙리스트 미등록) 리프레시 토큰이 존재하는 회원의 JSON `{"refreshToken":token}` | 200 OK, 응답에 새 액세스 토큰 포함, 기존 refresh 토큰은 그대로 유지 |
| POST /auth/reissue | 만료 혹은 위조된 refresh 토큰 | 만료 시각이 지난 토큰 혹은 서명이 훼손된 문자열 | 401 Unauthorized (JWT 파싱 실패), 토큰 재발급 안 됨 |
| POST /auth/reissue | refreshToken 누락/NULL | `{"refreshToken":null}` 혹은 필드 자체가 없는 요청 | 400 Bad Request (JWT 파싱 시 NPE 방지 필요), 예외 메시지 로깅 |
| POST /auth/reissue | 탈퇴 등으로 존재하지 않는 회원 토큰 | 토큰 decod 시 id는 있지만 `memberQueryService.findById` 가 NotFound를 던지는 상황 | 404 Not Found, 재발급 실패 로그 확인 |
| POST /auth/reissue | 블랙리스트에 등록된 refresh 토큰 | 로그아웃 등으로 refresh 가 폐기된 후 동일 토큰 사용 | 기대값: 401 Unauthorized. 현재 구현은 저장소 검증이 없어 재발급될 수 있으므로 테스트로 결함 확인 |
| POST /auth/logout | 정상 로그아웃 | 서명 검증 통과된 액세스 토큰을 `request attribute token`에 주입하여 호출 | 204 No Content, refresh 토큰 삭제 + access/refresh 모두 블랙리스트에 남은 만료시간만큼 등록 |
| POST /auth/logout | attribute 에 토큰 없음 | 필터가 attribute 를 세팅하지 못한 요청 | 500 혹은 400을 방지하도록 사전 검증 필요, 현 구조에서는 NPE 가능 → 시나리오에서 예외 발생 확인 |
| POST /auth/logout | 이미 블랙리스트 처리된 토큰으로 재요청 | 동일 access token 으로 두 번 로그아웃 호출 | 두 번째 요청에서도 204, 블랙리스트 TTL 갱신 여부 확인(중복 허용) |
| POST /auth/logout | 만료된 토큰 | exp 지난 access token attribute 로 전달 | JWT 파싱 시 예외 발생 → 401 Unauthorized |
| POST /auth/logout | refresh 토큰이 저장소에 없음 | 토큰 저장소에서 해당 회원 refresh 가 null (이미 만료/삭제) | 204 No Content, access 토큰만 블랙리스트에 등록되고 예외 발생하지 않아야 함 |

### ImageController (`/api/.../images`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| POST /v0/images/ocr | 정상 OCR + 저장 | 인증된 회원, 접근 가능한 이미지 URL | 200 OK, Mistral OCR 결과 문자열 반환, `image` 테이블에 (member, url) 레코드 추가 |
| POST /v0/images/ocr | OCR 서비스 예외 | OCR 대상 이미지 URL 이 HTTP 404 혹은 응답 지연으로 `MistralOcrService` 가 예외 throw | 500 InternalServerException (ImageErrorInfo.OCR_PROCESSING_FAILED) 반환, 이미지 URL 자체는 DB에 남음 |
| POST /v0/images/ocr | 권한 없음 | 토큰 없이 호출 | 401 Unauthorized |
| POST /v1/images/ocr | 동일 URL 재업로드 | 이미 저장된 URL 을 다시 요청 | 200 OK, OCR 재실행(캐싱 없음) 및 중복 레코드 저장되는지 확인 |
| POST /v1/images/ocr | URL 파라미터 누락 | `imageUrl` query param 누락 | 400 Bad Request (Spring 필수 파라미터 오류) |
| GET /v0/images | 기본 페이지네이션 | 기본값 page=1,size=5, 업로드 이력이 8건 이상인 회원 | 200 OK, `PageResponse` 의 page=1, size=5, totalElements=8, totalPages=2 |
| GET /v0/images | 마지막 페이지+1 조회 | page 값이 (총페이지+1)인 경우 | 200 OK, content 빈 리스트, totalElements/Pages 는 실제 값 유지 |
| GET /v0/images | page/size 유효성 실패 | page=0 또는 size=0 | 400 Bad Request (@Min 검증) |
| GET /v0/images | 업로드 이력 없는 회원 | 인증된 신규 회원 | 200 OK, `PageResponse.empty` 구조(body.data.dataList 비어있음) |
| GET /v0/images | 인증 없이 목록 조회 | Authorization 헤더 누락 | 401 Unauthorized |

## Personal Domain

### MemberController (`/api/v1/members`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| GET /me | 정상 내 정보 조회 | 인증된 회원 | 200 OK, 응답에 nickname/email/card·reading count 포함 |
| GET /me | 토큰 없이 호출 | 인증 헤더 미포함 | 401 Unauthorized |
| PATCH /me/info | 닉네임/프로필 모두 수정 | 유효한 URL, 중복되지 않은 닉네임 | 200 OK, MemberSimpleResponse 에 변경된 필드 반영 |
| PATCH /me/info | 기존 닉네임과 동일한 값 요청 | request.nickname 이 현재 회원 닉네임과 동일하지만 `existsByNickname` 가 true를 반환 | 400 Bad Request (ALREADY_EXIST_NICKNAME) → 동일 닉네임 유지가 불가능한지 확인 |
| PATCH /me/info | 닉네임 중복 | 다른 회원이 이미 사용하는 닉네임 | 400 Bad Request, DB 변경 없음 |
| PATCH /me/info | 필수 필드 검증 실패 | nickname 빈 문자열 혹은 profileImageUrl 잘못된 형식 | 400 Bad Request (Bean Validation) |
| PATCH /me/info | 인증 없이 수정 시도 | Authorization 헤더 누락 | 401 Unauthorized |
| GET /nickname/exists | 닉네임 존재 여부 true | `?nickname=dup` 로 기존 닉네임 조회 | 200 OK + body.data=true |
| GET /nickname/exists | 파라미터 누락 | nickname query param 없이 호출 | 400 Bad Request (MissingServletRequestParameterException) |

### BookshelfController (`/api/v2/bookshelf`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| GET /my | 기본 조회 | statuses 미지정(전체), page/size 기본, sortType 기본 NEWEST | 200 OK, 응답 content 최신순 |
| GET /my | 상태/정렬 필터링 | `statuses=READING,DONE`, `sortType=SCORE` | 200 OK, 지정한 상태만, score desc |
| GET /my | 잘못된 sortType | `sortType=unknown` | 400 Bad Request (바인딩 실패) |
| GET /my | page 최소 위반 | `page=0` | 400 Bad Request |
| GET /my | 인증 없이 접근 | Authorization 헤더 누락 | 401 Unauthorized |
| POST / (addBook) | 새 도서 + 책장 추가 | 필수 필드(title, author, isbn 등) 채운 요청, isPublic 미지정 → false | 201 Created, ReadingAddResponse 반환, Reading.status=NOT_START |
| POST / | 동일 도서 중복 추가 | 동일 member, 동일 bookId (isbn) 연속 요청 | 201 Created 이지만 서비스가 기존 Reading 반환 → 이미 존재하는 readingId 응답, 중복 insert 방지 확인 |
| POST / | 잘못된 ISBN 등 유효성 | 필드 비었을 때 | 400 Bad Request (BookService validation) |
| POST / | 인증 없이 추가 시도 | 토큰 없이 책장 추가 요청 | 401 Unauthorized |
| GET /search | 키워드 검색 성공 | `query=Harry`, page/size valid | 200 OK, pagination 정보 포함 |
| GET /search | page/size 범위 위반 | `size=0` | 400 Bad Request |
| GET /my/count | 완독 수 존재 | DONE 상태 3건인 회원 | 200 OK, body.data=3 |
| GET /my/count | 완독 없음 | DONE 0건 | 200 OK, 0 |
| GET /my/search | 제목/저자로 조회 성공 | 회원 책장에 동일 title+author 존재 | 200 OK, body.data=readingId |
| GET /my/search | 미존재 도서 | 일치 항목 없음 | 200 OK, body.data=null |
| GET /my/count·/my/search | 인증 없이 접근 | Authorization 헤더 누락 | 401 Unauthorized |
| PATCH /{id}/modify | 점수/기간 모두 수정 | score=4.5, startedAt<endedAt(과거) | 204 No Content, Reading.status DONE 으로 갱신 |
| PATCH /{id}/modify | 점수 0.5 단위 위반 | score=4.4 | 400 Bad Request ("0.5 단위") |
| PATCH /{id}/modify | startedAt/endedAt 미래 or 역전 | 미래 시각 또는 endedAt<startedAt | 400 Bad Request (Bean Validation) |
| PATCH /{id}/modify | 타인 독서 수정 시도 | 다른 회원의 readingId | 403 Forbidden (authorizeMember) |
| PATCH /{id}/rate | 정상 별점 등록 | score=3.5 | 204 No Content |
| PATCH /{id}/rate | 범위 위반 | score=5.5 또는 -0.5 | 400 Bad Request |
| PATCH /{id}/rate | null 로 점수 제거 | score=null | 204 No Content, DB score null |
| PATCH /{id}/status/start | 독서 시작 | NOT_START 상태 독서 | 204 No Content, startedAt=now, status=READING |
| PATCH /{id}/status/start | 이미 DONE 상태 | DONE 상태에서 다시 start | 204 No Content, status READING 으로 변경되어 endedAt null 유지 여부 확인 |
| PATCH /{id}/status/over | 독서 완료 | READING 상태 | 204, endedAt=now, status=DONE |
| PATCH /{id}/status/over | startedAt null 인 상태 | 이전에 start 호출 X | 204, endedAt 설정되지만 startedAt null → 허용 여부 확인 |
| PATCH /{id}/visibility | 공개로 전환 | isPublic=false 였던 독서 | 200 OK, ReadingVisibilityUpdateResponse.isPublic=true |
| PATCH /{id}/visibility | 비공개 전환 시 카드 동기화 | isPublic=true, 연관 카드 여럿 | 200 OK, 독서 isPublic=false 이고 모든 카드 isPublic=false (Reading.setPrivate cascades) |
| PATCH /{id}/visibility | 타인 독서 visibility | 다른 회원의 readingId | 403 Forbidden |
| DELETE /{id} | 정상 삭제 | 소유 독서 ID | 204 No Content, 연관 카드 orphan 처리 확인 |
| DELETE /{id} | 존재하지 않는 ID | 잘못된 readingId | 400 Bad Request (BookErrorInfo.BAD_REQUEST) |
| PATCH·DELETE 보호 엔드포인트 | 인증 헤더 누락 | status/rate/visibility/delete 등 @AuthenticatedMember 를 쓰는 요청에서 Authorization 생략 | 401 Unauthorized |

### CardController (`/api/v1/cards`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| POST / | 공개 독서에 카드 생성 | reading.isPublic=true, 요청 isPublic=true | 201 Created, 카드가 공개 상태로 저장 |
| POST / | 비공개 독서에 카드 생성 | reading.isPublic=false, 요청 isPublic=true | 201 Created, 실제 카드 isPublic=false (독서 상태 우선) |
| POST / | 존재하지 않는 readingId | memberBookId 잘못됨 | 400 Bad Request (CardErrorInfo.BAD_REQUEST) |
| POST / | content/image 누락 | content=null | 400 Bad Request (서비스 혹은 DB) |
| GET /my/count | 정상 조회 | 회원이 5장 보유 | 200 OK, 5 |
| GET /my | 정렬/페이지 정상 | page=1,size=10, sort=newest | 200 OK, CardListResponse.ofOwn 결과 |
| GET /my | 정렬 파라미터 오류 | sort=abc | 400 Bad Request (CardSortType.from) |
| GET /my | size/page 최소 위반 | page=0 | 400 Bad Request |
| POST/GET 보호된 카드 API | 인증 없이 호출 | Authorization 헤더 미포함으로 create/my/count 요청 | 401 Unauthorized |
| GET /{cardId} | 공개 카드 조회 | 타인의 공개 카드 | 200 OK |
| GET /{cardId} | 비공개 카드 접근 | 타인의 비공개 카드 | 403 Forbidden (authorizeMember) |
| PATCH /{cardId} | 소유 카드 수정 | content+imageUrl 수정 | 200 OK, 반환에 변경된 값 |
| PATCH /{cardId} | 유효성 실패 | content null, imageUrl invalid URL | 400 Bad Request |
| PATCH /{cardId} | 타인 카드 수정 시도 | 다른 회원 카드 | 403 Forbidden |
| DELETE /{cardId} | 정상 삭제 | 소유 카드 | 204 No Content |
| DELETE /{cardId} | 비소유 카드 삭제 | 다른 회원 카드 | 403 |
| PATCH·DELETE /{cardId} | 인증 없이 접근 | Authorization 헤더 누락 | 401 Unauthorized |
| POST /image | presigned URL 발급 | contentType=image/png,size=2MB | 200 OK, URL + headers 반환 |
| POST /image | 용량 초과 | contentLength>3MB | 400 Bad Request (IMAGE_TOO_LARGE) |
| POST /image | 이미지 타입 아님 | contentType=text/plain | 400 Bad Request (UNSUPPORTED_IMAGE_TYPE) |
| POST /image/ocr | OCR 업로드 presigned URL | 동일 검증, prefix=public/ocr 사용 여부 확인 | 200 OK |

### CardControllerV2 (`/api/v2/cards`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| POST /image | presigned POST form 발급 | 인증된 회원 요청 | 200 OK, 제한: Content-Type image/*, size<=3MB, 만료 5분 |
| POST /image | 허용 용량 초과 파일 업로드 시도 | 발급받은 form 으로 5MB 업로드 | S3 응답 400 (정책 위반) → form 조건 검증 |

## Social Domain

### SocialReadingController (`/api/v2/bookshelf`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| GET /popular | 인기 도서 10개 조회 | 데이터 12건 이상 존재 | 200 OK, content size 최대 10, 정렬이 최근 7일 추가 수 기준인지 검증 |
| GET /popular | 최근 일주일 내 추가 도서 없음 | Data empty | 200 OK, 빈 배열 |
| GET /{readingId} | 공개 독서 상세 | reading.isPublic=true | 200 OK, 카드/도서 상세 포함 |
| GET /{readingId} | 비공개 + 소유자 조회 | isPublic=false 이지만 owner 토큰 | 200 OK |
| GET /{readingId} | 비공개 + 타인 접근 | isPublic=false, 다른 회원 | 403 Forbidden (Reading.authorizeMember) |
| GET /{readingId} | 비공개 독서 + 비로그인 | Authorization 헤더 없음 | 401 Unauthorized (MemberContext 미획득) |
| GET /{readingId} | 존재하지 않는 ID | 잘못된 id | 400 Bad Request (BookErrorInfo.BAD_REQUEST) |

### SocialCardController (`/api/v1/cards`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| GET / | 전체 공개 카드 feed 최신순 | page=1,size=10,sort=newest | 200 OK, 공개 카드만 포함 |
| GET / | 정렬 파라미터 오류 | sort=invalid | 400 Bad Request |
| GET / | page 초과 | page=100, 데이터 부족 | 200 OK, 빈 리스트 |
| PATCH /{cardId}/visibility | 공개 → 비공개 | 카드 소유자, isPublic=false 로 요청 | 200 OK, CardVisibilityUpdateResponse.isPublic=false |
| PATCH /{cardId}/visibility | 비공개 → 공개 | 연관 독서도 공개 상태 | 200 OK |
| PATCH /{cardId}/visibility | 비공개 독서에 속한 카드 공개 시도 | 해당 카드가 private 독서에 연결되어 있음 | 기대값: 400 Bad Request 또는 403 (비공개 독서 카드는 공개 금지). 현재 서비스에 검증이 없다면 테스트로 결함 확인 |
| PATCH /{cardId}/visibility | 타인 카드 | 다른 회원 카드 | 403 Forbidden |
| PATCH /{cardId}/visibility | 인증 없이 요청 | Authorization 헤더 누락 | 401 Unauthorized |

### PostController (`/api/v1/posts`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| POST / | 게시글 작성 | title 1~50자, content, images<=10, bookId 존재 | 201 Created, Post 저장 + 이미지 순서 설정 |
| POST / | 이미지 11장 이상 | images 배열 길이 11 | 400 Bad Request (@Size) |
| POST / | bookId null | bookId 생략 | 400 Bad Request (NotNull) |
| POST / | 인증 없이 작성 시도 | Authorization 헤더 누락 | 401 Unauthorized |
| GET /my | 나의 게시글 목록 | 인증된 회원 | 200 OK, 자신이 작성한 모든 포스트 반환 |
| GET /my | 게시글 없음 | empty | 200 OK, 빈 리스트 |
| GET /my | 인증 없이 접근 | Authorization 헤더 없음 | 401 Unauthorized |
| GET / | 전체 feed 최신순 | page=1,size=10,sort=newest | 200 OK, Page wrapper |
| GET / | 정렬 파라미터 오류 | sort=abc | 400 Bad Request |
| GET / | page=0 | 400 Bad Request |
| GET /{postId} | 상세 조회 성공 | 게시글 존재, 요청자 인증됨 | 200 OK, like 정보/댓글 포함 |
| GET /{postId} | 존재하지 않는 게시글 | 잘못된 ID | 404 Not Found |
| GET /{postId} | 인증 없이 상세 조회 | Authorization 헤더 없음 | 401 Unauthorized |
| DELETE /{postId} | 소유 게시글 삭제 | 작성자 토큰 | 204 No Content |
| DELETE /{postId} | 타인 게시글 삭제 시도 | 다른 회원 토큰 | 403 Forbidden |
| DELETE /{postId} | 인증 없이 삭제 시도 | Authorization 비워둠 | 401 Unauthorized |
| POST /{postId}/publish | 공개 전환 | 작성자 토큰 | 204 No Content, isPublic=true |
| POST /{postId}/unpublish | 비공개 전환 | 작성자 토큰 | 204, isPublic=false, feed 에서 제외 |
| POST /{postId}/(un)publish | 인증 없이 호출 | Authorization 없음 | 401 Unauthorized |
| POST /like/{postId} | 최초 좋아요 | 아직 like 없음 | 200 OK, LikeInfoResponse.count=1,isLiked=true |
| POST /like/{postId} | 중복 좋아요 | 이미 좋아요한 상태 | 200 OK, count 변화 없음, isLiked=true |
| POST /unlike/{postId} | 좋아요 취소 | 좋아요했던 회원 | 200 OK, isLiked=false, count 감소 |
| POST /unlike/{postId} | 좋아요 안한 상태에서 취소 | like 존재 X | 200 OK, count 그대로, isLiked=false |
| POST /(un)like/{postId} | 인증 없이 호출 | Authorization 누락 | 401 Unauthorized |
| POST /image | presigned URL 발급 | contentLength<=1MB, image/png | 200 OK |
| POST /image | 용량 초과 | >1MB | 400 Bad Request |
| POST /image | 이미지 타입 아님 | contentType=application/pdf | 400 Bad Request |

### CommentController (`/api/v1/comments`)
| Endpoint | Scenario | Input / Setup | Expected Result |
| --- | --- | --- | --- |
| POST / | 댓글 작성 성공 | 존재하는 공개 게시글, content 1줄 이상 | 201 Created, commentId 반환 |
| POST / | 게시글 미존재 | postId 잘못됨 | 404 Not Found |
| POST / | content 누락 | content=null | 400 Bad Request |
| POST / | 인증 없이 작성 시도 | Authorization 헤더 없음 | 401 Unauthorized |
| POST /reply | 대댓글 성공 | parentComment 존재, 동일 게시글 내 | 201 Created, parent-child 관계 유지 |
| POST /reply | 삭제된/존재하지 않는 부모 | parentCommentId invalid | 404 Not Found |
| POST /reply | 인증 없이 작성 | Authorization 헤더 없음 | 401 Unauthorized |
| PATCH /{commentId} | 본인 댓글 수정 | content 변경 | 200 OK (void지만 204 예상) |
| PATCH /{commentId} | 타인 댓글 수정 시도 | 다른 회원 토큰 | 403 Forbidden |
| PATCH /{commentId} | 인증 없이 수정 시도 | Authorization 누락 | 401 Unauthorized |
| DELETE /{commentId} | 소유 댓글 삭제(soft delete) | comment.delete() 호출 | 204 No Content, isDeleted flag 확인 |
| DELETE /{commentId} | 이미 삭제된 댓글 재삭제 | delete() 후 다시 호출 | 204 유지, 중복 삭제 허용 |
| DELETE /{commentId} | 인증 없이 삭제 시도 | Authorization 없음 | 401 Unauthorized |
