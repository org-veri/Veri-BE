package org.veri.be.slice.web

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.social.SocialCardController
import org.veri.be.domain.card.controller.dto.response.CardVisibilityUpdateResponse
import org.veri.be.domain.card.controller.enums.CardSortType
import org.veri.be.domain.card.repository.dto.CardFeedItem
import org.veri.be.domain.card.service.CardCommandService
import org.veri.be.domain.card.service.CardQueryService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.lib.response.ApiResponseAdvice
import org.veri.be.support.ControllerTestSupport
import org.veri.be.support.fixture.MemberFixture
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class SocialCardControllerTest : ControllerTestSupport() {

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
        val controller = SocialCardController(cardCommandService, cardQueryService)
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
    @DisplayName("GET /api/v1/cards")
    inner class GetCards {

        @Test
        @DisplayName("요청하면 → 카드 피드를 반환한다")
        fun returnsCardFeed() {
            val item = CardFeedItem(
                10L,
                member,
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
            given(cardQueryService.getAllCards(0, 10, CardSortType.NEWEST)).willReturn(page)

            get(
                "/api/v1/cards",
                mapOf(
                    "page" to "1",
                    "size" to "10",
                    "sort" to "newest"
                )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards[0].cardId").value(10L))

            then(cardQueryService).should().getAllCards(0, 10, CardSortType.NEWEST)
        }

        @Test
        @DisplayName("page가 0 이하면 → 400을 반환한다")
        fun returns400WhenPageIsInvalid() {
            get(
                "/api/v1/cards",
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
                "/api/v1/cards",
                mapOf(
                    "page" to "1",
                    "size" to "0"
                )
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}/visibility")
    inner class ModifyVisibility {

        @Test
        @DisplayName("카드 공개 여부를 수정하면 → 결과를 반환한다")
        fun updatesVisibility() {
            given(cardCommandService.modifyVisibility(member.id, 20L, true))
                .willReturn(CardVisibilityUpdateResponse(20L, true))

            mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/cards/20/visibility")
                    .param("isPublic", "true")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(true))
        }
    }
}
