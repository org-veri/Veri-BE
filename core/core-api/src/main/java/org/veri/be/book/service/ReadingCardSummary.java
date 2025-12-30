package org.veri.be.book.service;

public record ReadingCardSummary(
        Long cardId,
        String cardImage,
        boolean isPublic
) {
}
