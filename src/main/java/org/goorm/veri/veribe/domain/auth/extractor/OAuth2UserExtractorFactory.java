package org.goorm.veri.veribe.domain.auth.extractor;

import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OAuth2UserExtractorFactory {

    private final Map<ProviderType, OAuth2UserExtractor> extractors = new ConcurrentHashMap<>();

    public OAuth2UserExtractorFactory(List<OAuth2UserExtractor> oAuth2UserExtractorList) {
        oAuth2UserExtractorList.forEach(oAuth2UserExtractor -> extractors.put(oAuth2UserExtractor.getProviderType(), oAuth2UserExtractor));
    }

    public OAuth2UserExtractor getExtractor(ProviderType providerType) {
        return extractors.get(providerType);
    }
}
