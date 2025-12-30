package org.veri.be.image.service;

import org.veri.be.image.entity.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface OcrResultRepository extends JpaRepository<OcrResult, Long> {
}
