package org.goorm.veri.veribe.domain.image.repository;

import org.goorm.veri.veribe.domain.image.entity.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, Long> {
}
