package org.veri.be.slice.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.veri.be.api.social.SocialCardController;
import org.veri.be.domain.card.controller.dto.response.CardVisibilityUpdateResponse;
import org.veri.be.domain.card.controller.enums.CardSortType;
import org.veri.be.domain.card.repository.dto.CardFeedItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.veri.be.domain.card.service.CardCommandService;
import org.veri.be.domain.card.service.CardQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.lib.response.ApiResponseAdvice;

@ExtendWith(MockitoExtension.class)
class SocialCardControllerTest {

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

        SocialCardController controller = new SocialCardController(cardCommandService, cardQueryService);
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
    @DisplayName("GET /api/v1/cards")
    class GetCards {

        @Test
        @DisplayName("카드 피드를 반환한다")
        void returnsCardFeed() throws Exception {
            CardFeedItem item = new CardFeedItem(
                    10L,
                    member,
                    "book",
                    "content",
                    "https://example.com/card.png",
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    true
            );
            PageImpl<CardFeedItem> page = new PageImpl<>(
                    List.of(item),
                    PageRequest.of(0, 10),
                    1
            );
            given(cardQueryService.getAllCards(0, 10, CardSortType.NEWEST)).willReturn(page);

            mockMvc.perform(get("/api/v1/cards")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort", "newest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.cards[0].cardId").value(10L));

            verify(cardQueryService).getAllCards(0, 10, CardSortType.NEWEST);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}/visibility")
    class ModifyVisibility {

        @Test
        @DisplayName("카드 공개 여부를 수정한다")
        void updatesVisibility() throws Exception {
            given(cardCommandService.modifyVisibility(member, 20L, true))
                    .willReturn(new CardVisibilityUpdateResponse(20L, true));

            mockMvc.perform(patch("/api/v1/cards/20/visibility")
                            .param("isPublic", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.isPublic").value(true));
        }
    }
}
