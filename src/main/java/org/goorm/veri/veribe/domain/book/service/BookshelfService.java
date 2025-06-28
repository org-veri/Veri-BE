package org.goorm.veri.veribe.domain.book.service;

import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookSortResponse;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.member.entity.Member;

import java.util.List;

public interface BookshelfService {

    MemberBook addToBookshelf(Member member, Long bookId);

    MemberBookSortResponse searchAllNewest(int page, int size, Member member);

    MemberBookSortResponse searchAllOldest(int page, int size, Member member);

    MemberBookSortResponse searchAllHighScore(int page, int size, Member member);

    MemberBookDetailResponse searchDetail(Long memberBookId);

    List<BookPopularResponse> searchPopular();

    void rateScore(Double score, Long memberBookId);

    void readStart(Long memberBookId);

    void readOver(Long memberBookId);

    void deleteBook(Long memberBookId);
}
