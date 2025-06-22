package org.goorm.veri.veribe.domain.member.service;

import org.goorm.veri.veribe.domain.member.entity.Member;

public interface MemberQueryService {
    Member findById(Long id);
}
