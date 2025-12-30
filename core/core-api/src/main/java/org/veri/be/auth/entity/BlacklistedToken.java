package org.veri.be.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BlacklistedToken {
    @Id
    @Column(length = 512)
    private String token;

    @Column(nullable = false)
    private Instant expiredAt;
} 
