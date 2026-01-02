package org.veri.be.domain.card.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.entity.Authorizable;
import org.veri.be.global.entity.BaseEntity;
import org.veri.be.lib.exception.ApplicationException;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "card")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Card extends BaseEntity implements Authorizable {

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
            name = "reading_id",
            foreignKey = @ForeignKey(
                    name = "fk_card_reading",
                    value = ConstraintMode.CONSTRAINT,
                    foreignKeyDefinition =
                            "FOREIGN KEY (reading_id) " +
                                    "REFERENCES reading(id) " +
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


    public Card updateContent(String content, String imageUrl, Long memberId) {
        authorizeOrThrow(memberId);
        return this.toBuilder()
                .content(content)
                .image(imageUrl)
                .build();
    }

    public void changeVisibility(Long memberId, boolean makePublic) {
        authorizeOrThrow(memberId);
        if (makePublic) {
            setPublic();
        } else {
            setPrivate();
        }
    }

    public void setPublic() {
        if (!this.reading.isPublic()) {
            throw ApplicationException.of(CardErrorInfo.READING_MS_NOT_PUBLIC);
        }
        this.isPublic = true;
    }

    public void setPrivate() {
        this.isPublic = false;
    }

    @Override
    public boolean authorizeMember(Long memberId) {
        return memberId.equals(this.member.getId());
    }

    public void assertReadableBy(Long memberId) {
        if (this.isPublic) {
            return;
        }
        if (memberId != null && this.authorizeMember(memberId)) {
            return;
        }
        throw ApplicationException.of(CardErrorInfo.READING_MS_NOT_PUBLIC);
    }
}
