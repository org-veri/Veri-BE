package org.veri.be.auth.storage;

import org.veri.be.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
} 
