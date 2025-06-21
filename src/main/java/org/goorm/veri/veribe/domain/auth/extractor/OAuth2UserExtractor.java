package org.goorm.veri.veribe.domain.auth.extractor;

import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserExtractor {

    OAuth2Request.OAuth2LoginRequest extractUser(OAuth2User oAuth2User);
    ProviderType getProviderType();
}
