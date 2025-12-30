package org.veri.be.mock;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.veri.be.book.entity.Book;
import org.veri.be.book.entity.Reading;
import org.veri.be.card.entity.Card;
import org.veri.be.member.entity.Member;
import org.veri.be.member.entity.enums.ProviderType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Configuration
@RequiredArgsConstructor
public class InsertMockData {

    private final EntityManager em;

    @EventListener(value = ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        if (em.createQuery("SELECT COUNT(m) FROM Member m", Long.class).getSingleResult() > 0) {
            return; // 데이터가 이미 존재하면 초기화하지 않음
        }

        Member member = Member.builder()
                .email("test@test.com")
                .nickname("테스트")
                .profileImageUrl("https://example.com/image.jpg")
                .providerId("test-provider-id")
                .providerType(ProviderType.KAKAO)
                .build();

        Member member2 = Member.builder()
                .email("d")
                .nickname("d")
                .profileImageUrl("https://example.com/image.jpg")
                .providerId("d")
                .providerType(ProviderType.KAKAO)
                .build();

        em.persist(member);
        em.persist(member2);

        Book book = Book.builder()
                .title("테스트 책")
                .image("https://example.com/book-image.jpg")
                .author("<UNK> <UNK>")
                .publisher("member")
                .isbn("123456789")
                .build();

        em.persist(book);

        Reading reading = Reading.builder()
                .member(member)
                .book(book)
                .build();

        Reading reading2 = Reading.builder()
                .member(member2)
                .book(book)
                .build();

        em.persist(reading);
        em.persist(reading2);

        String exampleUrlPrefix = "https://example.com/card-image-";
        String exampleContentPrefix = "테스트 카드 내용 ";
        for (int i = 0; i < 5; i++) {
            Card card = Card.builder()
                    .image(exampleUrlPrefix + i + ".jpg")
                    .content(exampleContentPrefix + i)
                    .reading(reading)
                    .member(member)
                    .build();
            em.persist(card);

            Card card2 = Card.builder()
                    .image(exampleUrlPrefix + i + ".jpg")
                    .content(exampleContentPrefix + i)
                    .reading(reading2)
                    .member(member2)
                    .build();
            em.persist(card2);
        }

        for (int i = 5; i < 10; i++) {
            Card card = Card.builder()
                    .image(exampleUrlPrefix + i + ".jpg")
                    .content(exampleContentPrefix + i)
                    .reading(reading)
                    .member(member)
                    .isPublic(true)
                    .build();
            em.persist(card);

            Card card2 = Card.builder()
                    .image(exampleUrlPrefix + i + ".jpg")
                    .content(exampleContentPrefix + i)
                    .reading(reading2)
                    .member(member2)
                    .isPublic(true)
                    .build();
            em.persist(card2);
        }
    }
}
