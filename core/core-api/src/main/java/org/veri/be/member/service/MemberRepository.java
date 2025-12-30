package org.veri.be.member.service;

import org.veri.be.member.entity.Member;
import org.veri.be.member.entity.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByProviderIdAndProviderType(String providerId, ProviderType providerType);

    boolean existsByNickname(String nickname);
}
