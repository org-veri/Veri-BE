package org.veri.be.slice.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.book.BookshelfController
import org.veri.be.book.controller.enums.ReadingSortType
import org.veri.be.book.dto.book.AddBookRequest
import org.veri.be.book.dto.book.BookResponse
import org.veri.be.book.dto.book.BookSearchResponse
import org.veri.be.book.dto.reading.request.ReadingModifyRequest
import org.veri.be.book.dto.reading.request.ReadingScoreRequest
import org.veri.be.book.dto.reading.response.ReadingResponse
import org.veri.be.book.dto.reading.response.ReadingVisibilityUpdateResponse
import org.veri.be.book.entity.Reading
import org.veri.be.book.entity.enums.ReadingStatus
import org.veri.be.book.service.BookCommandService
import org.veri.be.book.service.BookQueryService
import org.veri.be.book.service.BookshelfCommandService
import org.veri.be.book.service.BookshelfQueryService
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.auth.context.AuthenticatedMemberResolver
import org.veri.be.lib.auth.context.MemberContext
import org.veri.be.member.auth.context.MemberRequestContext
import org.veri.be.member.auth.context.ThreadLocalCurrentMemberAccessor
import org.veri.be.member.service.MemberQueryService
import org.veri.be.lib.exception.handler.GlobalExceptionHandler
import org.veri.be.lib.response.ApiResponseAdvice
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BookshelfControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @org.mockito.Mock
    private lateinit var bookCommandService: BookCommandService

    @org.mockito.Mock
    private lateinit var bookQueryService: BookQueryService

    @org.mockito.Mock
    private lateinit var bookshelfCommandService: BookshelfCommandService

    @org.mockito.Mock
    private lateinit var bookshelfQueryService: BookshelfQueryService

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        member = Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()
        MemberContext.setCurrentMemberId(member.id)
        lenient().`when`(memberQueryService.findOptionalById(member.id)).thenReturn(Optional.of(member))

        val controller = BookshelfController(
            bookshelfCommandService,
            bookshelfQueryService,
            bookCommandService,
            bookQueryService
        )
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice(), GlobalExceptionHandler())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(ThreadLocalCurrentMemberAccessor(memberQueryService))
            )
            .build()
    }

    @AfterEach
    fun tearDown() {
        MemberContext.clear()
        MemberRequestContext.clear()
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my")
    inner class GetAllBooks {

        @Test
        @DisplayName("내 책장을 조회한다")
        fun returnsReadingList() {
            val item = ReadingResponse(
                10L,
                20L,
                "title",
                "author",
                "https://example.com/book.png",
                4.5,
                null,
                ReadingStatus.READING,
                true
            )
            val page = PageImpl(
                listOf(item),
                PageRequest.of(0, 10),
                1
            )
            given(bookshelfQueryService.searchAllReadingOfMember(eq(1L), any(), eq(0), eq(10), eq(ReadingSortType.NEWEST)))
                .willReturn(page)

            mockMvc.perform(
                get("/api/v2/bookshelf/my")
                    .param("page", "1")
                    .param("size", "10")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.page").value(1))
                .andExpect(jsonPath("$.result.memberBooks[0].title").value("title"))
        }
    }

    @Nested
    @DisplayName("POST /api/v2/bookshelf")
    inner class AddBook {

        @Test
        @DisplayName("도서를 추가한다")
        fun addsBook() {
            val request = AddBookRequest(
                "title",
                "https://example.com/book.png",
                "author",
                "publisher",
                "isbn-1",
                true
            )
            given(bookCommandService.addBook(any(), any(), any(), any(), any())).willReturn(10L)
            val reading = Reading.builder()
                .id(20L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build()
            given(bookshelfCommandService.addToBookshelf(member, 10L, true)).willReturn(reading)

            mockMvc.perform(
                post("/api/v2/bookshelf")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result.memberBookId").value(20L))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = AddBookRequest(null, null, null, null, null, true)

            mockMvc.perform(
                post("/api/v2/bookshelf")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/search")
    inner class SearchBooks {

        @Test
        @DisplayName("도서 검색 결과를 반환한다")
        fun returnsSearchResults() {
            val response = BookSearchResponse(
                listOf(
                    BookResponse.builder()
                        .title("title")
                        .author("author")
                        .imageUrl("https://example.com/book.png")
                        .publisher("publisher")
                        .isbn("isbn-1")
                        .build()
                ),
                1,
                10,
                1,
                1
            )
            given(bookQueryService.searchBook("query", 1, 10)).willReturn(response)

            mockMvc.perform(
                get("/api/v2/bookshelf/search")
                    .param("query", "query")
                    .param("page", "1")
                    .param("size", "10")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books[0].title").value("title"))
        }

        @Test
        @DisplayName("page가 1보다 작으면 400을 반환한다")
        fun returns400WhenPageInvalid() {
            mockMvc.perform(
                get("/api/v2/bookshelf/search")
                    .param("query", "query")
                    .param("page", "0")
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/modify")
    inner class ModifyBook {

        @Test
        @DisplayName("독서 정보를 수정한다")
        fun modifiesReading() {
            val request = ReadingModifyRequest(
                4.5,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0)
            )

            mockMvc.perform(
                patch("/api/v2/bookshelf/10/modify")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNoContent)

            verify(bookshelfCommandService).modifyBook(eq(member), eq(4.5), any(), any(), eq(10L))
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my/count")
    inner class GetMyBookCount {

        @Test
        @DisplayName("완독 책 개수를 반환한다")
        fun returnsDoneCount() {
            given(bookshelfQueryService.searchMyReadingDoneCount(1L)).willReturn(3)

            mockMvc.perform(get("/api/v2/bookshelf/my/count"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(3))
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my/search")
    inner class GetMyBookByTitleAndAuthor {

        @Test
        @DisplayName("제목과 저자로 책장 ID를 반환한다")
        fun returnsReadingId() {
            given(bookshelfQueryService.searchByTitleAndAuthor(1L, "title", "author"))
                .willReturn(10L)

            mockMvc.perform(
                get("/api/v2/bookshelf/my/search")
                    .param("title", "title")
                    .param("author", "author")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(10L))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/rate")
    inner class RateBook {

        @Test
        @DisplayName("책 평점을 등록한다")
        fun ratesBook() {
            val request = ReadingScoreRequest(4.5)

            mockMvc.perform(
                patch("/api/v2/bookshelf/10/rate")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNoContent)

            verify(bookshelfCommandService).rateScore(member, 4.5, 10L)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/status/start")
    inner class StartReading {

        @Test
        @DisplayName("독서 시작 상태로 변경한다")
        fun startsReading() {
            mockMvc.perform(patch("/api/v2/bookshelf/10/status/start"))
                .andExpect(status().isNoContent)

            verify(bookshelfCommandService).readStart(member, 10L)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/status/over")
    inner class FinishReading {

        @Test
        @DisplayName("독서 완료 상태로 변경한다")
        fun finishesReading() {
            mockMvc.perform(patch("/api/v2/bookshelf/10/status/over"))
                .andExpect(status().isNoContent)

            verify(bookshelfCommandService).readOver(member, 10L)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/visibility")
    inner class ModifyVisibility {

        @Test
        @DisplayName("독서 공개 여부를 수정한다")
        fun modifiesVisibility() {
            given(bookshelfCommandService.modifyVisibility(member, 10L, true))
                .willReturn(ReadingVisibilityUpdateResponse(10L, true))

            mockMvc.perform(
                patch("/api/v2/bookshelf/10/visibility")
                    .param("isPublic", "true")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(true))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/bookshelf/{readingId}")
    inner class DeleteBook {

        @Test
        @DisplayName("독서를 삭제한다")
        fun deletesReading() {
            mockMvc.perform(delete("/api/v2/bookshelf/10"))
                .andExpect(status().isNoContent)

            verify(bookshelfCommandService).deleteBook(member, 10L)
        }
    }
}
