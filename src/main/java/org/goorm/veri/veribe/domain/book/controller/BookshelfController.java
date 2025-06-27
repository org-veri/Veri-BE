package org.goorm.veri.veribe.domain.book.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.dtos.book.BookRequest;
import org.goorm.veri.veribe.domain.book.dtos.book.BookResponse;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.*;
import org.goorm.veri.veribe.domain.book.service.BookService;
import org.goorm.veri.veribe.domain.book.service.BookshelfService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/v0/bookshelf")
@RestController
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;
    private final BookService bookService;

    @PostMapping
    public DefaultResponse<MemberBookAddResponse> addBook(@RequestBody MemberBookAddRequest request) {

        BookRequest bookRequest = request.bookRequest();

        Long bookId = bookService.addBook(
                bookRequest.title(),
                bookRequest.image(),
                bookRequest.author(),
                bookRequest.publisher(),
                bookRequest.isbn());

        Long memberBookId = bookshelfService.addToBookshelf(request.memberId(), bookId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime parsedTime = LocalDateTime.of(now.getYear(),
                now.getMonth(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                0,
                0);

        return DefaultResponse.created(new MemberBookAddResponse(memberBookId, parsedTime));
    }

    @GetMapping("/all")
    public DefaultResponse<List<MemberBookResponse>> getAllBooks(@RequestParam Long memberId) {
        List<MemberBookResponse> result = bookshelfService.searchAll(memberId);

        return DefaultResponse.ok(result);
    }

    @GetMapping("/detail")
    public DefaultResponse<MemberBookDetailResponse> getBookDetail(@RequestParam Long memberBookId) {
        MemberBookDetailResponse result = bookshelfService.searchDetail(memberBookId);

        return DefaultResponse.ok(result);
    }

    @GetMapping("/search/{display}/{start}")
    public DefaultResponse<List<BookResponse>> searchBooks(@PathVariable("display") int display,
                                                           @PathVariable("start") int start,
                                                           @RequestParam String query)
    {
        List<BookResponse> bookResponses = bookService.searchBook(query, display, start);

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
