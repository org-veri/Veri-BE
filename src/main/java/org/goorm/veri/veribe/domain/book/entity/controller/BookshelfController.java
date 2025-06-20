package org.goorm.veri.veribe.domain.book.entity.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.entity.dtos.memberBook.MemberBookAddRequest;
import org.goorm.veri.veribe.domain.book.entity.dtos.memberBook.MemberBookAddResponse;
import org.goorm.veri.veribe.domain.book.entity.dtos.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.entity.dtos.memberBook.MemberBookResponse;
import org.goorm.veri.veribe.domain.book.entity.service.BookshelfService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/v0/bookshelf")
@RestController
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;

    @PostMapping
    public DefaultResponse<MemberBookAddResponse> addBook(@RequestBody MemberBookAddRequest request) {
        Long memberBookId = bookshelfService.addBookshelf(
                request.memberId(),
                request.bookId(),
                request.score(),
                request.startedAt(),
                request.endedAt());

        return DefaultResponse.created(new MemberBookAddResponse(memberBookId, LocalDate.now().atStartOfDay()));
    }

    @GetMapping("/all")
    public DefaultResponse<List<MemberBookResponse>> getAllBooks(@RequestParam Long memberId) {
        List<MemberBookResponse> result = bookshelfService.searchAll(memberId);

        return DefaultResponse.ok(result);
    }

    //TODO: 책장에서 책의 상세정보 열람
    //의문점: 책장에서 책의 상세정보를 열람할때 해당 책의 독서카드도 같이 보여줘야 한다 -> MemberBook 에서 어떻게 Card 를 참조할지....

//    @GetMapping("/{memberBookId}")
//    public DefaultResponse<MemberBookDetailResponse> getBookDetail(@PathVariable("memberBookId") Long memberBookId) {
//        MemberBookResponse response = bookshelfService.searchDetail(memberBookId);
//
//        return DefaultResponse.ok(response);
//    }

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
    public DefaultResponse<Void> deleteBookshelf(@RequestParam Long memberBookId) {
        bookshelfService.deleteBook(memberBookId);

        return DefaultResponse.noContent();
    }

}
