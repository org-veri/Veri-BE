package org.goorm.veri.veribe.domain.book.service;

import org.goorm.veri.veribe.domain.book.controller.enums.ReadingSortType;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.reading.ReadingDetailResponse;
import org.goorm.veri.veribe.domain.book.dto.reading.ReadingResponse;
import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface BookshelfService {

    Reading addToBookshelf(Member member, Long bookId);

    Page<ReadingResponse> searchAll(Long memberId, int page, int size, ReadingSortType sortType);

    ReadingDetailResponse searchDetail(Long memberBookId);

    Page<BookPopularResponse> searchPopular(int page, int size);

    int searchMyReadingDoneCount(Long memberId);

    Long searchByTitleAndAuthor(Long memberId, String title, String author);

    void modifyBook(Double score, LocalDateTime startedAt, LocalDateTime endedAt, Long memberBookId);

    void rateScore(Double score, Long memberBookId);

    void readStart(Long memberBookId);

    void readOver(Long memberBookId);

    void deleteBook(Long memberBookId);
}
