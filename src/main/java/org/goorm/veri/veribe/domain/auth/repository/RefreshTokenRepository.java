package org.goorm.veri.veribe.domain.auth.repository;

import org.goorm.veri.veribe.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
} 