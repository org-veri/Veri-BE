package org.veri.be.integration.usecase

import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.ReadingFixture
import org.veri.be.support.steps.SocialReadingSteps

class SocialReadingIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var readingRepository: ReadingRepository

    @Nested
    @DisplayName("GET /api/v2/bookshelf/popular")
    inner class GetPopular {
        @Test
        @DisplayName("요청하면 → 인기 도서를 조회한다")
        fun getPopularSuccess() {
            SocialReadingSteps.getPopular(mockMvc)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("최근 일주일 내 추가 도서가 없으면 → 빈 목록을 반환한다")
        fun getPopularEmpty() {
            SocialReadingSteps.getPopular(mockMvc)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books").isEmpty)
        }

        @Test
        @DisplayName("인기 도서는 → 최대 10개로 제한된다")
        fun getPopularLimit() {
            for (i in 0 until 12) {
                createReading(true, getMockMember(), "ISBN$i")
            }

            SocialReadingSteps.getPopular(mockMvc)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books.length()").value(Matchers.lessThanOrEqualTo(10)))
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/{readingId}")
    inner class GetReadingDetail {
        @Test
        @DisplayName("공개 독서 상세를 조회하면 → 정보를 반환한다")
        fun getReadingDetailSuccess() {
            val reading = createReading(true, getMockMember(), "ISBN-DETAIL")

            SocialReadingSteps.getDetail(mockMvc, reading.id)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.memberBookId").value(reading.id))
        }

        @Test
        @DisplayName("비공개 + 소유자 조회면 → 200을 반환한다")
        fun getPrivateOwner() {
            val reading = createReading(false, getMockMember(), "ISBN-PRIVATE")

            SocialReadingSteps.getDetail(mockMvc, reading.id)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("비공개 + 타인 접근이면 → 404를 반환한다")
        fun getPrivateOther() {
            val other = MemberFixture.aMember()
                .email("o")
                .nickname("o")
                .profileImageUrl("p")
                .providerId("p")
                .providerType(ProviderType.KAKAO)
                .build()
            memberRepository.save(other)
            val reading = createReading(false, other, "ISBN-OTHER")

            SocialReadingSteps.getDetail(mockMvc, reading.id)
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("존재하지 않는 ID면 → 400을 반환한다")
        fun getNotFound() {
            SocialReadingSteps.getDetail(mockMvc, 999L)
                .andExpect(status().isBadRequest)
        }
    }

    private fun createReading(isPublic: Boolean, member: org.veri.be.domain.member.entity.Member, isbn: String): org.veri.be.domain.book.entity.Reading {
        var book = BookFixture.aBook().title("T").image("I").isbn(isbn).build()
        book = bookRepository.save(book)
        val reading = ReadingFixture.aReading()
            .member(member)
            .book(book)
            .isPublic(isPublic)
            .build()
        return readingRepository.save(reading)
    }
}
