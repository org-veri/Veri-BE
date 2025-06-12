package org.goorm.veri.veribe.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
import org.goorm.veri.veribe.domain.card.exception.CardException;
import org.goorm.veri.veribe.domain.card.repository.CardRepository;
import org.goorm.veri.veribe.domain.card.repository.dto.CardListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CardQueryServiceImpl implements CardQueryService {

    private final CardRepository cardRepository;

    @Override
    public Page<CardListItem> getOwnedCards(Long userId) {
        Pageable pageable = Pageable.unpaged();

        List<CardListItem> cards = cardRepository.findAllByMemberId(userId, null);
        int totalCards = cardRepository.countAllByMemberId(userId);

        return new PageImpl<>(cards, pageable, totalCards);
    }

    @Override
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException(CardErrorCode.NOT_FOUND));
    }
}
