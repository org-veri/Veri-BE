package org.goorm.veri.veribe.domain.book.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.book.controller.enums.ReadingSortType;
import org.goorm.veri.veribe.domain.book.dto.reading.request.ReadingModifyRequest;
import org.goorm.veri.veribe.domain.book.dto.reading.request.ReadingScoreRequest;
import org.goorm.veri.veribe.domain.book.dto.reading.response.*;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularListResponseV2;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.book.BookRequest;
import org.goorm.veri.veribe.domain.book.dto.book.BookSearchResponse;
import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.book.service.BookService;
import org.goorm.veri.veribe.domain.book.service.BookshelfService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "책장 API V2")
@RequestMapping("/api/v2/bookshelf")
@RestController
@RequiredArgsConstructor
public class BookshelfControllerV2 {

    private final BookshelfService bookshelfService;
    private final BookService bookService;

    @Operation(summary = "책장에 책 추가", description = "신규 도서를 등록하고 내 책장에 추가합니다.")
    @PostMapping
    public ApiResponse<ReadingAddResponse> addBook(@RequestBody BookRequest request, @AuthenticatedMember Member member) {

        Long bookId = bookService.addBook(
                request.title(),
                request.image(),
                request.author(),
                request.publisher(),
                request.isbn());

        Reading reading = bookshelfService.addToBookshelf(member, bookId);

        return ApiResponse.created(new ReadingAddResponse(reading.getId(), reading.getCreatedAt()));
    }

    @Operation(summary = "내 책장 전체 조회", description = "내 책장에 등록된 모든 책을 페이지네이션과 정렬 기준으로 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<ReadingListResponse> getAllBooks(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "newest") String sort,
            @AuthenticatedMember Member member
    ) {
        ReadingSortType sortType = ReadingSortType.from(sort);
        Page<ReadingResponse> pageData = bookshelfService.searchAll(member.getId(), page - 1, size, sortType);

        return ApiResponse.ok(new ReadingListResponse(pageData));
    }

    @Operation(summary = "독서 상세 조회", description = "memberBookId로 책장에 등록된 책의 상세 정보를 조회합니다.")
    @GetMapping("/{readingId}")
    public ApiResponse<ReadingDetailResponse> getBookDetail(@PathVariable Long readingId) {
        ReadingDetailResponse result = bookshelfService.searchDetail(readingId);

        return ApiResponse.ok(result);
    }

    @Operation(summary = "도서 검색", description = "검색어로 도서를 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<BookSearchResponse> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        BookSearchResponse bookResponses = bookService.searchBook(query, page, size);

        return ApiResponse.ok(bookResponses);
    }

    @Operation(summary = "인기 도서 조회", description = "인기 도서 상위 10개를 조회합니다.")
    @GetMapping("/popular")
    public ApiResponse<BookPopularListResponseV2> getPopularBooks() {
        Page<BookPopularResponse> pageData = bookshelfService.searchPopular(0, 10); // 상위 10개

        return ApiResponse.ok(new BookPopularListResponseV2(pageData.getContent()));
    }

    @Operation(summary = "내 완독 책 개수 조회", description = "내가 완독한 책의 개수를 조회합니다.")
    @GetMapping("/my/count")
    public ApiResponse<Integer> getMyBookCount(@AuthenticatedMember Member member) {
        Integer count = bookshelfService.searchMyReadingDoneCount(member.getId());

        return ApiResponse.ok(count);
    }

    @Operation(summary = "내 책장 책 제목 & 저자 조회", description = "내 책장에서 제목과 저자값으로 책의 id를 검색합니다")
    @GetMapping("/my/search")
    public ApiResponse<Long> getMyBookByTitleAndAuthor(
            @AuthenticatedMember Member member,
            @RequestParam String title,
            @RequestParam String author) {
        Long memberBookId = bookshelfService.searchByTitleAndAuthor(member.getId(), title, author);

        return ApiResponse.ok(memberBookId);
    }

    @Operation(summary = "책장 도서 내용 전체 수정", description = "책장에 등록된 책의 별점, 독서 시작 시간, 독서 완료 시간, 독서 상태를 변경합니다")
    @PatchMapping("/{readingId}/modify")
    public ApiResponse<Void> modifyBook(
            @RequestBody @Valid ReadingModifyRequest request,
            @PathVariable Long readingId
    ) {
        bookshelfService.modifyBook(request.score(), request.startedAt(), request.endedAt(), readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "책 평점 등록/수정", description = "책장에 등록된 책의 평점을 등록 또는 수정합니다.")
    @PatchMapping("/{readingId}/rate")
    public ApiResponse<Void> rateBook(
            @RequestBody @Valid ReadingScoreRequest request,
            @PathVariable Long readingId) {
        bookshelfService.rateScore(request.score(), readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "독서 시작 상태 변경", description = "책장에 등록된 책의 상태를 '읽는 중'으로 변경합니다.")
    @PatchMapping("/{readingId}/status/start")
    public ApiResponse<Void> startReading(@PathVariable Long readingId) {
        bookshelfService.readStart(readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "독서 완료 상태 변경", description = "책장에 등록된 책의 상태를 '완독'으로 변경합니다.")
    @PatchMapping("/{readingId}/status/over")
    public ApiResponse<Void> finishReading(@PathVariable Long readingId) {
        bookshelfService.readOver(readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "독서 공개 여부 수정", description = "비공개시 해당 독서에 대한 모든 독서카드도 비공개로 설정됩니다.")
    @PatchMapping("/{readingId}/visibility")
    public ApiResponse<ReadingVisibilityUpdateResponse> modifyVisibility(
            @PathVariable Long readingId,
            @RequestParam boolean isPublic
    ) {
        return ApiResponse.ok(bookshelfService.modifyVisibility(readingId, isPublic));
    }

    @Operation(summary = "독서 삭제", description = "책장에 등록된 책을 삭제합니다.")
    @DeleteMapping("/{readingId}")
    public ApiResponse<Void> deleteFromBookshelf(@PathVariable Long readingId) {
        bookshelfService.deleteBook(readingId);

        return ApiResponse.noContent();
    }

}
