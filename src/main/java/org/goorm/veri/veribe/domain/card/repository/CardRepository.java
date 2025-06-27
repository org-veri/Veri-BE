package org.goorm.veri.veribe.domain.card.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.repository.dto.CardListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT new org.goorm.veri.veribe.domain.card.repository.dto.CardListItem(c.id, c.content, c.image) " +
            "FROM Card c " +
            "WHERE c.memberBook.member.id = :memberId")
    Page<CardListItem> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.memberBook.member.id = :memberId")
    int countAllByMemberId(@Param("memberId") Long memberId);
}
