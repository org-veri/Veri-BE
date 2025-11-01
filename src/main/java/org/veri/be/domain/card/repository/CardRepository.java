package org.veri.be.domain.card.repository;

import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.repository.dto.CardListItem;
import org.veri.be.domain.card.repository.dto.CardFeedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query(
            "SELECT new org.veri.be.domain.card.repository.dto.CardListItem(" +
                    "c.id, " +
                    "c.reading.book.title, " +
                    "c.content, " +
                    "c.image, " +
                    "c.createdAt, " +
                    "c.isPublic" +
                    ") " +
                    "FROM Card c " +
                    "WHERE c.member.id = :memberId")
    Page<CardListItem> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.member.id = :memberId")
    int countAllByMemberId(@Param("memberId") Long memberId);

    @Query(
            "SELECT new org.veri.be.domain.card.repository.dto.CardFeedItem(" +
                    "c.id, " +
                    "c.member, " + // Note. 이후 연관 관계 조회시 유의
                    "c.reading.book.title, " +
                    "c.content, " +
                    "c.image, " +
                    "c.createdAt," +
                    "c.isPublic" +
                    ") " +
                    "FROM Card c " +
                    "WHERE c.isPublic = true")
    Page<CardFeedItem> findAllPublicItems(Pageable pageable);
}
