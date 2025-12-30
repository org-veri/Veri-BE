package org.veri.be.slice.web

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.personal.CardControllerV2
import org.veri.be.domain.card.service.CardCommandService
import org.veri.be.global.storage.dto.PresignedPostFormResponse
import org.veri.be.lib.response.ApiResponseAdvice

@ExtendWith(MockitoExtension::class)
class CardControllerV2Test {

    private lateinit var mockMvc: MockMvc

    @org.mockito.Mock
    private lateinit var cardCommandService: CardCommandService

    @BeforeEach
    fun setUp() {
        val controller = CardControllerV2(cardCommandService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .build()
    }

    @Nested
    @DisplayName("POST /api/v2/cards/image")
    inner class UploadCardImageV2 {

        @Test
        @DisplayName("presigned post form을 반환한다")
        fun returnsPresignedPostForm() {
            val form = mock(PresignedPostFormResponse::class.java)
            given(cardCommandService.getPresignedPost()).willReturn(form)

            mockMvc.perform(post("/api/v2/cards/image"))
                .andExpect(status().isOk)

            verify(cardCommandService).getPresignedPost()
        }
    }
}
