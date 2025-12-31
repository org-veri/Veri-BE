package org.veri.be.slice.persistence.reading

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceUnitUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.veri.be.book.entity.Book
import org.veri.be.book.entity.Reading
import org.veri.be.book.entity.enums.ReadingStatus
import org.veri.be.book.service.BookRepository
import org.veri.be.book.service.ReadingRepository
import org.veri.be.book.repository.dto.ReadingQueryResult
import org.veri.be.card.entity.Card
import org.veri.be.card.service.CardRepository
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.service.MemberRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport

class ReadingRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var readingRepository: ReadingRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Nested
    @DisplayName("findByIdWithBook")
    inner class FindByIdWithBook {

        @Test
        @DisplayName("독서와 책을 fetch join으로 조회한다")
        fun fetchesBook() {
            val member = saveMember("reading@test.com", "reader")
            val book = saveBook("isbn-1", "book-1")
            val reading = saveReading(member, book, ReadingStatus.READING)

            entityManager.flush()
            entityManager.clear()

            val found = readingRepository.findByIdWithBook(reading.id).orElseThrow()
            val util: PersistenceUnitUtil = entityManager.entityManagerFactory.persistenceUnitUtil

            assertThat(util.isLoaded(found.book)).isTrue()
        }
    }

    @Nested
    @DisplayName("findReadingPage")
    inner class FindReadingPage {

        @Test
        @DisplayName("상태 조건에 맞는 독서 페이징을 반환한다")
        fun returnsReadingPageForStatuses() {
            val member = saveMember("reading@test.com", "reader")
            val book = saveBook("isbn-1", "book-1")
            saveReading(member, book, ReadingStatus.READING)
            saveReading(member, book, ReadingStatus.DONE)
            saveReading(member, book, ReadingStatus.NOT_START)

            val page = readingRepository.findReadingPage(
                member.id,
                listOf(ReadingStatus.READING, ReadingStatus.DONE),
                PageRequest.of(0, 10, Sort.by("id").ascending())
            )

            assertThat(page.totalElements).isEqualTo(2)
            assertThat(page.content).allMatch { item: ReadingQueryResult ->
                item.status() == ReadingStatus.READING || item.status() == ReadingStatus.DONE
            }
        }
    }

    @Nested
    @DisplayName("countByStatusAndMember")
    inner class CountByStatusAndMember {

        @Test
        @DisplayName("상태와 회원 조건에 해당하는 독서 개수를 반환한다")
        fun returnsCountByStatusAndMember() {
            val member = saveMember("reading@test.com", "reader")
            val book = saveBook("isbn-1", "book-1")
            saveReading(member, book, ReadingStatus.READING)
            saveReading(member, book, ReadingStatus.DONE)

            val count = readingRepository.countByStatusAndMember(ReadingStatus.READING, member.id)

            assertThat(count).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("findByMemberAndBook")
    inner class FindByMemberAndBook {

        @Test
        @DisplayName("회원과 도서로 독서를 조회한다")
        fun returnsReadingByMemberAndBook() {
            val member = saveMember("reading@test.com", "reader")
            val book = saveBook("isbn-1", "book-1")
            val reading = saveReading(member, book, ReadingStatus.READING)

            val found = readingRepository.findByMemberAndBook(member.id, book.id).orElseThrow()

            assertThat(found.id).isEqualTo(reading.id)
        }
    }

    @Nested
    @DisplayName("findByAuthorAndTitle")
    inner class FindByAuthorAndTitle {

        @Test
        @DisplayName("회원과 도서의 저자/제목으로 독서를 조회한다")
        fun returnsReadingByAuthorAndTitle() {
            val member = saveMember("reading@test.com", "reader")
            val book = saveBook("isbn-1", "book-1")
            val reading = saveReading(member, book, ReadingStatus.READING)

            val found = readingRepository.findByAuthorAndTitle(member.id, "book-1", "author")
                .orElseThrow()

            assertThat(found.id).isEqualTo(reading.id)
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

    private fun saveReading(member: Member, book: Book, status: ReadingStatus): Reading {
        return readingRepository.save(
            Reading.builder()
                .member(member)
                .book(book)
                .status(status)
                .isPublic(true)
                .build()
        )
    }

    private fun saveCard(member: Member, reading: Reading, content: String): Card {
        return cardRepository.save(
            Card.builder()
                .member(member)
                .reading(reading)
                .content(content)
                .image("https://example.com/card.png")
                .isPublic(true)
                .build()
        )
    }
}
