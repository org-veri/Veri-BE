package org.goorm.veri.veribe.domain.image.repository;

import org.goorm.veri.veribe.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i.imageUrl FROM Image i WHERE i.member.id = :memberId")
    List<String> findByMemberId(Long memberId);
}
