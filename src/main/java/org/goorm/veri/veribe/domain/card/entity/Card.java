package org.goorm.veri.veribe.domain.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.global.entity.BaseEntity;

@Getter
@Builder
@Entity
@Table(name = "card")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "image")
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
}
