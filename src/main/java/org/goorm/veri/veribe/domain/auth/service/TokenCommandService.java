package org.goorm.veri.veribe.domain.auth.service;

import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.member.entity.Member;

public interface TokenCommandService {
    OAuth2Response.OAuth2LoginResponse loginToken(Member member);
}
