package org.veri.be.book.service;

import java.util.List;

public interface ReadingCardSummaryProvider {

    List<ReadingCardSummary> getCardSummaries(Long readingId);
}
