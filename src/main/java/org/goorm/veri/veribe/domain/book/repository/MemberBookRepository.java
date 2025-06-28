package org.goorm.veri.veribe.domain.book.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookResponse;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberBookRepository extends JpaRepository<MemberBook, Long> {

    @Query("SELECT mb FROM MemberBook mb " +
            "LEFT JOIN FETCH mb.cards " +
            "JOIN FETCH mb.book " +
            "WHERE mb.id = :memberBookId")
    Optional<MemberBook> findByIdWithCardsAndBook(@Param("memberBookId") Long memberBookId);

    @Query("""
            SELECT new org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse(
            mb.book.image,
            mb.book.title,
            mb.book.author,
            mb.book.publisher,
            mb.book.isbn)
            FROM MemberBook mb
            WHERE mb.startedAt >= :startOfWeek
            GROUP BY mb.book
            ORDER BY COUNT(mb) DESC
            """)
    Page<BookPopularResponse> findMostPopularBook(@Param("startOfWeek") LocalDateTime startOfWeek, Pageable pageable);

    @Query("""
            SELECT new org.goorm.veri.veribe.domain.book.dto.memberBook.MemberBookResponse(
            mb.book.id,
            mb.book.title,
            mb.book.author,
            mb.book.image,
            mb.score,
            mb.status)
            FROM MemberBook mb
            WHERE mb.member.id = :memberId
            """)
    Page<MemberBookResponse> findMemberBookPage(@Param("memberId") Long memberId, Pageable pageable);
}
