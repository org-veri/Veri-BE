package org.veri.be.book.service;

import java.util.List;
import org.veri.be.book.dto.reading.response.ReadingDetailResponse;

public interface ReadingCardSummaryProvider {

    List<ReadingDetailResponse.CardSummaryResponse> getCardSummaries(Long readingId);

    void setCardsPrivate(Long readingId);
}
