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
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookAddResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookListResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookScoreRequest;
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

@RequestMapping("/api/v2/bookshelf")
@RestController
@RequiredArgsConstructor
public class BookshelfControllerV2 {

    private final BookshelfService bookshelfService;
    private final BookService bookService;

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

    @GetMapping("/{memberBookId}")
    public DefaultResponse<MemberBookDetailResponse> getBookDetail(@PathVariable Long memberBookId) {
        MemberBookDetailResponse result = bookshelfService.searchDetail(memberBookId);

        return DefaultResponse.ok(result);
    }

    @GetMapping("/search")
    public DefaultResponse<BookSearchResponse> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        BookSearchResponse bookResponses = bookService.searchBook(query, page, size);

        return DefaultResponse.ok(bookResponses);
    }

    @GetMapping("/popular")
    public DefaultResponse<BookPopularListResponseV2> getPopularBooks() {
        Page<BookPopularResponse> pageData = bookshelfService.searchPopular(0, 10); // 상위 10개

        return DefaultResponse.ok(new BookPopularListResponseV2(pageData.getContent()));
    }

    @GetMapping("/my/count")
    public DefaultResponse<Integer> getMyBookCount(@AuthenticatedMember Member member) {
        Integer count = bookshelfService.searchMyReadingDoneCount(member.getId());

        return DefaultResponse.ok(count);
    }

    @PatchMapping("/{memberBookId}/rate")
    public DefaultResponse<Void> rateBook(
            @RequestBody @Valid MemberBookScoreRequest request,
            @PathVariable Long memberBookId) {
        bookshelfService.rateScore(request.score(), memberBookId);

        return DefaultResponse.noContent();
    }

    @PatchMapping("/{memberBookId}/status/start")
    public DefaultResponse<Void> startReading(@PathVariable Long memberBookId) {
        bookshelfService.readStart(memberBookId);

        return DefaultResponse.noContent();
    }

    @PatchMapping("/{memberBookId}/status/over")
    public DefaultResponse<Void> finishReading(@PathVariable Long memberBookId) {
        bookshelfService.readOver(memberBookId);

        return DefaultResponse.noContent();
    }

    @DeleteMapping("/{memberBookId}")
    public DefaultResponse<Void> deleteFromBookshelf(@PathVariable Long memberBookId) {
        bookshelfService.deleteBook(memberBookId);

        return DefaultResponse.noContent();
    }

}
