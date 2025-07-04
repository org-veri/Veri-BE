package org.goorm.veri.veribe.domain.book.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.book.controller.enums.MemberBookSortType;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularListResponseV2;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.book.BookRequest;
import org.goorm.veri.veribe.domain.book.dto.book.BookSearchResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.*;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.book.service.BookService;
import org.goorm.veri.veribe.domain.book.service.BookshelfService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "책장 API V2")
@RequestMapping("/api/v2/bookshelf")
@RestController
@RequiredArgsConstructor
public class BookshelfControllerV2 {

    private final BookshelfService bookshelfService;
    private final BookService bookService;

    @Operation(summary = "책장에 책 추가", description = "신규 도서를 등록하고 내 책장에 추가합니다.")
    @PostMapping
    public DefaultResponse<MemberBookAddResponse> addBook(@RequestBody BookRequest request, @AuthenticatedMember Member member) {

        Long bookId = bookService.addBook(
                request.title(),
                request.image(),
                request.author(),
                request.publisher(),
                request.isbn());

        MemberBook memberBook = bookshelfService.addToBookshelf(member, bookId);

        return DefaultResponse.created(new MemberBookAddResponse(memberBook.getId(), memberBook.getCreatedAt()));
    }

    @Operation(summary = "내 책장 전체 조회", description = "내 책장에 등록된 모든 책을 페이지네이션과 정렬 기준으로 조회합니다.")
    @GetMapping("/my")
    public DefaultResponse<MemberBookListResponse> getAllBooks(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "newest") String sort,
            @AuthenticatedMember Member member
    ) {
        MemberBookSortType sortType = MemberBookSortType.from(sort);
        Page<MemberBookResponse> pageData = bookshelfService.searchAll(member.getId(), page - 1, size, sortType);

        return DefaultResponse.ok(new MemberBookListResponse(pageData));
    }

    @Operation(summary = "책장 상세 조회", description = "memberBookId로 책장에 등록된 책의 상세 정보를 조회합니다.")
    @GetMapping("/{memberBookId}")
    public DefaultResponse<MemberBookDetailResponse> getBookDetail(@PathVariable Long memberBookId) {
        MemberBookDetailResponse result = bookshelfService.searchDetail(memberBookId);

        return DefaultResponse.ok(result);
    }

    @Operation(summary = "도서 검색", description = "검색어로 도서를 검색합니다.")
    @GetMapping("/search")
    public DefaultResponse<BookSearchResponse> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        BookSearchResponse bookResponses = bookService.searchBook(query, page, size);

        return DefaultResponse.ok(bookResponses);
    }

    @Operation(summary = "인기 도서 조회", description = "인기 도서 상위 10개를 조회합니다.")
    @GetMapping("/popular")
    public DefaultResponse<BookPopularListResponseV2> getPopularBooks() {
        Page<BookPopularResponse> pageData = bookshelfService.searchPopular(0, 10); // 상위 10개

        return DefaultResponse.ok(new BookPopularListResponseV2(pageData.getContent()));
    }

    @Operation(summary = "내 완독 책 개수 조회", description = "내가 완독한 책의 개수를 조회합니다.")
    @GetMapping("/my/count")
    public DefaultResponse<Integer> getMyBookCount(@AuthenticatedMember Member member) {
        Integer count = bookshelfService.searchMyReadingDoneCount(member.getId());

        return DefaultResponse.ok(count);
    }

    @Operation(summary = "책장 도서 내용 전체 수정", description = "책장에 등록된 책의 별점, 독서 시작 시간, 독서 완료 시간, 독서 상태를 변경합니다")
    @PatchMapping("/{memberBookId}/modify")
    public DefaultResponse<Void> modifyBook(
            @RequestBody @Valid MemberBookModifyRequest request,
            @PathVariable Long memberBookId
    )
    {
        bookshelfService.modifyBook(request.score(), request.startedAt(), request.endedAt(), memberBookId);

        return DefaultResponse.noContent();
    }

    @Operation(summary = "책 평점 등록/수정", description = "책장에 등록된 책의 평점을 등록 또는 수정합니다.")
    @PatchMapping("/{memberBookId}/rate")
    public DefaultResponse<Void> rateBook(
            @RequestBody @Valid MemberBookScoreRequest request,
            @PathVariable Long memberBookId) {
        bookshelfService.rateScore(request.score(), memberBookId);

        return DefaultResponse.noContent();
    }

    @Operation(summary = "독서 시작 상태 변경", description = "책장에 등록된 책의 상태를 '읽는 중'으로 변경합니다.")
    @PatchMapping("/{memberBookId}/status/start")
    public DefaultResponse<Void> startReading(@PathVariable Long memberBookId) {
        bookshelfService.readStart(memberBookId);

        return DefaultResponse.noContent();
    }

    @Operation(summary = "독서 완료 상태 변경", description = "책장에 등록된 책의 상태를 '완독'으로 변경합니다.")
    @PatchMapping("/{memberBookId}/status/over")
    public DefaultResponse<Void> finishReading(@PathVariable Long memberBookId) {
        bookshelfService.readOver(memberBookId);

        return DefaultResponse.noContent();
    }

    @Operation(summary = "책장 도서 삭제", description = "책장에 등록된 책을 삭제합니다.")
    @DeleteMapping("/{memberBookId}")
    public DefaultResponse<Void> deleteFromBookshelf(@PathVariable Long memberBookId) {
        bookshelfService.deleteBook(memberBookId);

        return DefaultResponse.noContent();
    }

}
