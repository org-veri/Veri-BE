package org.goorm.veri.veribe.domain.card.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.entity.BaseEntity;

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
    private MemberBook memberBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
