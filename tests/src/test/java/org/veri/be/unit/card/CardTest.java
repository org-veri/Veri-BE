package org.veri.be.unit.card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.entity.CardErrorInfo;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.support.assertion.ExceptionAssertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class CardTest {

    @Nested
    @DisplayName("authorizeMember")
    class AuthorizeMember {

        @Test
        @DisplayName("다른 멤버면 false를 반환한다")
        void throwsWhenNotOwner() {
            Member owner = member(1L, "owner@test.com", "owner");
            Member other = member(2L, "other@test.com", "other");
            Card card = Card.builder()
                    .member(owner)
                    .build();

            assertThat(card.authorizeMember(other.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("updateContent")
    class UpdateContent {

        @Test
        @DisplayName("작성자가 수정하면 내용과 이미지가 변경된다")
        void updatesContent() {
            Member owner = member(1L, "owner@test.com", "owner");
            Card card = Card.builder()
                    .member(owner)
                    .content("before")
                    .image("https://example.com/before.png")
                    .build();

            Card updated = card.updateContent("after", "https://example.com/after.png", owner);

            assertThat(updated.getContent()).isEqualTo("after");
            assertThat(updated.getImage()).isEqualTo("https://example.com/after.png");
        }
    }

    @Nested
    @DisplayName("changeVisibility")
    class ChangeVisibility {

        @Test
        @DisplayName("공개 요청 시 독서가 비공개면 예외가 발생한다")
        void throwsWhenReadingPrivate() {
            Member owner = member(1L, "owner@test.com", "owner");
            Reading reading = Reading.builder()
                    .id(10L)
                    .isPublic(false)
                    .build();
            Card card = Card.builder()
                    .member(owner)
                    .reading(reading)
                    .isPublic(false)
                    .build();

            ExceptionAssertions.assertApplicationException(
                    () -> card.changeVisibility(owner, true),
                    CardErrorInfo.READING_MS_NOT_PUBLIC
            );
        }

        @Test
        @DisplayName("비공개 요청 시 isPublic이 false로 변경된다")
        void setsPrivate() {
            Member owner = member(1L, "owner@test.com", "owner");
            Reading reading = Reading.builder()
                    .id(10L)
                    .isPublic(true)
                    .build();
            Card card = Card.builder()
                    .member(owner)
                    .reading(reading)
                    .isPublic(true)
                    .build();

            card.changeVisibility(owner, false);

            assertThat(card.isPublic()).isFalse();
        }
    }

    @Nested
    @DisplayName("assertReadableBy")
    class AssertReadableBy {

        @Test
        @DisplayName("공개 카드면 누구나 읽을 수 있다")
        void allowsWhenPublic() {
            Card card = Card.builder()
                    .isPublic(true)
                    .build();

            card.assertReadableBy(null);
        }

        @Test
        @DisplayName("비공개 카드는 작성자만 읽을 수 있다")
        void throwsWhenPrivateAndNoViewer() {
            var owner = member(1L, "owner", "owner");
            var reader = member(2L, "reader", "reader");

            Card card = Card.builder()
                    .member(owner)
                    .isPublic(false)
                    .build();

            assertThatNoException().isThrownBy(() -> card.assertReadableBy(owner));
            ExceptionAssertions.assertApplicationException(
                    () -> card.assertReadableBy(reader),
                    CardErrorInfo.READING_MS_NOT_PUBLIC
            );
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
