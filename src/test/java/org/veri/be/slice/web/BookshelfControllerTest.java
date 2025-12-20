package org.veri.be.slice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.veri.be.api.personal.BookshelfController;
import org.veri.be.domain.book.dto.book.AddBookRequest;
import org.veri.be.domain.book.dto.book.BookSearchResponse;
import org.veri.be.domain.book.dto.book.BookResponse;
import org.veri.be.domain.book.dto.reading.request.ReadingModifyRequest;
import org.veri.be.domain.book.dto.reading.request.ReadingScoreRequest;
import org.veri.be.domain.book.dto.reading.response.ReadingResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingVisibilityUpdateResponse;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.domain.book.service.BookService;
import org.veri.be.domain.book.service.BookshelfService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.veri.be.domain.book.service.BookshelfService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.lib.auth.guard.UseGuardsAspect;
import org.veri.be.lib.response.ApiResponseAdvice;

@ExtendWith(MockitoExtension.class)
class BookshelfControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @Mock
    BookService bookService;

    @Mock
    BookshelfService bookshelfService;

    @Mock
    UseGuardsAspect useGuardsAspect;

    Member member;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        member = Member.builder()
                .id(1L)
                .email("member@test.com")
                .nickname("member")
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build();
        MemberContext.setCurrentMember(member);

        BookshelfController controller = new BookshelfController(bookshelfService, bookService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiResponseAdvice())
                .setCustomArgumentResolvers(new AuthenticatedMemberResolver(new ThreadLocalCurrentMemberAccessor(null)))
                .build();
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my")
    class GetAllBooks {

        @Test
        @DisplayName("내 책장을 조회한다")
        void returnsReadingList() throws Exception {
            ReadingResponse item = new ReadingResponse(
                    10L,
                    20L,
                    "title",
                    "author",
                    "https://example.com/book.png",
                    4.5,
                    null,
                    ReadingStatus.READING,
                    true
            );
            PageImpl<ReadingResponse> page = new PageImpl<>(
                    List.of(item),
                    PageRequest.of(0, 10),
                    1
            );
            given(bookshelfService.searchAllReadingOfMember(eq(1L), any(), eq(0), eq(10), eq(org.veri.be.domain.book.controller.enums.ReadingSortType.NEWEST)))
                    .willReturn(page);

            mockMvc.perform(get("/api/v2/bookshelf/my")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.page").value(1))
                    .andExpect(jsonPath("$.result.memberBooks[0].title").value("title"));
        }
    }

    @Nested
    @DisplayName("POST /api/v2/bookshelf")
    class AddBook {

        @Test
        @DisplayName("도서를 추가한다")
        void addsBook() throws Exception {
            AddBookRequest request = new AddBookRequest(
                    "title",
                    "https://example.com/book.png",
                    "author",
                    "publisher",
                    "isbn-1",
                    true
            );
            given(bookService.addBook(any(), any(), any(), any(), any())).willReturn(10L);
            Reading reading = Reading.builder()
                    .id(20L)
                    .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                    .build();
            given(bookshelfService.addToBookshelf(eq(member), eq(10L), eq(true))).willReturn(reading);

            mockMvc.perform(post("/api/v2/bookshelf")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result.memberBookId").value(20L));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/search")
    class SearchBooks {

        @Test
        @DisplayName("도서 검색 결과를 반환한다")
        void returnsSearchResults() throws Exception {
            BookSearchResponse response = new BookSearchResponse(
                    List.of(BookResponse.builder()
                            .title("title")
                            .author("author")
                            .imageUrl("https://example.com/book.png")
                            .publisher("publisher")
                            .isbn("isbn-1")
                            .build()),
                    1,
                    10,
                    1,
                    1
            );
            given(bookService.searchBook("query", 1, 10)).willReturn(response);

            mockMvc.perform(get("/api/v2/bookshelf/search")
                            .param("query", "query")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.books[0].title").value("title"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/modify")
    class ModifyBook {

        @Test
        @DisplayName("독서 정보를 수정한다")
        void modifiesReading() throws Exception {
            ReadingModifyRequest request = new ReadingModifyRequest(
                    4.5,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 1, 2, 0, 0)
            );

            mockMvc.perform(patch("/api/v2/bookshelf/10/modify")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).modifyBook(eq(member), eq(4.5), any(), any(), eq(10L));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my/count")
    class GetMyBookCount {

        @Test
        @DisplayName("완독 책 개수를 반환한다")
        void returnsDoneCount() throws Exception {
            given(bookshelfService.searchMyReadingDoneCount(1L)).willReturn(3);

            mockMvc.perform(get("/api/v2/bookshelf/my/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(3));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my/search")
    class GetMyBookByTitleAndAuthor {

        @Test
        @DisplayName("제목과 저자로 책장 ID를 반환한다")
        void returnsReadingId() throws Exception {
            given(bookshelfService.searchByTitleAndAuthor(1L, "title", "author"))
                    .willReturn(10L);

            mockMvc.perform(get("/api/v2/bookshelf/my/search")
                            .param("title", "title")
                            .param("author", "author"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(10L));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/rate")
    class RateBook {

        @Test
        @DisplayName("책 평점을 등록한다")
        void ratesBook() throws Exception {
            ReadingScoreRequest request = new ReadingScoreRequest(4.5);

            mockMvc.perform(patch("/api/v2/bookshelf/10/rate")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).rateScore(member, 4.5, 10L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/status/start")
    class StartReading {

        @Test
        @DisplayName("독서 시작 상태로 변경한다")
        void startsReading() throws Exception {
            mockMvc.perform(patch("/api/v2/bookshelf/10/status/start"))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).readStart(member, 10L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/status/over")
    class FinishReading {

        @Test
        @DisplayName("독서 완료 상태로 변경한다")
        void finishesReading() throws Exception {
            mockMvc.perform(patch("/api/v2/bookshelf/10/status/over"))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).readOver(member, 10L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{readingId}/visibility")
    class ModifyVisibility {

        @Test
        @DisplayName("독서 공개 여부를 수정한다")
        void modifiesVisibility() throws Exception {
            given(bookshelfService.modifyVisibility(member, 10L, true))
                    .willReturn(new ReadingVisibilityUpdateResponse(10L, true));

            mockMvc.perform(patch("/api/v2/bookshelf/10/visibility")
                            .param("isPublic", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.idPublic").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/bookshelf/{readingId}")
    class DeleteBook {

        @Test
        @DisplayName("독서를 삭제한다")
        void deletesReading() throws Exception {
            mockMvc.perform(delete("/api/v2/bookshelf/10"))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).deleteBook(eq(member), eq(10L));
        }
    }
}
