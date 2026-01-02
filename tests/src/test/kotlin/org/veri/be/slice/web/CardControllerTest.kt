package org.veri.be.slice.web

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.personal.CardController
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest
import org.veri.be.domain.card.controller.dto.request.CardUpdateRequest
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse
import org.veri.be.domain.card.repository.dto.CardListItem
import org.veri.be.domain.card.service.CardCommandService
import org.veri.be.domain.card.service.CardQueryService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.dto.PresignedUrlResponse
import org.veri.be.lib.response.ApiResponseAdvice
import org.veri.be.support.ControllerTestSupport
import org.veri.be.support.fixture.MemberFixture
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CardControllerTest : ControllerTestSupport() {

    @org.mockito.Mock
    private lateinit var cardCommandService: CardCommandService

    @org.mockito.Mock
    private lateinit var cardQueryService: CardQueryService

    private lateinit var member: Member
    private lateinit var memberInfo: CurrentMemberInfo

    @BeforeEach
    fun setUp() {
        member = MemberFixture.aMember()
            .id(1L)
            .providerType(ProviderType.KAKAO)
            .build()

        memberInfo = CurrentMemberInfo.from(JwtClaimsPayload(member.id, member.email, member.nickname, false))
        val controller = CardController(cardCommandService, cardQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(testMemberAccessor(memberInfo))
            )
            .build()
    }

    private fun testMemberAccessor(memberInfo: CurrentMemberInfo): CurrentMemberAccessor {
        return object : CurrentMemberAccessor {
            override fun getCurrentMemberInfoOrNull() = memberInfo
            override fun getCurrentMember() = Optional.empty<Member>()
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards")
    inner class CreateCard {

        @Test
        @DisplayName("카드를 생성하면 → 결과를 반환한다")
        fun createsCard() {
            val request = CardCreateRequest("content", "https://example.com/card.png", 10L, true)
            given(cardCommandService.createCard(member.id, "content", "https://example.com/card.png", 10L, true))
                .willReturn(1L)

            postJson("/api/v1/cards", request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result.cardId").value(1L))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 → 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = CardCreateRequest(null, null, null, null)

            postJson("/api/v1/cards", request)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my/count")
    inner class GetMyCardCount {

        @Test
        @DisplayName("요청하면 → 내 카드 개수를 반환한다")
        fun returnsCount() {
            given(cardQueryService.getOwnedCardCount(1L)).willReturn(3)

            get("/api/v1/cards/my/count")
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(3))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my")
    inner class GetMyCards {

        @Test
        @DisplayName("요청하면 → 내 카드 목록을 반환한다")
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

            get(
                "/api/v1/cards/my",
                mapOf(
                    "page" to "1",
                    "size" to "10",
                    "sort" to "newest"
                )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards[0].cardId").value(1L))
        }

        @Test
        @DisplayName("page가 0 이하면 → 400을 반환한다")
        fun returns400WhenPageIsInvalid() {
            get(
                "/api/v1/cards/my",
                mapOf(
                    "page" to "0",
                    "size" to "10"
                )
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("size가 0 이하면 → 400을 반환한다")
        fun returns400WhenSizeIsInvalid() {
            get(
                "/api/v1/cards/my",
                mapOf(
                    "page" to "1",
                    "size" to "0"
                )
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/{cardId}")
    inner class GetCard {

        @Test
        @DisplayName("카드를 조회하면 → 상세를 반환한다")
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
            given(cardQueryService.getCardDetail(1L, member.id)).willReturn(response)

            get("/api/v1/cards/1")
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.id").value(1L))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}")
    inner class UpdateCard {

        @Test
        @DisplayName("카드를 수정하면 → 결과를 반환한다")
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
            given(cardCommandService.updateCard(member.id, 1L, "content", "https://example.com/card.png"))
                .willReturn(response)

            patchJson("/api/v1/cards/1", request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.id").value(1L))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cards/{cardId}")
    inner class DeleteCard {

        @Test
        @DisplayName("카드를 삭제하면 → 204를 반환한다")
        fun deletesCard() {
            delete("/api/v1/cards/1")
                .andExpect(status().isNoContent)

            then(cardCommandService).should().deleteCard(member.id, 1L)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image")
    inner class UploadCardImage {

        @Test
        @DisplayName("요청하면 → Presigned URL을 반환한다")
        fun returnsPresignedUrl() {
            val request = PresignedUrlRequest("image/png", 100)
            val response = PresignedUrlResponse("https://example.com/presigned", "https://example.com/public")
            given(cardCommandService.getPresignedUrl(any(PresignedUrlRequest::class.java))).willReturn(response)

            postJson("/api/v1/cards/image", request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.presignedUrl").value("https://example.com/presigned"))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 → 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = PresignedUrlRequest(null, 0)

            postJson("/api/v1/cards/image", request)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image/ocr")
    inner class UploadCardImageForOcr {

        @Test
        @DisplayName("요청하면 → OCR용 Presigned URL을 반환한다")
        fun returnsPresignedUrl() {
            val request = PresignedUrlRequest("image/png", 100)
            val response = PresignedUrlResponse("https://example.com/presigned", "https://example.com/public")
            given(cardCommandService.getPresignedUrlForOcr(any(PresignedUrlRequest::class.java))).willReturn(response)

            postJson("/api/v1/cards/image/ocr", request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.presignedUrl").value("https://example.com/presigned"))
        }
    }
}
