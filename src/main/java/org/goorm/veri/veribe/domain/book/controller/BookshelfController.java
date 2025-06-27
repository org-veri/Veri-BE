package org.goorm.veri.veribe.domain.book.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.book.dto.book.BookRequest;
import org.goorm.veri.veribe.domain.book.dto.book.BookResponse;
import org.goorm.veri.veribe.domain.book.dto.book.BookSearchResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.*;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.book.service.BookService;
import org.goorm.veri.veribe.domain.book.service.BookshelfService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v0/bookshelf")
@RestController
@RequiredArgsConstructor
public class BookshelfController {

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

    @GetMapping("/all")
    public DefaultResponse<List<MemberBookResponse>> getAllBooks(@AuthenticatedMember Member member) {
        List<MemberBookResponse> result = bookshelfService.searchAll(member);

        return DefaultResponse.ok(result);
    }

    @GetMapping("/detail")
    public DefaultResponse<MemberBookDetailResponse> getBookDetail(@RequestParam Long memberBookId) {
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

    @PatchMapping("/rate/{score}")
    public DefaultResponse<Void> rateBook(@PathVariable("score") Double score, @RequestParam Long memberBookId) {
        bookshelfService.rateScore(score, memberBookId);

        return DefaultResponse.noContent();
    }

    @PatchMapping("/status/start")
    public DefaultResponse<Void> startReading(@RequestParam Long memberBookId) {
        bookshelfService.readStart(memberBookId);

        return DefaultResponse.noContent();
    }

    @PatchMapping("/status/over")
    public DefaultResponse<Void> finishReading(@RequestParam Long memberBookId) {
        bookshelfService.readOver(memberBookId);

        return DefaultResponse.noContent();
    }

    @DeleteMapping
    public DefaultResponse<Void> deleteFromBookshelf(@RequestParam Long memberBookId) {
        bookshelfService.deleteBook(memberBookId);

        return DefaultResponse.noContent();
    }

}
