package org.veri.be.card.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.book.service.ReadingCardSummaryProvider;
import org.veri.be.card.entity.Card;
import org.veri.be.card.repository.CardRepository;

@Service
@RequiredArgsConstructor
public class ReadingCardSummaryProviderService implements ReadingCardSummaryProvider {

    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReadingDetailResponse.CardSummaryResponse> getCardSummaries(Long readingId) {
        return cardRepository.findAllByReadingId(readingId).stream()
                .map(card -> new ReadingDetailResponse.CardSummaryResponse(
                        card.getId(),
                        card.getImage(),
                        card.isPublic()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void setCardsPrivate(Long readingId) {
        List<Card> cards = cardRepository.findAllByReadingId(readingId);
        cards.forEach(Card::setPrivate);
    }
}
