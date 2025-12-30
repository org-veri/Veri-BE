package org.veri.be.unit.card

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.card.entity.Card
import org.veri.be.domain.card.entity.CardErrorInfo
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.support.assertion.ExceptionAssertions

class CardTest {

    @Nested
    @DisplayName("authorizeMember")
    inner class AuthorizeMember {

        @Test
        @DisplayName("다른 멤버면 false를 반환한다")
        fun throwsWhenNotOwner() {
            val owner = member(1L, "owner@test.com", "owner")
            val other = member(2L, "other@test.com", "other")
            val card = Card.builder()
                .member(owner)
                .build()

            assertThat(card.authorizeMember(other.id)).isFalse()
        }
    }

    @Nested
    @DisplayName("updateContent")
    inner class UpdateContent {

        @Test
        @DisplayName("작성자가 수정하면 내용과 이미지가 변경된다")
        fun updatesContent() {
            val owner = member(1L, "owner@test.com", "owner")
            val card = Card.builder()
                .member(owner)
                .content("before")
                .image("https://example.com/before.png")
                .build()

            val updated = card.updateContent("after", "https://example.com/after.png", owner)

            assertThat(updated.content).isEqualTo("after")
            assertThat(updated.image).isEqualTo("https://example.com/after.png")
        }
    }

    @Nested
    @DisplayName("changeVisibility")
    inner class ChangeVisibility {

        @Test
        @DisplayName("공개 요청 시 독서가 비공개면 예외가 발생한다")
        fun throwsWhenReadingPrivate() {
            val owner = member(1L, "owner@test.com", "owner")
            val reading = Reading.builder()
                .id(10L)
                .isPublic(false)
                .build()
            val card = Card.builder()
                .member(owner)
                .reading(reading)
                .isPublic(false)
                .build()

            ExceptionAssertions.assertApplicationException(
                { card.changeVisibility(owner, true) },
                CardErrorInfo.READING_MS_NOT_PUBLIC
            )
        }

        @Test
        @DisplayName("비공개 요청 시 isPublic이 false로 변경된다")
        fun setsPrivate() {
            val owner = member(1L, "owner@test.com", "owner")
            val reading = Reading.builder()
                .id(10L)
                .isPublic(true)
                .build()
            val card = Card.builder()
                .member(owner)
                .reading(reading)
                .isPublic(true)
                .build()

            card.changeVisibility(owner, false)

            assertThat(card.isPublic).isFalse()
        }
    }

    @Nested
    @DisplayName("assertReadableBy")
    inner class AssertReadableBy {

        @Test
        @DisplayName("공개 카드면 누구나 읽을 수 있다")
        fun allowsWhenPublic() {
            val card = Card.builder()
                .isPublic(true)
                .build()

            card.assertReadableBy(null)
        }

        @Test
        @DisplayName("비공개 카드는 작성자만 읽을 수 있다")
        fun throwsWhenPrivateAndNoViewer() {
            val owner = member(1L, "owner", "owner")
            val reader = member(2L, "reader", "reader")

            val card = Card.builder()
                .member(owner)
                .isPublic(false)
                .build()

            assertThatNoException().isThrownBy { card.assertReadableBy(owner) }
            ExceptionAssertions.assertApplicationException(
                { card.assertReadableBy(reader) },
                CardErrorInfo.READING_MS_NOT_PUBLIC
            )
        }
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
