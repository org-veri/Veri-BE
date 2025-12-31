package org.veri.be.slice.persistence.card

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veri.be.book.entity.Book
import org.veri.be.book.entity.Reading
import org.veri.be.book.service.BookRepository
import org.veri.be.book.service.ReadingRepository
import org.veri.be.card.entity.Card
import org.veri.be.card.service.CardRepository
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.service.MemberRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport

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
        @DisplayName("독서를 삭제하면 카드의 reading_id가 null로 변경된다")
        fun setsReadingToNullOnDelete() {
            val member = memberRepository.save(
                Member.builder()
                    .email("member@test.com")
                    .nickname("tester")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-id")
                    .providerType(ProviderType.KAKAO)
                    .build()
            )
            val book = bookRepository.save(
                Book.builder()
                    .image("https://example.com/book.png")
                    .title("book")
                    .author("author")
                    .isbn("isbn-1")
                    .build()
            )
            val reading = readingRepository.save(
                Reading.builder()
                    .member(member)
                    .book(book)
                    .isPublic(true)
                    .build()
            )
            val card = cardRepository.save(
                Card.builder()
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
