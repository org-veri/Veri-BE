package org.goorm.veri.veribe.api.social;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularListResponseV2;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingDetailResponse;
import org.goorm.veri.veribe.domain.book.service.BookService;
import org.goorm.veri.veribe.domain.book.service.BookshelfService;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "소셜")
@Tag(name = "독서")
@RequestMapping("/api/v2/bookshelf")
@RestController
@RequiredArgsConstructor
public class SocialReadingController {

    private final BookshelfService bookshelfService;
    private final BookService bookService;

    @Operation(summary = "주간 인기 도서 조회", description = "인기 도서 상위 10개를 조회합니다. (책장에 최근 7일간 가장 많이 추가된 책)")
    @GetMapping("/popular")
    public ApiResponse<BookPopularListResponseV2> getPopularBooks() {
        Page<BookPopularResponse> pageData = bookshelfService.searchWeeklyPopular(0, 10); // 상위 10개

        return ApiResponse.ok(new BookPopularListResponseV2(pageData.getContent()));
    }

    @Operation(summary = "독서 상세 조회", description = "memberBookId로 책장에 등록된 책의 상세 정보를 조회합니다.")
    @GetMapping("/{readingId}")
    public ApiResponse<ReadingDetailResponse> getBookDetail(@PathVariable Long readingId) {
        ReadingDetailResponse result = bookshelfService.searchDetail(readingId);

        return ApiResponse.ok(result);
    }
}
