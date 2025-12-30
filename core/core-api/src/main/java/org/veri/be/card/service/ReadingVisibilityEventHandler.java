package org.veri.be.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.event.ReadingVisibilityChangedEvent;
import org.veri.be.card.entity.Card;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReadingVisibilityEventHandler {

    private final CardRepository cardRepository;

    @EventListener
    @Transactional
    public void onReadingVisibilityChanged(ReadingVisibilityChangedEvent event) {
        if (event.isPublic()) {
            return;
        }

        List<Card> cards = cardRepository.findAllByReadingId(event.readingId());
        cards.forEach(Card::setPrivate);
    }
}
