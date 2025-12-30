package org.veri.be.integration.usecase

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.dto.book.AddBookRequest
import org.veri.be.global.auth.context.MemberContext
import org.veri.be.integration.IntegrationTestSupport

class GlobalUnauthorizedTest : IntegrationTestSupport() {

    @BeforeEach
    fun clearAuth() {
        MemberContext.clear()
    }

    @Test
    @DisplayName("GET /me | 토큰 없이 호출")
    fun getMeUnauthorized() {
        mockMvc.perform(get("/api/v1/members/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("POST /bookshelf | 인증 없이 추가 시도")
    fun addBookUnauthorized() {
        val request = AddBookRequest("T", "I", "A", "P", "1", false)

        mockMvc.perform(
            post("/api/v2/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }
}
