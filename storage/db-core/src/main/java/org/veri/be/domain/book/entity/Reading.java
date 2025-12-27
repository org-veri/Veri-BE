package org.veri.be.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;
import org.veri.be.lib.exception.ApplicationException;
import lombok.experimental.SuperBuilder;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.domain.book.exception.ReadingErrorCode;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.entity.BaseEntity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "reading")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reading extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "score")
    private Double score;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "status")
    private ReadingStatus status;

    @Builder.Default
    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Builder.Default
    @OneToMany(mappedBy = "reading")
    private List<Card> cards = new ArrayList<>();

    public void setPublic() {
        this.isPublic = true;
    }

    /**
     * 독서를 비공개 설정 하면 모든 카드도 비공개
     */
    public void setPrivate() {
        this.isPublic = false;
        this.cards.forEach(Card::setPrivate);
    }

    public void authorizeMember(Long memberId) {
        if (!this.member.getId().equals(memberId)) {
            throw ApplicationException.of(ReadingErrorCode.FORBIDDEN);
        }
    }

    public void updateProgress(Double score, LocalDateTime startedAt, LocalDateTime endedAt) {
        this.score = score;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = decideStatus(startedAt, endedAt);
    }

    public void updateScore(Double score) {
        this.score = score;
    }

    public void start(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock).withSecond(0).withNano(0);
        this.startedAt = now;
        this.status = ReadingStatus.READING;
    }

    public void finish(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock).withSecond(0).withNano(0);
        this.endedAt = now;
        this.status = ReadingStatus.DONE;
    }

    private ReadingStatus decideStatus(LocalDateTime start, LocalDateTime end) {
        if (end != null) {
            return ReadingStatus.DONE;
        }

        if (start != null) {
            return ReadingStatus.READING;
        }

        return ReadingStatus.NOT_START;
    }
}
