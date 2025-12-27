package org.veri.be.unit.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingTest {

    @Nested
    @DisplayName("updateProgress")
    class UpdateProgress {

        @Test
        @DisplayName("시작/종료 시간에 따라 상태를 결정한다")
        void updatesStatusBasedOnTimes() {
            Reading reading = Reading.builder().status(ReadingStatus.NOT_START).build();
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0);

            reading.updateProgress(null, null, null);
            assertThat(reading.getStatus()).isEqualTo(ReadingStatus.NOT_START);

            reading.updateProgress(3.0, start, null);
            assertThat(reading.getStatus()).isEqualTo(ReadingStatus.READING);

            reading.updateProgress(4.0, start, end);
            assertThat(reading.getStatus()).isEqualTo(ReadingStatus.DONE);
        }
    }

    @Nested
    @DisplayName("setPrivate")
    class SetPrivate {

        @Test
        @DisplayName("독서를 비공개로 바꾸면 카드도 비공개 처리된다")
        void setsPrivateOnCards() {
            Card card1 = Card.builder().id(1L).image("https://example.com/1.png").isPublic(true).build();
            Card card2 = Card.builder().id(2L).image("https://example.com/2.png").isPublic(true).build();
            Reading reading = Reading.builder()
                    .isPublic(true)
                    .cards(List.of(card1, card2))
                    .build();

            reading.setPrivate();

            assertThat(reading.isPublic()).isFalse();
            assertThat(reading.getCards()).allMatch(card -> !card.isPublic());
        }
    }

    @Nested
    @DisplayName("start/finish")
    class StartFinish {

        @Test
        @DisplayName("시작 시 상태와 시작 시간이 설정된다")
        void startsReading() {
            Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:30Z"), ZoneId.of("UTC"));
            Reading reading = Reading.builder().status(ReadingStatus.NOT_START).build();

            reading.start(clock);

            assertThat(reading.getStatus()).isEqualTo(ReadingStatus.READING);
            assertThat(reading.getStartedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
        }

        @Test
        @DisplayName("완료 시 상태와 종료 시간이 설정된다")
        void finishesReading() {
            Clock clock = Clock.fixed(Instant.parse("2024-01-01T12:34:56Z"), ZoneId.of("UTC"));
            Reading reading = Reading.builder().status(ReadingStatus.READING).build();

            reading.finish(clock);

            assertThat(reading.getStatus()).isEqualTo(ReadingStatus.DONE);
            assertThat(reading.getEndedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 34));
        }
    }

    @Nested
    @DisplayName("authorizeMember")
    class AuthorizeMember {

        @Test
        @DisplayName("작성자와 다르면 false 를 반환한다")
        void throwsWhenNotOwner() {
            Member owner = member(1L, "owner@test.com", "owner");
            Reading reading = Reading.builder().member(owner).build();

            assertThat(reading.authorizeMember(2L)).isFalse();
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
