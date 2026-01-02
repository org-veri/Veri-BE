package org.veri.be.unit.card

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.domain.card.entity.CardErrorInfo
import org.veri.be.support.assertion.ExceptionAssertions
import org.veri.be.support.fixture.CardFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.ReadingFixture

class CardTest {

    @Nested
    @DisplayName("authorizeMember")
    inner class AuthorizeMember {

        @Test
        @DisplayName("다른 멤버면 → false를 반환한다")
        fun throwsWhenNotOwner() {
            val owner = MemberFixture.aMember().id(1L).nickname("owner").build()
            val other = MemberFixture.aMember().id(2L).nickname("other").build()
            val card = CardFixture.aCard().member(owner).build()

            assertThat(card.authorizeMember(other.id)).isFalse()
        }
    }

    @Nested
    @DisplayName("updateContent")
    inner class UpdateContent {

        @Test
        @DisplayName("작성자가 수정하면 → 내용과 이미지가 변경된다")
        fun updatesContent() {
            val owner = MemberFixture.aMember().id(1L).nickname("owner").build()
            val card = CardFixture.aCard()
                .member(owner)
                .content("before")
                .image("https://example.com/before.png")
                .build()

            val updated = card.updateContent("after", "https://example.com/after.png", owner.id)

            assertThat(updated.content).isEqualTo("after")
            assertThat(updated.image).isEqualTo("https://example.com/after.png")
        }
    }

    @Nested
    @DisplayName("changeVisibility")
    inner class ChangeVisibility {

        @Test
        @DisplayName("공개 요청 시 독서가 비공개면 → 예외가 발생한다")
        fun throwsWhenReadingPrivate() {
            val owner = MemberFixture.aMember().id(1L).nickname("owner").build()
            val reading = ReadingFixture.aReading().id(10L).isPublic(false).build()
            val card = CardFixture.aCard().member(owner).reading(reading).isPublic(false).build()

            ExceptionAssertions.assertApplicationException(
                { card.changeVisibility(owner.id, true) },
                CardErrorInfo.READING_MS_NOT_PUBLIC
            )
        }

        @Test
        @DisplayName("비공개 요청 시 → isPublic이 false로 변경된다")
        fun setsPrivate() {
            val owner = MemberFixture.aMember().id(1L).nickname("owner").build()
            val reading = ReadingFixture.aReading().id(10L).isPublic(true).build()
            val card = CardFixture.aCard().member(owner).reading(reading).isPublic(true).build()

            card.changeVisibility(owner.id, false)

            assertThat(card.isPublic).isFalse()
        }
    }

    @Nested
    @DisplayName("assertReadableBy")
    inner class AssertReadableBy {

        @Test
        @DisplayName("공개 카드면 → 누구나 읽을 수 있다")
        fun allowsWhenPublic() {
            val card = CardFixture.aCard().isPublic(true).build()

            card.assertReadableBy(null)
        }

        @Test
        @DisplayName("비공개 카드는 → 작성자만 읽을 수 있다")
        fun throwsWhenPrivateAndNoViewer() {
            val owner = MemberFixture.aMember().id(1L).nickname("owner").build()
            val reader = MemberFixture.aMember().id(2L).nickname("reader").build()

            val card = CardFixture.aCard().member(owner).isPublic(false).build()

            assertThatNoException().isThrownBy { card.assertReadableBy(owner.id) }
            ExceptionAssertions.assertApplicationException(
                { card.assertReadableBy(reader.id) },
                CardErrorInfo.READING_MS_NOT_PUBLIC
            )
        }
    }
}
