package org.goorm.veri.veribe.domain.book.service;

import org.goorm.veri.veribe.domain.book.controller.enums.MemberBookSortType;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookResponse;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface BookshelfService {

    MemberBook addToBookshelf(Member member, Long bookId);

    Page<MemberBookResponse> searchAll(Long memberId, int page, int size, MemberBookSortType sortType);

    MemberBookDetailResponse searchDetail(Long memberBookId);

    Page<BookPopularResponse> searchPopular(int page, int size);

    int searchMyReadingDoneCount(Long memberId);

    Long searchByTitleAndAuthor(Long memberId, String title, String author);

    void modifyBook(Double score, LocalDateTime startedAt, LocalDateTime endedAt, Long memberBookId);

    void rateScore(Double score, Long memberBookId);

    void readStart(Long memberBookId);

    void readOver(Long memberBookId);

    void deleteBook(Long memberBookId);
}
