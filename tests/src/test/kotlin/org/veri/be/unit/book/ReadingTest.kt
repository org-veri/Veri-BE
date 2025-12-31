package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.book.entity.Reading
import org.veri.be.book.entity.enums.ReadingStatus
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReadingTest {

    @Nested
    @DisplayName("updateProgress")
    inner class UpdateProgress {

        @Test
        @DisplayName("시작/종료 시간에 따라 상태를 결정한다")
        fun updatesStatusBasedOnTimes() {
            val reading = Reading.builder().status(ReadingStatus.NOT_START).build()
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
        @DisplayName("독서를 비공개로 변경한다")
        fun setsPrivate() {
            val reading = Reading.builder()
                .isPublic(true)
                .build()

            reading.setPrivate()

            assertThat(reading.isPublic).isFalse()
        }
    }

    @Nested
    @DisplayName("start/finish")
    inner class StartFinish {

        @Test
        @DisplayName("시작 시 상태와 시작 시간이 설정된다")
        fun startsReading() {
            val clock = Clock.fixed(Instant.parse("2024-01-01T00:00:30Z"), ZoneId.of("UTC"))
            val reading = Reading.builder().status(ReadingStatus.NOT_START).build()

            reading.start(clock)

            assertThat(reading.status).isEqualTo(ReadingStatus.READING)
            assertThat(reading.startedAt).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0))
        }

        @Test
        @DisplayName("완료 시 상태와 종료 시간이 설정된다")
        fun finishesReading() {
            val clock = Clock.fixed(Instant.parse("2024-01-01T12:34:56Z"), ZoneId.of("UTC"))
            val reading = Reading.builder().status(ReadingStatus.READING).build()

            reading.finish(clock)

            assertThat(reading.status).isEqualTo(ReadingStatus.DONE)
            assertThat(reading.endedAt).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 34))
        }
    }

    @Nested
    @DisplayName("authorizeMember")
    inner class AuthorizeMember {

        @Test
        @DisplayName("작성자와 다르면 false 를 반환한다")
        fun throwsWhenNotOwner() {
            val owner = member(1L, "owner@test.com", "owner")
            val reading = Reading.builder().member(owner).build()

            assertThat(reading.authorizeMember(2L)).isFalse()
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
