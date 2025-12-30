package org.veri.be.slice.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
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
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.MemberContext
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor
import org.veri.be.lib.response.ApiResponseAdvice
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class SocialCardControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @org.mockito.Mock
    private lateinit var cardCommandService: CardCommandService

    @org.mockito.Mock
    private lateinit var cardQueryService: CardQueryService

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
        MemberContext.setCurrentMember(member)

        val controller = SocialCardController(cardCommandService, cardQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(ThreadLocalCurrentMemberAccessor(null))
            )
            .build()
    }

    @AfterEach
    fun tearDown() {
        MemberContext.clear()
    }

    @Nested
    @DisplayName("GET /api/v1/cards")
    inner class GetCards {

        @Test
        @DisplayName("카드 피드를 반환한다")
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

            mockMvc.perform(
                get("/api/v1/cards")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "newest")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards[0].cardId").value(10L))

            verify(cardQueryService).getAllCards(0, 10, CardSortType.NEWEST)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}/visibility")
    inner class ModifyVisibility {

        @Test
        @DisplayName("카드 공개 여부를 수정한다")
        fun updatesVisibility() {
            given(cardCommandService.modifyVisibility(member, 20L, true))
                .willReturn(CardVisibilityUpdateResponse(20L, true))

            mockMvc.perform(
                patch("/api/v1/cards/20/visibility")
                    .param("isPublic", "true")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(true))
        }
    }
}
