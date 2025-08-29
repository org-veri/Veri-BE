package org.goorm.veri.veribe.domain.book.repository;

import org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse;
import org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingResponse;
import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    @Query("SELECT mb FROM Reading mb " +
            "LEFT JOIN FETCH mb.cards " +
            "JOIN FETCH mb.book " +
            "WHERE mb.id = :memberBookId")
    Optional<Reading> findByIdWithCardsAndBook(@Param("memberBookId") Long memberBookId);

    int countAllByMember(Member member);

    @Query("""
            SELECT new org.goorm.veri.veribe.domain.book.dto.book.BookPopularResponse(
            mb.book.image,
            mb.book.title,
            mb.book.author,
            mb.book.publisher,
            mb.book.isbn)
            FROM Reading mb
            WHERE mb.createdAt >= :startOfWeek
            AND mb.createdAt < :startOfNextWeek
            GROUP BY mb.book
            ORDER BY COUNT(mb) DESC
            """)
    Page<BookPopularResponse> findMostPopularBook(@Param("startOfWeek") LocalDateTime startOfWeek,
                                                  @Param("startOfNextWeek") LocalDateTime startOfNextWeek,
                                                  Pageable pageable
    );

    @Query("""
            SELECT new org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingResponse(
            mb.id,
            mb.book.title,
            mb.book.author,
            mb.book.image,
            mb.score,
            mb.startedAt,
            mb.status,
            mb.isPublic)
            FROM Reading mb
            WHERE mb.member.id = :memberId
            """)
    Page<ReadingResponse> findReadingPage(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
            SELECT COUNT(mb)
            FROM Reading mb
            WHERE mb.status = :status
            AND mb.member.id = :memberId
            """)
    int countByStatusAndMember(@Param("status") BookStatus status, @Param("memberId") Long memberId);

    @Query("""
                SELECT mb FROM Reading mb
                WHERE mb.book.id = :bookId AND mb.member.id = :memberId
            """)
    Optional<Reading> findByMemberAndBook(@Param("memberId") Long memberId,
                                          @Param("bookId") Long bookId);

    @Query("""
                    SELECT mb FROM Reading mb
                    WHERE mb.member.id = :memberId
                        AND mb.book.title = :title
                        AND mb.book.author = :author
            """)
    Optional<Reading> findByAuthorAndTitle(@Param("memberId") Long memberId,
                                           @Param("title") String title, @Param("author") String author);
}
