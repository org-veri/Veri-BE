package org.veri.be.slice.persistence.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class CardEntityMappingTest extends PersistenceSliceTestSupport {

    @Autowired
    CardRepository cardRepository;

    @Autowired
    ReadingRepository readingRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    @DisplayName("reading 연관관계")
    class ReadingAssociation {

        @Test
        @DisplayName("독서를 삭제하면 카드의 reading_id가 null로 변경된다")
        void setsReadingToNullOnDelete() {
            Member member = memberRepository.save(Member.builder()
                    .email("member@test.com")
                    .nickname("tester")
                    .profileImageUrl("https://example.com/profile.png")
                    .providerId("provider-id")
                    .providerType(ProviderType.KAKAO)
                    .build());
            Book book = bookRepository.save(Book.builder()
                    .image("https://example.com/book.png")
                    .title("book")
                    .author("author")
                    .isbn("isbn-1")
                    .build());
            Reading reading = readingRepository.save(Reading.builder()
                    .member(member)
                    .book(book)
                    .isPublic(true)
                    .build());
            Card card = cardRepository.save(Card.builder()
                    .member(member)
                    .reading(reading)
                    .content("content")
                    .image("https://example.com/card.png")
                    .isPublic(true)
                    .build());

            entityManager.flush();
            entityManager.clear();

            entityManager.createNativeQuery("DELETE FROM reading WHERE id = :id")
                    .setParameter("id", reading.getId())
                    .executeUpdate();
            entityManager.clear();

            Card found = cardRepository.findById(card.getId()).orElseThrow();
            assertThat(found.getReading()).isNull();
        }
    }
}
