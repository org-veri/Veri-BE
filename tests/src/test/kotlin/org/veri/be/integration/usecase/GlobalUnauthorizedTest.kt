package org.veri.be.integration.usecase

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.dto.book.AddBookRequest
import org.veri.be.global.auth.context.MemberContext
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.steps.BookshelfSteps
import org.veri.be.support.steps.MemberSteps

class GlobalUnauthorizedTest : IntegrationTestSupport() {

    @BeforeEach
    fun clearAuth() {
        MemberContext.clear()
    }

    @Test
    @DisplayName("토큰 없이 /me 호출하면 → 401을 반환한다")
    fun getMeUnauthorized() {
        MemberSteps.getMyInfo(mockMvc)
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("인증 없이 /bookshelf 추가하면 → 401을 반환한다")
    fun addBookUnauthorized() {
        val request = AddBookRequest("T", "I", "A", "P", "1", false)

        BookshelfSteps.addBook(mockMvc, objectMapper, request)
            .andExpect(status().isUnauthorized)
    }
}
