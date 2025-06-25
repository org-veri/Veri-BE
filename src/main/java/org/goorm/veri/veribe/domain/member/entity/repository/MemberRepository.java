package org.goorm.veri.veribe.domain.member.entity.repository;

import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
