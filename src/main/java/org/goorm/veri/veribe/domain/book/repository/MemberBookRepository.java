package org.goorm.veri.veribe.domain.book.repository;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberBookRepository extends JpaRepository<MemberBook, Long> {
    List<MemberBook> findAllByMember_Id(Long memberId);
}
