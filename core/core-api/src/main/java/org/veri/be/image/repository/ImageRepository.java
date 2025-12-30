package org.veri.be.image.repository;

import org.veri.be.image.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i.imageUrl FROM Image i WHERE i.member.id = :memberId")
    Page<String> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
