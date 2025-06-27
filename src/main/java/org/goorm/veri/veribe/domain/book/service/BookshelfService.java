package org.goorm.veri.veribe.domain.book.service;

import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dtos.memberBook.MemberBookResponse;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;

import java.util.List;

public interface BookshelfService {

    MemberBook addToBookshelf(Long memberId, Long bookId);

    List<MemberBookResponse> searchAll(Long memberId);

    MemberBookDetailResponse searchDetail(Long memberBookId);

    void rateScore(Double score, Long memberBookId);

    void readStart(Long memberBookId);

    void readOver(Long memberBookId);

    void deleteBook(Long memberBookId);
}
