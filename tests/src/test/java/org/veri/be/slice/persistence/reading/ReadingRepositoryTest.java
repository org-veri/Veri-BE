package org.veri.be.slice.persistence.reading;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.veri.be.domain.book.repository.dto.ReadingQueryResult;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class ReadingRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    ReadingRepository readingRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CardRepository cardRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    @DisplayName("findByIdWithCardsAndBook")
    class FindByIdWithCardsAndBook {

        @Test
        @DisplayName("독서와 카드, 책을 fetch join으로 조회한다")
        void fetchesCardsAndBook() {
            Member member = saveMember("reading@test.com", "reader");
            Book book = saveBook("isbn-1", "book-1");
            Reading reading = saveReading(member, book, ReadingStatus.READING);
            saveCard(member, reading, "content-1");
            saveCard(member, reading, "content-2");

            entityManager.flush();
            entityManager.clear();

            Reading found = readingRepository.findByIdWithCardsAndBook(reading.getId()).orElseThrow();
            PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

            assertThat(util.isLoaded(found.getCards())).isTrue();
            assertThat(util.isLoaded(found.getBook())).isTrue();
            assertThat(found.getCards()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findReadingPage")
    class FindReadingPage {

        @Test
        @DisplayName("상태 조건에 맞는 독서 페이징을 반환한다")
        void returnsReadingPageForStatuses() {
            Member member = saveMember("reading@test.com", "reader");
            Book book = saveBook("isbn-1", "book-1");
            saveReading(member, book, ReadingStatus.READING);
            saveReading(member, book, ReadingStatus.DONE);
            saveReading(member, book, ReadingStatus.NOT_START);

            Page<ReadingQueryResult> page = readingRepository.findReadingPage(
                    member.getId(),
                    List.of(ReadingStatus.READING, ReadingStatus.DONE),
                    PageRequest.of(0, 10, Sort.by("id").ascending())
            );

            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent()).allMatch(item ->
                    item.status() == ReadingStatus.READING || item.status() == ReadingStatus.DONE
            );
        }
    }

    @Nested
    @DisplayName("countByStatusAndMember")
    class CountByStatusAndMember {

        @Test
        @DisplayName("상태와 회원 조건에 해당하는 독서 개수를 반환한다")
        void returnsCountByStatusAndMember() {
            Member member = saveMember("reading@test.com", "reader");
            Book book = saveBook("isbn-1", "book-1");
            saveReading(member, book, ReadingStatus.READING);
            saveReading(member, book, ReadingStatus.DONE);

            int count = readingRepository.countByStatusAndMember(ReadingStatus.READING, member.getId());

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findByMemberAndBook")
    class FindByMemberAndBook {

        @Test
        @DisplayName("회원과 도서로 독서를 조회한다")
        void returnsReadingByMemberAndBook() {
            Member member = saveMember("reading@test.com", "reader");
            Book book = saveBook("isbn-1", "book-1");
            Reading reading = saveReading(member, book, ReadingStatus.READING);

            Reading found = readingRepository.findByMemberAndBook(member.getId(), book.getId()).orElseThrow();

            assertThat(found.getId()).isEqualTo(reading.getId());
        }
    }

    @Nested
    @DisplayName("findByAuthorAndTitle")
    class FindByAuthorAndTitle {

        @Test
        @DisplayName("회원과 도서의 저자/제목으로 독서를 조회한다")
        void returnsReadingByAuthorAndTitle() {
            Member member = saveMember("reading@test.com", "reader");
            Book book = saveBook("isbn-1", "book-1");
            Reading reading = saveReading(member, book, ReadingStatus.READING);

            Reading found = readingRepository.findByAuthorAndTitle(member.getId(), "book-1", "author")
                    .orElseThrow();

            assertThat(found.getId()).isEqualTo(reading.getId());
        }
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build());
    }

    private Book saveBook(String isbn, String title) {
        return bookRepository.save(Book.builder()
                .image("https://example.com/book.png")
                .title(title)
                .author("author")
                .isbn(isbn)
                .build());
    }

    private Reading saveReading(Member member, Book book, ReadingStatus status) {
        return readingRepository.save(Reading.builder()
                .member(member)
                .book(book)
                .status(status)
                .isPublic(true)
                .build());
    }

    private Card saveCard(Member member, Reading reading, String content) {
        return cardRepository.save(Card.builder()
                .member(member)
                .reading(reading)
                .content(content)
                .image("https://example.com/card.png")
                .isPublic(true)
                .build());
    }
}
