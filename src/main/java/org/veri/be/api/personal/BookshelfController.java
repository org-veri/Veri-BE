package org.veri.be.api.personal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.veri.be.domain.book.dto.book.AddBookRequest;
import org.veri.be.domain.book.dto.book.BookSearchResponse;
import org.veri.be.domain.book.dto.reading.request.ReadingModifyRequest;
import org.veri.be.domain.book.dto.reading.request.ReadingPageRequest;
import org.veri.be.domain.book.dto.reading.request.ReadingScoreRequest;
import org.veri.be.domain.book.dto.reading.response.ReadingAddResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingListResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingVisibilityUpdateResponse;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.service.BookService;
import org.veri.be.domain.book.service.BookshelfService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.context.AuthenticatedMember;
import org.veri.be.global.auth.guards.MemberGuard;
import org.veri.be.lib.auth.guard.UseGuards;
import org.veri.be.lib.response.ApiResponse;

@Tag(name = "책장")
@RequestMapping("/api/v2/bookshelf")
@RestController
@RequiredArgsConstructor
@UseGuards({MemberGuard.class})
public class BookshelfController {

    private final BookshelfService bookshelfService;
    private final BookService bookService;

    @Operation(summary = "내 책장 전체 조회",
            description = "내 책장에 등록된 모든 책을 페이지네이션과 정렬 기준으로 조회합니다.\n"
                    + "정렬 기준은 'NEWEST', 'OLDEST', 'SCORE' 가 있습니다. (기본값 'NEWEST')\n"
                    + "상태 목록으로 조회할 상태를 지정할 수 있습니다.\n"
                    + "독서 상태는 'NOT_START', 'READING', 'DONE' 가 있습니다. (기본값 전체)"
    )
    @GetMapping("/my")
    public ApiResponse<ReadingListResponse> getAllBooks(
            @ModelAttribute ReadingPageRequest request,
            @AuthenticatedMember Member member
    ) {
        Page<ReadingResponse> pageData = bookshelfService.searchAllReadingOfMember(
                member.getId(),
                request.getStatuses(),
                request.getPage() - 1, request.getSize(), request.getSortType());

        return ApiResponse.ok(new ReadingListResponse(pageData));
    }

    @Operation(summary = "책장에 책 추가", description = "신규 도서를 등록하고 내 책장에 추가합니다.")
    @PostMapping
    public ApiResponse<ReadingAddResponse> addBook(@RequestBody @Valid AddBookRequest request, @AuthenticatedMember Member member) {

        Long bookId = bookService.addBook(
                request.title(),
                request.image(),
                request.author(),
                request.publisher(),
                request.isbn());

        Reading reading = bookshelfService.addToBookshelf(member, bookId, request.isPublic());

        return ApiResponse.created(new ReadingAddResponse(reading.getId(), reading.getCreatedAt()));
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
            @PathVariable Long readingId,
            @AuthenticatedMember Member member
    ) {
        bookshelfService.modifyBook(member, request.score(), request.startedAt(), request.endedAt(), readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "책 평점 등록/수정", description = "책장에 등록된 책의 평점을 등록 또는 수정합니다.")
    @PatchMapping("/{readingId}/rate")
    public ApiResponse<Void> rateBook(
            @RequestBody @Valid ReadingScoreRequest request,
            @PathVariable Long readingId,
            @AuthenticatedMember Member member) {
        bookshelfService.rateScore(member, request.score(), readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "독서 시작 상태 변경", description = "책장에 등록된 책의 상태를 '읽는 중'으로 변경합니다.")
    @PatchMapping("/{readingId}/status/start")
    public ApiResponse<Void> startReading(
            @PathVariable Long readingId,
            @AuthenticatedMember Member member
    ) {
        bookshelfService.readStart(member, readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "독서 완료 상태 변경", description = "책장에 등록된 책의 상태를 '완독'으로 변경합니다.")
    @PatchMapping("/{readingId}/status/over")
    public ApiResponse<Void> finishReading(
            @PathVariable Long readingId,
            @AuthenticatedMember Member member
    ) {
        bookshelfService.readOver(member, readingId);

        return ApiResponse.noContent();
    }

    @Operation(summary = "독서 공개 여부 수정", description = "비공개시 해당 독서에 대한 모든 독서카드도 비공개로 설정됩니다.")
    @PatchMapping("/{readingId}/visibility")
    public ApiResponse<ReadingVisibilityUpdateResponse> modifyVisibility(
            @PathVariable Long readingId,
            @RequestParam boolean isPublic,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.ok(bookshelfService.modifyVisibility(member, readingId, isPublic));
    }

    @Operation(summary = "독서 삭제", description = "책장에 등록된 책을 삭제합니다.")
    @DeleteMapping("/{readingId}")
    public ApiResponse<Void> deleteFromBookshelf(
            @PathVariable Long readingId,
            @AuthenticatedMember Member member
    ) {
        bookshelfService.deleteBook(member, readingId);

        return ApiResponse.noContent();
    }

}
