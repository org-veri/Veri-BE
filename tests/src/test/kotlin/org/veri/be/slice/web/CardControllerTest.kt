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
import org.veri.be.card.CardController
import org.veri.be.card.controller.dto.request.CardCreateRequest
import org.veri.be.card.controller.dto.request.CardUpdateRequest
import org.veri.be.card.controller.dto.response.CardDetailResponse
import org.veri.be.card.controller.dto.response.CardUpdateResponse
import org.veri.be.card.repository.dto.CardListItem
import org.veri.be.card.service.CardCommandService
import org.veri.be.card.service.CardQueryService
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.auth.context.AuthenticatedMemberResolver
import org.veri.be.lib.auth.context.MemberContext
import org.veri.be.member.auth.context.MemberRequestContext
import org.veri.be.member.auth.context.ThreadLocalCurrentMemberAccessor
import org.veri.be.member.service.MemberQueryService
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.dto.PresignedUrlResponse
import org.veri.be.lib.response.ApiResponseAdvice
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CardControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @org.mockito.Mock
    private lateinit var cardCommandService: CardCommandService

    @org.mockito.Mock
    private lateinit var cardQueryService: CardQueryService

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

        val controller = CardController(cardCommandService, cardQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
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
    @DisplayName("POST /api/v1/cards")
    inner class CreateCard {

        @Test
        @DisplayName("카드를 생성한다")
        fun createsCard() {
            val request = CardCreateRequest("content", "https://example.com/card.png", 10L, true)
            given(cardCommandService.createCard(member, "content", "https://example.com/card.png", 10L, true))
                .willReturn(1L)

            mockMvc.perform(
                post("/api/v1/cards")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result.cardId").value(1L))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = CardCreateRequest(null, null, null, null)

            mockMvc.perform(
                post("/api/v1/cards")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my/count")
    inner class GetMyCardCount {

        @Test
        @DisplayName("내 카드 개수를 반환한다")
        fun returnsCount() {
            given(cardQueryService.getOwnedCardCount(1L)).willReturn(3)

            mockMvc.perform(get("/api/v1/cards/my/count"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(3))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my")
    inner class GetMyCards {

        @Test
        @DisplayName("내 카드 목록을 조회한다")
        fun returnsCards() {
            val item = CardListItem(
                1L,
                "book",
                "content",
                "https://example.com/card.png",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                true
            )
            val page = PageImpl(
                listOf(item),
                PageRequest.of(0, 10),
                1
            )
            given(cardQueryService.getOwnedCards(eq(1L), eq(0), eq(10), any()))
                .willReturn(page)

            mockMvc.perform(
                get("/api/v1/cards/my")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "newest")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards[0].cardId").value(1L))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/{cardId}")
    inner class GetCard {

        @Test
        @DisplayName("카드 상세를 조회한다")
        fun returnsCardDetail() {
            val response = CardDetailResponse(
                1L,
                null,
                "content",
                "https://example.com/card.png",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                null,
                true,
                true
            )
            given(cardQueryService.getCardDetail(1L, member)).willReturn(response)

            mockMvc.perform(get("/api/v1/cards/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.id").value(1L))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}")
    inner class UpdateCard {

        @Test
        @DisplayName("카드를 수정한다")
        fun updatesCard() {
            val request = CardUpdateRequest("content", "https://example.com/card.png")
            val response = CardUpdateResponse(
                1L,
                "content",
                "https://example.com/card.png",
                null,
                null,
                null
            )
            given(cardCommandService.updateCard(member, 1L, "content", "https://example.com/card.png"))
                .willReturn(response)

            mockMvc.perform(
                patch("/api/v1/cards/1")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.id").value(1L))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cards/{cardId}")
    inner class DeleteCard {

        @Test
        @DisplayName("카드를 삭제한다")
        fun deletesCard() {
            mockMvc.perform(delete("/api/v1/cards/1"))
                .andExpect(status().isNoContent)

            verify(cardCommandService).deleteCard(member, 1L)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image")
    inner class UploadCardImage {

        @Test
        @DisplayName("Presigned URL을 반환한다")
        fun returnsPresignedUrl() {
            val request = PresignedUrlRequest("image/png", 100)
            val response = PresignedUrlResponse("https://example.com/presigned", "https://example.com/public")
            given(cardCommandService.getPresignedUrl(any(PresignedUrlRequest::class.java))).willReturn(response)

            mockMvc.perform(
                post("/api/v1/cards/image")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.presignedUrl").value("https://example.com/presigned"))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = PresignedUrlRequest(null, 0)

            mockMvc.perform(
                post("/api/v1/cards/image")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image/ocr")
    inner class UploadCardImageForOcr {

        @Test
        @DisplayName("OCR용 Presigned URL을 반환한다")
        fun returnsPresignedUrl() {
            val request = PresignedUrlRequest("image/png", 100)
            val response = PresignedUrlResponse("https://example.com/presigned", "https://example.com/public")
            given(cardCommandService.getPresignedUrlForOcr(any(PresignedUrlRequest::class.java))).willReturn(response)

            mockMvc.perform(
                post("/api/v1/cards/image/ocr")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.presignedUrl").value("https://example.com/presigned"))
        }
    }
}
