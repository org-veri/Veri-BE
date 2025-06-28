package org.goorm.veri.veribe.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.card.controller.enums.CardSortType;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
import org.goorm.veri.veribe.domain.card.exception.CardException;
import org.goorm.veri.veribe.domain.card.repository.CardRepository;
import org.goorm.veri.veribe.domain.card.repository.dto.CardListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CardQueryServiceImpl implements CardQueryService {

    private final CardRepository cardRepository;

    @Override
    public Page<CardListItem> getOwnedCards(Long userId, int page, int size, CardSortType sortType) {
        Pageable pageRequest = PageRequest.of(page, size, sortType.getSort());

        Page<CardListItem> cards = cardRepository.findAllByMemberId(userId, pageRequest);
        return cards;
    }

    @Override
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException(CardErrorCode.NOT_FOUND));
    }

    @Override
    public int getOwnedCardCount(Long memberId) {
        return cardRepository.countAllByMemberId(memberId);
    }
}
