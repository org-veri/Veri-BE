package org.veri.be.slice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.veri.be.api.personal.CardController;
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest;
import org.veri.be.domain.card.controller.dto.request.CardUpdateRequest;
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.repository.dto.CardListItem;
import org.veri.be.domain.card.service.CardCommandService;
import org.veri.be.domain.card.service.CardQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.lib.response.ApiResponseAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @Mock
    CardCommandService cardCommandService;

    @Mock
    CardQueryService cardQueryService;

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

        CardController controller = new CardController(cardCommandService, cardQueryService);
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
    @DisplayName("POST /api/v1/cards")
    class CreateCard {

        @Test
        @DisplayName("카드를 생성한다")
        void createsCard() throws Exception {
            CardCreateRequest request = new CardCreateRequest("content", "https://example.com/card.png", 10L, true);
            given(cardCommandService.createCard(member, "content", "https://example.com/card.png", 10L, true))
                    .willReturn(1L);

            mockMvc.perform(post("/api/v1/cards")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result.cardId").value(1L));
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        void returns400WhenFieldMissing() throws Exception {
            CardCreateRequest request = new CardCreateRequest(null, null, null, null);

            mockMvc.perform(post("/api/v1/cards")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my/count")
    class GetMyCardCount {

        @Test
        @DisplayName("내 카드 개수를 반환한다")
        void returnsCount() throws Exception {
            given(cardQueryService.getOwnedCardCount(1L)).willReturn(3);

            mockMvc.perform(get("/api/v1/cards/my/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(3));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my")
    class GetMyCards {

        @Test
        @DisplayName("내 카드 목록을 조회한다")
        void returnsCards() throws Exception {
            CardListItem item = new CardListItem(
                    1L,
                    "book",
                    "content",
                    "https://example.com/card.png",
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    true
            );
            PageImpl<CardListItem> page = new PageImpl<>(
                    List.of(item),
                    PageRequest.of(0, 10),
                    1
            );
            given(cardQueryService.getOwnedCards(eq(1L), eq(0), eq(10), any()))
                    .willReturn(page);

            mockMvc.perform(get("/api/v1/cards/my")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort", "newest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.cards[0].cardId").value(1L));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/{cardId}")
    class GetCard {

        @Test
        @DisplayName("카드 상세를 조회한다")
        void returnsCardDetail() throws Exception {
            CardDetailResponse response = new CardDetailResponse(
                    1L,
                    null,
                    "content",
                    "https://example.com/card.png",
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    null,
                    true,
                    true
            );
            given(cardQueryService.getCardDetail(1L, member)).willReturn(response);

            mockMvc.perform(get("/api/v1/cards/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(1L));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}")
    class UpdateCard {

        @Test
        @DisplayName("카드를 수정한다")
        void updatesCard() throws Exception {
            CardUpdateRequest request = new CardUpdateRequest("content", "https://example.com/card.png");
            CardUpdateResponse response = new CardUpdateResponse(
                    1L,
                    "content",
                    "https://example.com/card.png",
                    null,
                    null,
                    null
            );
            given(cardCommandService.updateCard(member, 1L, "content", "https://example.com/card.png"))
                    .willReturn(response);

            mockMvc.perform(patch("/api/v1/cards/1")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(1L));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cards/{cardId}")
    class DeleteCard {

        @Test
        @DisplayName("카드를 삭제한다")
        void deletesCard() throws Exception {
            mockMvc.perform(delete("/api/v1/cards/1"))
                    .andExpect(status().isNoContent());

            verify(cardCommandService).deleteCard(member, 1L);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image")
    class UploadCardImage {

        @Test
        @DisplayName("Presigned URL을 반환한다")
        void returnsPresignedUrl() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 100);
            PresignedUrlResponse response = new PresignedUrlResponse("https://example.com/presigned", "https://example.com/public");
            given(cardCommandService.getPresignedUrl(any(PresignedUrlRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/cards/image")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.presignedUrl").value("https://example.com/presigned"));
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        void returns400WhenFieldMissing() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest(null, 0);

            mockMvc.perform(post("/api/v1/cards/image")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image/ocr")
    class UploadCardImageForOcr {

        @Test
        @DisplayName("OCR용 Presigned URL을 반환한다")
        void returnsPresignedUrl() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 100);
            PresignedUrlResponse response = new PresignedUrlResponse("https://example.com/presigned", "https://example.com/public");
            given(cardCommandService.getPresignedUrlForOcr(any(PresignedUrlRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/cards/image/ocr")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.presignedUrl").value("https://example.com/presigned"));
        }
    }
}
