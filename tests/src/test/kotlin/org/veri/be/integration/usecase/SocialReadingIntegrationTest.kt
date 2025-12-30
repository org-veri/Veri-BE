package org.veri.be.integration.usecase

import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.integration.IntegrationTestSupport

class SocialReadingIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var readingRepository: ReadingRepository

    @Nested
    @DisplayName("GET /api/v2/bookshelf/popular")
    inner class GetPopular {
        @Test
        @DisplayName("인기 도서 10개 조회")
        fun getPopularSuccess() {
            mockMvc.perform(get("/api/v2/bookshelf/popular"))
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("최근 일주일 내 추가 도서 없음")
        fun getPopularEmpty() {
            mockMvc.perform(get("/api/v2/bookshelf/popular"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books").isEmpty)
        }

        @Test
        @DisplayName("인기 도서 최대 10개 제한")
        fun getPopularLimit() {
            for (i in 0 until 12) {
                createReading(true, getMockMember(), "ISBN$i")
            }

            mockMvc.perform(get("/api/v2/bookshelf/popular"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books.length()").value(Matchers.lessThanOrEqualTo(10)))
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/{readingId}")
    inner class GetReadingDetail {
        @Test
        @DisplayName("공개 독서 상세")
        fun getReadingDetailSuccess() {
            val reading = createReading(true, getMockMember(), "ISBN-DETAIL")

            mockMvc.perform(get("/api/v2/bookshelf/${reading.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.memberBookId").value(reading.id))
        }

        @Test
        @DisplayName("비공개 + 소유자 조회")
        fun getPrivateOwner() {
            val reading = createReading(false, getMockMember(), "ISBN-PRIVATE")

            mockMvc.perform(get("/api/v2/bookshelf/${reading.id}"))
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("비공개 + 타인 접근")
        fun getPrivateOther() {
            val other = Member.builder()
                .email("o")
                .nickname("o")
                .profileImageUrl("p")
                .providerId("p")
                .providerType(ProviderType.KAKAO)
                .build()
            memberRepository.save(other)
            val reading = createReading(false, other, "ISBN-OTHER")

            mockMvc.perform(get("/api/v2/bookshelf/${reading.id}"))
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("존재하지 않는 ID")
        fun getNotFound() {
            mockMvc.perform(get("/api/v2/bookshelf/999"))
                .andExpect(status().isBadRequest)
        }
    }

    private fun createReading(isPublic: Boolean, member: Member, isbn: String): Reading {
        var book = Book.builder().title("T").image("I").isbn(isbn).build()
        book = bookRepository.save(book)
        val reading = Reading.builder()
            .member(member)
            .book(book)
            .isPublic(isPublic)
            .build()
        return readingRepository.save(reading)
    }
}
