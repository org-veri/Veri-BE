package org.veri.be.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.card.controller.dto.CardConverter;
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.enums.CardSortType;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.card.repository.dto.CardFeedItem;
import org.veri.be.domain.card.repository.dto.CardListItem;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.lib.exception.http.NotFoundException;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CardQueryService {

    private final CardRepository cardRepository;
    private final CardConverter cardConverter;

    public Page<CardListItem> getOwnedCards(Long memberId, int page, int size, CardSortType sortType) {
        Pageable pageRequest = PageRequest.of(page, size, sortType.getSort());

        Page<CardListItem> cards = cardRepository.findAllByMemberId(memberId, pageRequest);
        return cards;
    }

    public CardDetailResponse getCardDetail(Long cardId, Member viewer) {
        Card card = cardRepository.findByIdWithAllAssociations(cardId)
                .orElseThrow(() -> new NotFoundException(CardErrorInfo.NOT_FOUND));

        card.assertReadableBy(viewer);

        return cardConverter.toCardDetailResponse(card, viewer);
    }

    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(CardErrorInfo.NOT_FOUND));
    }

    public int getOwnedCardCount(Long memberId) {
        return cardRepository.countAllByMemberId(memberId);
    }

    public Page<CardFeedItem> getAllCards(int page, int size, CardSortType sortType) {
        Pageable pageRequest = PageRequest.of(page, size, sortType.getSort());
        return cardRepository.findAllPublicItems(pageRequest);
    }
}
