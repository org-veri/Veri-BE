package org.veri.be.slice.persistence.card

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.card.repository.CardRepository
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.CardFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.ReadingFixture

class CardEntityMappingTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var readingRepository: ReadingRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Nested
    @DisplayName("reading 연관관계")
    inner class ReadingAssociation {

        @Test
        @DisplayName("독서를 삭제하면 → 카드의 reading_id가 null로 변경된다")
        fun setsReadingToNullOnDelete() {
            val member = memberRepository.save(
                MemberFixture.aMember()
                    .email("member@test.com")
                    .nickname("tester")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-id")
                    .providerType(ProviderType.KAKAO)
                    .build()
            )
            val book = bookRepository.save(
                BookFixture.aBook()
                    .image("https://example.com/book.png")
                    .title("book")
                    .author("author")
                    .isbn("isbn-1")
                    .build()
            )
            val reading = readingRepository.save(
                ReadingFixture.aReading()
                    .member(member)
                    .book(book)
                    .isPublic(true)
                    .build()
            )
            val card = cardRepository.save(
                CardFixture.aCard()
                    .member(member)
                    .reading(reading)
                    .content("content")
                    .image("https://example.com/card.png")
                    .isPublic(true)
                    .build()
            )

            entityManager.flush()
            entityManager.clear()

            entityManager.createNativeQuery("DELETE FROM reading WHERE id = :id")
                .setParameter("id", reading.id)
                .executeUpdate()
            entityManager.clear()

            val found = cardRepository.findById(card.id).orElseThrow()
            assertThat(found.reading).isNull()
        }
    }
}
