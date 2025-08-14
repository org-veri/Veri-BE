package org.goorm.veri.veribe.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.goorm.veri.veribe.global.util.UrlUtil;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * 요청 Origin 또는 Referer에 따라 redirect URI를 동적으로 설정하는 커스텀 OAuth2AuthorizationRequestResolver.
 */
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo, String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(request, req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(request, req);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            HttpServletRequest request,
            OAuth2AuthorizationRequest req
    ) {
        if (req == null) {
            return null;
        }

        String url = UrlUtil.getRequestingUrl(request);
        if (url == null || url.isBlank()) {
            return req; // 요청 URL이 없으면 기본 요청 반환
        }

        return buildNewRequest(req, url);
    }

    /**
     * 새로운 OAuth2AuthorizationRequest를 생성
     */
    private OAuth2AuthorizationRequest buildNewRequest(OAuth2AuthorizationRequest req, String baseUri) {
        String newRedirectUri = baseUri + "/oauth/callback/kakao";
        return OAuth2AuthorizationRequest.from(req)
                .redirectUri(newRedirectUri)
                .build();
    }
}
