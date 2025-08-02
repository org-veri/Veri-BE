package org.goorm.veri.veribe.mock;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
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
                .image("https://example.com/image.jpg")
                .providerId("test-provider-id")
                .providerType(ProviderType.KAKAO)
                .build();

        em.persist(member);

//        Book book = Book.builder()
//                .title("테스트 책")
//                .image("https://example.com/book-image.jpg")
//                .author("<UNK> <UNK>")
//                .publisher("member")
//                .isbn("123456789")
//                .build();
//
//        em.persist(book);
//
//        MemberBook memberBook = MemberBook.builder()
//                .member(member)
//                .book(book)
//                .build();
//
//        em.persist(memberBook);
//
//        for (int i = 0; i < 10; i++) {
//            Card card = Card.builder()
//                    .image("https://example.com/card-image-" + i + ".jpg")
//                    .content("테스트 카드 내용 " + i)
//                    .memberBook(memberBook)
//                    .build();
//            em.persist(card);
//        }
    }
}
