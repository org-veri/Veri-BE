package org.goorm.veri.veribe.domain.card.service;

import org.goorm.veri.veribe.domain.card.controller.enums.CardSortType;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.repository.dto.CardListItem;
import org.springframework.data.domain.Page;

public interface CardQueryService {

    Page<CardListItem> getOwnedCards(Long memberId, int page, int size, CardSortType sortType);

    Card getCardById(Long cardId);

    int getOwnedCardCount(Long memberId);
}
