package org.goorm.veri.veribe.domain.auth.repository;

import org.goorm.veri.veribe.domain.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
} 