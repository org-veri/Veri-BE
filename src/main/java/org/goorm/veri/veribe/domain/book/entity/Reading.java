package org.goorm.veri.veribe.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;
import org.goorm.veri.veribe.domain.book.exception.ReadingErrorInfo;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.entity.BaseEntity;
import org.goorm.veri.veribe.global.exception.http.ForbiddenException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "member_book")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reading extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_book_id")
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
    private BookStatus status;

    @Builder.Default
    @Column(name = "is_public")
    private boolean isPublic = false;

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
            throw new ForbiddenException(ReadingErrorInfo.FORBIDDEN);
        }
    }
}
