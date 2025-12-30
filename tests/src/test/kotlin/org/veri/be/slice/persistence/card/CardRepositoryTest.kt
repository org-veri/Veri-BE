package org.veri.be.slice.persistence.card

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceUnitUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.card.entity.Card
import org.veri.be.domain.card.repository.CardRepository
import org.veri.be.domain.card.repository.dto.CardFeedItem
import org.veri.be.domain.card.repository.dto.CardListItem
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport

class CardRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var readingRepository: ReadingRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Nested
    @DisplayName("findAllByMemberId")
    inner class FindAllByMemberId {

        @Test
        @DisplayName("memberId에 해당하는 카드만 조회한다")
        fun returnsCardsForMember() {
            val member = saveMember("member-1@test.com", "member-1")
            val other = saveMember("member-2@test.com", "member-2")
            val reading = saveReading(member, saveBook("isbn-1", "book-1"))
            val otherReading = saveReading(other, saveBook("isbn-2", "book-2"))

            val card1 = saveCard(member, reading, true, "content-1")
            val card2 = saveCard(member, reading, false, "content-2")
            saveCard(other, otherReading, true, "content-3")

            val result = cardRepository.findAllByMemberId(
                member.id,
                PageRequest.of(0, 10, Sort.by("id").ascending())
            )

            val cardIds = result.map { item: CardListItem -> item.cardId }.content
            assertThat(cardIds).containsExactlyInAnyOrder(card1.id, card2.id)
        }
    }

    @Nested
    @DisplayName("countAllByMemberId")
    inner class CountAllByMemberId {

        @Test
        @DisplayName("memberId에 해당하는 카드 개수를 반환한다")
        fun returnsCardCountForMember() {
            val member = saveMember("member-1@test.com", "member-1")
            val other = saveMember("member-2@test.com", "member-2")
            val reading = saveReading(member, saveBook("isbn-1", "book-1"))
            val otherReading = saveReading(other, saveBook("isbn-2", "book-2"))

            saveCard(member, reading, true, "content-1")
            saveCard(member, reading, false, "content-2")
            saveCard(other, otherReading, true, "content-3")

            val count = cardRepository.countAllByMemberId(member.id)

            assertThat(count).isEqualTo(2)
        }
    }

    @Nested
    @DisplayName("findAllPublicItems")
    inner class FindAllPublicItems {

        @Test
        @DisplayName("공개 카드만 페이징 조회한다")
        fun returnsOnlyPublicCards() {
            val member = saveMember("member-1@test.com", "member-1")
            val reading = saveReading(member, saveBook("isbn-1", "book-1"))

            val publicCard1 = saveCard(member, reading, true, "content-1")
            val publicCard2 = saveCard(member, reading, true, "content-2")
            saveCard(member, reading, false, "content-3")

            val result = cardRepository.findAllPublicItems(
                PageRequest.of(0, 10, Sort.by("id").ascending())
            )

            val cardIds = result.map { item: CardFeedItem -> item.cardId }.content
            assertThat(cardIds).containsExactlyInAnyOrder(publicCard1.id, publicCard2.id)
        }
    }

    @Nested
    @DisplayName("findByIdWithAllAssociations")
    inner class FindByIdWithAllAssociations {

        @Test
        @DisplayName("member, reading, book을 fetch join으로 조회한다")
        fun fetchesAllAssociations() {
            val member = saveMember("member-1@test.com", "member-1")
            val book = saveBook("isbn-1", "book-1")
            val reading = saveReading(member, book)
            val card = saveCard(member, reading, true, "content-1")

            entityManager.flush()
            entityManager.clear()

            val found = cardRepository.findByIdWithAllAssociations(card.id).orElseThrow()
            val util: PersistenceUnitUtil = entityManager.entityManagerFactory.persistenceUnitUtil

            assertThat(util.isLoaded(found.member)).isTrue()
            assertThat(util.isLoaded(found.reading)).isTrue()
            assertThat(util.isLoaded(found.reading.book)).isTrue()
        }
    }

    private fun saveMember(email: String, nickname: String): Member {
        return memberRepository.save(
            Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-$nickname")
                .providerType(ProviderType.KAKAO)
                .build()
        )
    }

    private fun saveBook(isbn: String, title: String): Book {
        return bookRepository.save(
            Book.builder()
                .image("https://example.com/book.png")
                .title(title)
                .author("author")
                .isbn(isbn)
                .build()
        )
    }

    private fun saveReading(member: Member, book: Book): Reading {
        return readingRepository.save(
            Reading.builder()
                .member(member)
                .book(book)
                .isPublic(true)
                .build()
        )
    }

    private fun saveCard(member: Member, reading: Reading, isPublic: Boolean, content: String): Card {
        return cardRepository.save(
            Card.builder()
                .member(member)
                .reading(reading)
                .content(content)
                .image("https://example.com/card.png")
                .isPublic(isPublic)
                .build()
        )
    }
}
