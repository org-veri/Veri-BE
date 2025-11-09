package org.veri.be.lib.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.veri.be.lib.auth.util.UrlUtil;

/**
 * 요청 Origin 또는 Referer에 따라 redirect URI를 동적으로 설정하는 커스텀 OAuth2AuthorizationRequestResolver.
 */
@ConditionalOnProperty(name = "auth.oauth2.dynamic-redirect", havingValue = "true")
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        String OAUTH2_BASE_URL = "/oauth2/authorization";
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, OAUTH2_BASE_URL);
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
        String provider = (String) req.getAttributes().get(OAuth2ParameterNames.REGISTRATION_ID);

        String newRedirectUri = baseUri + "/oauth/callback/" + provider;
        return OAuth2AuthorizationRequest.from(req)
                .redirectUri(newRedirectUri)
                .build();
    }
}
