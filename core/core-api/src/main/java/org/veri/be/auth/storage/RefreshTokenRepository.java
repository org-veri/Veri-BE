package org.veri.be.auth.storage;

import org.veri.be.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
} 
