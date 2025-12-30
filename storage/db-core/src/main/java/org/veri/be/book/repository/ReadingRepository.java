package org.veri.be.book.repository;

import org.veri.be.book.entity.Reading;
import org.veri.be.book.entity.enums.ReadingStatus;
import org.veri.be.book.repository.dto.BookPopularQueryResult;
import org.veri.be.book.repository.dto.ReadingQueryResult;
import org.veri.be.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    @Query("SELECT mb FROM Reading mb " +
            "JOIN FETCH mb.book " +
            "WHERE mb.id = :memberBookId")
    Optional<Reading> findByIdWithBook(@Param("memberBookId") Long memberBookId);

    int countAllByMember(Member member);

    long countAllByMemberId(Long memberId);

    @Query("""
            SELECT new org.veri.be.book.repository.dto.BookPopularQueryResult(
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
    Page<BookPopularQueryResult> findMostPopularBook(@Param("startOfWeek") LocalDateTime startOfWeek,
                                                     @Param("startOfNextWeek") LocalDateTime startOfNextWeek,
                                                     Pageable pageable
    );

    @Query("""
            SELECT new org.veri.be.book.repository.dto.ReadingQueryResult(
            mb.book.id,
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
            AND mb.status IN (:statuses)
            """)
    Page<ReadingQueryResult> findReadingPage(
            @Param("memberId") Long memberId,
            @Param("statuses") List<ReadingStatus> statuses,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(mb)
            FROM Reading mb
            WHERE mb.status = :status
            AND mb.member.id = :memberId
            """)
    int countByStatusAndMember(@Param("status") ReadingStatus status, @Param("memberId") Long memberId);

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
