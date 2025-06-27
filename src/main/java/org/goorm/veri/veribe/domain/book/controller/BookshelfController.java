package org.goorm.veri.veribe.domain.book.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.dtos.book.BookRequest;
import org.goorm.veri.veribe.domain.book.dtos.book.BookResponse;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookAddRequest;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookAddResponse;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookResponse;
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

    //TODO: 책장에서 책의 상세정보 열람
    //의문점: 책장에서 책의 상세정보를 열람할때 해당 책의 독서카드도 같이 보여줘야 한다 -> MemberBook 에서 어떻게 Card 를 참조할지....
    @GetMapping("/detail")
    public DefaultResponse<MemberBookDetailResponse> getBookDetail(@RequestParam Long memberBookId) {
        MemberBookDetailResponse response = bookshelfService.searchDetail(memberBookId);

        return DefaultResponse.ok(response);
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
