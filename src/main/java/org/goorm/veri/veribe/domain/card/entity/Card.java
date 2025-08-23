package org.goorm.veri.veribe.domain.card.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.card.exception.CardErrorInfo;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.entity.BaseEntity;
import org.goorm.veri.veribe.global.exception.http.BadRequestException;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "card")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image", nullable = false, columnDefinition = "VARCHAR(2083)")
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_book_id",
            foreignKey = @ForeignKey(
                    name = "fk_card_member_book",
                    value = ConstraintMode.CONSTRAINT,
                    foreignKeyDefinition =
                            "FOREIGN KEY (member_book_id) " +
                                    "REFERENCES member_book(member_book_id) " +
                                    "ON DELETE SET NULL"
            )
    )
    private Reading reading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    public void setPublic() {
        if (!this.reading.isPublic()) {
            throw new BadRequestException(CardErrorInfo.READING_MS_NOT_PUBLIC);
        }
        this.isPublic = true;
    }

    public void setPrivate() {
        this.isPublic = false;
    }

    public void authorizeMember(Long memberId) {
        if (!this.member.getId().equals(memberId)) {
            throw new BadRequestException(CardErrorInfo.FORBIDDEN);
        }
    }
}
