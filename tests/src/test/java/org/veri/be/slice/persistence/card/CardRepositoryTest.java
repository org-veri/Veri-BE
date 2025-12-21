package org.veri.be.slice.persistence.card;

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
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.card.repository.dto.CardFeedItem;
import org.veri.be.domain.card.repository.dto.CardListItem;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class CardRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    CardRepository cardRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReadingRepository readingRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    @DisplayName("findAllByMemberId")
    class FindAllByMemberId {

        @Test
        @DisplayName("memberId에 해당하는 카드만 조회한다")
        void returnsCardsForMember() {
            Member member = saveMember("member-1@test.com", "member-1");
            Member other = saveMember("member-2@test.com", "member-2");
            Reading reading = saveReading(member, saveBook("isbn-1", "book-1"));
            Reading otherReading = saveReading(other, saveBook("isbn-2", "book-2"));

            Card card1 = saveCard(member, reading, true, "content-1");
            Card card2 = saveCard(member, reading, false, "content-2");
            saveCard(other, otherReading, true, "content-3");

            Page<CardListItem> result = cardRepository.findAllByMemberId(
                    member.getId(),
                    PageRequest.of(0, 10, Sort.by("id").ascending())
            );

            List<Long> cardIds = result.map(CardListItem::getCardId).getContent();
            assertThat(cardIds).containsExactlyInAnyOrder(card1.getId(), card2.getId());
        }
    }

    @Nested
    @DisplayName("countAllByMemberId")
    class CountAllByMemberId {

        @Test
        @DisplayName("memberId에 해당하는 카드 개수를 반환한다")
        void returnsCardCountForMember() {
            Member member = saveMember("member-1@test.com", "member-1");
            Member other = saveMember("member-2@test.com", "member-2");
            Reading reading = saveReading(member, saveBook("isbn-1", "book-1"));
            Reading otherReading = saveReading(other, saveBook("isbn-2", "book-2"));

            saveCard(member, reading, true, "content-1");
            saveCard(member, reading, false, "content-2");
            saveCard(other, otherReading, true, "content-3");

            int count = cardRepository.countAllByMemberId(member.getId());

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findAllPublicItems")
    class FindAllPublicItems {

        @Test
        @DisplayName("공개 카드만 페이징 조회한다")
        void returnsOnlyPublicCards() {
            Member member = saveMember("member-1@test.com", "member-1");
            Reading reading = saveReading(member, saveBook("isbn-1", "book-1"));

            Card publicCard1 = saveCard(member, reading, true, "content-1");
            Card publicCard2 = saveCard(member, reading, true, "content-2");
            saveCard(member, reading, false, "content-3");

            Page<CardFeedItem> result = cardRepository.findAllPublicItems(
                    PageRequest.of(0, 10, Sort.by("id").ascending())
            );

            List<Long> cardIds = result.map(CardFeedItem::cardId).getContent();
            assertThat(cardIds).containsExactlyInAnyOrder(publicCard1.getId(), publicCard2.getId());
        }
    }

    @Nested
    @DisplayName("findByIdWithAllAssociations")
    class FindByIdWithAllAssociations {

        @Test
        @DisplayName("member, reading, book을 fetch join으로 조회한다")
        void fetchesAllAssociations() {
            Member member = saveMember("member-1@test.com", "member-1");
            Book book = saveBook("isbn-1", "book-1");
            Reading reading = saveReading(member, book);
            Card card = saveCard(member, reading, true, "content-1");

            entityManager.flush();
            entityManager.clear();

            Card found = cardRepository.findByIdWithAllAssociations(card.getId()).orElseThrow();
            PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

            assertThat(util.isLoaded(found.getMember())).isTrue();
            assertThat(util.isLoaded(found.getReading())).isTrue();
            assertThat(util.isLoaded(found.getReading().getBook())).isTrue();
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

    private Reading saveReading(Member member, Book book) {
        return readingRepository.save(Reading.builder()
                .member(member)
                .book(book)
                .isPublic(true)
                .build());
    }

    private Card saveCard(Member member, Reading reading, boolean isPublic, String content) {
        return cardRepository.save(Card.builder()
                .member(member)
                .reading(reading)
                .content(content)
                .image("https://example.com/card.png")
                .isPublic(isPublic)
                .build());
    }
}
