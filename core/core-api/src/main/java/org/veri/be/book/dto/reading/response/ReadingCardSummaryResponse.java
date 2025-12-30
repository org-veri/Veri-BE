package org.veri.be.book.dto.reading.response;

public record ReadingCardSummaryResponse(
        Long cardId,
        String cardImage,
        boolean isPublic
) {
}
