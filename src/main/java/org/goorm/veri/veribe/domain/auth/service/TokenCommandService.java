package org.goorm.veri.veribe.domain.auth.service;

import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;

public interface TokenCommandService {
    AuthResponse.LoginResponse createLoginToken(Member member);
}
