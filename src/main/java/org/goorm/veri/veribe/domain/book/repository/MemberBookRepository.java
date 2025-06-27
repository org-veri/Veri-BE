package org.goorm.veri.veribe.domain.book.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.goorm.veri.veribe.domain.book.entity.Book;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberBookRepository extends JpaRepository<MemberBook, Long> {
    List<MemberBook> findAllByMember_Id(Long memberId);

    @Query("SELECT mb FROM MemberBook mb " +
            "LEFT JOIN FETCH mb.cards " +
            "JOIN FETCH mb.book " +
            "WHERE mb.id = :memberBookId")
    Optional<MemberBook> findByIdWithCardsAndBook(@Param("memberBookId") Long memberBookId);


    @Query("""
            SELECT mb.book
            FROM MemberBook mb
            WHERE mb.startedAt >= :oneWeekAgo
            GROUP BY mb.book
            ORDER BY COUNT(mb) DESC
            """)
    List<Book> findMostPopularBook(LocalDateTime oneWeekAgo);
}
