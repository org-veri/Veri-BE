package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.domain.book.entity.enums.ReadingStatus
import org.veri.be.support.fixture.CardFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.ReadingFixture
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReadingTest {

    @Nested
    @DisplayName("updateProgress")
    inner class UpdateProgress {

        @Test
        @DisplayName("시작/종료 시간에 따라 → 상태를 결정한다")
        fun updatesStatusBasedOnTimes() {
            val reading = ReadingFixture.aReading().status(ReadingStatus.NOT_START).build()
            val start = LocalDateTime.of(2024, 1, 1, 10, 0)
            val end = LocalDateTime.of(2024, 1, 2, 10, 0)

            reading.updateProgress(null, null, null)
            assertThat(reading.status).isEqualTo(ReadingStatus.NOT_START)

            reading.updateProgress(3.0, start, null)
            assertThat(reading.status).isEqualTo(ReadingStatus.READING)

            reading.updateProgress(4.0, start, end)
            assertThat(reading.status).isEqualTo(ReadingStatus.DONE)
        }
    }

    @Nested
    @DisplayName("setPrivate")
    inner class SetPrivate {

        @Test
        @DisplayName("독서를 비공개로 바꾸면 → 카드도 비공개 처리된다")
        fun setsPrivateOnCards() {
            val card1 = CardFixture.aCard().id(1L).image("https://example.com/1.png").isPublic(true).build()
            val card2 = CardFixture.aCard().id(2L).image("https://example.com/2.png").isPublic(true).build()
            val reading = ReadingFixture.aReading()
                .isPublic(true)
                .cards(listOf(card1, card2))
                .build()

            reading.setPrivate()

            assertThat(reading.isPublic).isFalse()
            assertThat(reading.cards).allMatch { card -> !card.isPublic }
        }
    }

    @Nested
    @DisplayName("start/finish")
    inner class StartFinish {

        @Test
        @DisplayName("시작 시 → 상태와 시작 시간이 설정된다")
        fun startsReading() {
            val clock = Clock.fixed(Instant.parse("2024-01-01T00:00:30Z"), ZoneId.of("UTC"))
            val reading = ReadingFixture.aReading().status(ReadingStatus.NOT_START).build()

            reading.start(clock)

            assertThat(reading.status).isEqualTo(ReadingStatus.READING)
            assertThat(reading.startedAt).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0))
        }

        @Test
        @DisplayName("완료 시 → 상태와 종료 시간이 설정된다")
        fun finishesReading() {
            val clock = Clock.fixed(Instant.parse("2024-01-01T12:34:56Z"), ZoneId.of("UTC"))
            val reading = ReadingFixture.aReading().status(ReadingStatus.READING).build()

            reading.finish(clock)

            assertThat(reading.status).isEqualTo(ReadingStatus.DONE)
            assertThat(reading.endedAt).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 34))
        }
    }

    @Nested
    @DisplayName("authorizeMember")
    inner class AuthorizeMember {

        @Test
        @DisplayName("작성자와 다르면 → false 를 반환한다")
        fun throwsWhenNotOwner() {
            val owner = member(1L, "owner")
            val reading = ReadingFixture.aReading().member(owner).build()

            assertThat(reading.authorizeMember(2L)).isFalse()
        }
    }

    private fun member(id: Long, nickname: String): org.veri.be.domain.member.entity.Member {
        return MemberFixture.aMember().id(id).nickname(nickname).build()
    }
}
