package org.goorm.veri.veribe.domain.book.service;

import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface BookshelfService {

    Long addToBookshelf(Long memberId, Long bookId, Double score, LocalDateTime startedAt, LocalDateTime endedAt);

    List<MemberBookResponse> searchAll(Long memberId);

    MemberBookDetailResponse searchDetail(Long memberBookId);

    void readStart(Long memberBookId);

    void readOver(Long memberBookId);

    void deleteBook(Long memberBookId);
}
