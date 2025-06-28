package org.goorm.veri.veribe.domain.book.service;

import org.goorm.veri.veribe.domain.book.controller.enums.MemberBookSortType;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookDetailResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookResponse;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookshelfService {

    MemberBook addToBookshelf(Member member, Long bookId);

    Page<MemberBookResponse> searchAll(Long memberId, int page, int size, MemberBookSortType sortType);

    MemberBookDetailResponse searchDetail(Long memberBookId);

    List<BookPopularResponse> searchPopular();

    void rateScore(Double score, Long memberBookId);

    void readStart(Long memberBookId);

    void readOver(Long memberBookId);

    void deleteBook(Long memberBookId);
}
