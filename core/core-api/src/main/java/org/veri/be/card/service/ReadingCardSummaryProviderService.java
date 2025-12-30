package org.veri.be.card.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.service.ReadingCardSummary;
import org.veri.be.book.service.ReadingCardSummaryProvider;
import org.veri.be.card.entity.Card;
import org.veri.be.card.service.CardRepository;

@Service
@RequiredArgsConstructor
public class ReadingCardSummaryProviderService implements ReadingCardSummaryProvider {

    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReadingCardSummary> getCardSummaries(Long readingId) {
        return cardRepository.findAllByReadingId(readingId).stream()
                .map(card -> new ReadingCardSummary(
                        card.getId(),
                        card.getImage(),
                        card.isPublic()
                ))
                .toList();
    }

}
