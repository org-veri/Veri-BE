package org.veri.be.slice.web

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.common.dto.MemberProfileResponse
import org.veri.be.api.social.SocialReadingController
import org.veri.be.domain.book.dto.book.BookPopularResponse
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse
import org.veri.be.domain.book.entity.enums.ReadingStatus
import org.veri.be.domain.book.service.BookshelfService
import org.veri.be.lib.response.ApiResponseAdvice
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class SocialReadingControllerTest {

    private lateinit var mockMvc: MockMvc

    @org.mockito.Mock
    private lateinit var bookshelfService: BookshelfService

    @BeforeEach
    fun setUp() {
        val controller = SocialReadingController(bookshelfService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .build()
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/popular")
    inner class GetPopularBooks {

        @Test
        @DisplayName("주간 인기 도서 목록을 반환한다")
        fun returnsPopularBooks() {
            val response = BookPopularResponse(
                "https://example.com/book.png",
                "title",
                "author",
                "publisher",
                "isbn-1"
            )
            val page = PageImpl(
                listOf(response),
                PageRequest.of(0, 10),
                1
            )
            given(bookshelfService.searchWeeklyPopular(0, 10)).willReturn(page)

            mockMvc.perform(get("/api/v2/bookshelf/popular"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books[0].title").value("title"))
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/{readingId}")
    inner class GetBookDetail {

        @Test
        @DisplayName("독서 상세 정보를 반환한다")
        fun returnsDetail() {
            val response = ReadingDetailResponse.builder()
                .memberBookId(10L)
                .member(MemberProfileResponse(1L, "member", "https://example.com/profile.png"))
                .title("title")
                .author("author")
                .imageUrl("https://example.com/book.png")
                .status(ReadingStatus.READING)
                .score(4.5)
                .startedAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endedAt(LocalDateTime.of(2024, 1, 2, 0, 0))
                .cardSummaries(listOf(ReadingDetailResponse.CardSummaryResponse(1L, "img", true)))
                .isPublic(true)
                .build()
            given(bookshelfService.searchDetail(10L)).willReturn(response)

            mockMvc.perform(get("/api/v2/bookshelf/10"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.memberBookId").value(10L))
                .andExpect(jsonPath("$.result.title").value("title"))
        }
    }
}
