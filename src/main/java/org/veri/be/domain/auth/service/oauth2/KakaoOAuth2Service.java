package org.veri.be.domain.auth.service.oauth2;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.veri.be.domain.auth.dto.AuthRequest;
import org.veri.be.domain.auth.dto.KakaoOAuth2DTO;
import org.veri.be.domain.auth.exception.AuthErrorInfo;
import org.veri.be.domain.auth.service.TokenCommandService;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.oauth2.KakaoOAuth2ConfigData;
import org.veri.be.lib.exception.http.ExternalApiException;

@Slf4j
@Service
public class KakaoOAuth2Service extends AbstractOAuth2Service {

    private final KakaoOAuth2ConfigData kakaoOAuth2ConfigData;
    private final KakaoApi kakaoApi;

    public KakaoOAuth2Service(
            MemberRepository memberRepository,
            MemberQueryService memberQueryService,
            TokenCommandService tokenCommandService,
            KakaoOAuth2ConfigData kakaoOAuth2ConfigData,
            WebClient webClient
    ) {
        super(ProviderType.KAKAO, memberRepository, memberQueryService, tokenCommandService);
        this.kakaoOAuth2ConfigData = kakaoOAuth2ConfigData;

        val adapter = WebClientAdapter.create(webClient.mutate().build());
        val httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        this.kakaoApi = httpServiceProxyFactory.createClient(KakaoApi.class);
    }

    @Override
    protected String getAccessToken(String code, String redirectUri) {
        try {
            KakaoOAuth2DTO.OAuth2TokenDTO tokenDTO = kakaoApi.getToken("authorization_code",
                    kakaoOAuth2ConfigData.getClientId(),
                    redirectUri,
                    code);
            return tokenDTO.getAccess_token();
        } catch (RestClientResponseException e) {
            log.error("Failed to get access token from Kakao: {}\n{}", e.getMessage(), e.getResponseBodyAsString());
            throw new ExternalApiException(AuthErrorInfo.FAIL_GET_ACCESS_TOKEN);
        } catch (RestClientException e) {
            throw new ExternalApiException(AuthErrorInfo.FAIL_PROCESS_RESPONSE);
        }
    }

    @Override
    protected AuthRequest.OAuth2LoginUserInfo getUserInfo(String token) {
        try {
            KakaoOAuth2DTO.KakaoProfile kakaoProfile = kakaoApi.getUserInfo("Bearer " + token);
            return AuthRequest.OAuth2LoginUserInfo.builder()
                    .email(kakaoProfile.getKakao_account().getEmail())
                    .providerId(String.valueOf(kakaoProfile.getId()))
                    .nickname(kakaoProfile.getProperties().getNickname())
                    .image(kakaoProfile.getProperties().getProfile_image())
                    .providerType(ProviderType.KAKAO)
                    .build();
        } catch (RestClientResponseException e) {
            log.error("Failed to get user info from Kakao: {}\n{}", e.getMessage(), e.getResponseBodyAsString());
            throw new ExternalApiException(AuthErrorInfo.FAIL_GET_USER_INFO);
        } catch (RestClientException e) {
            throw new ExternalApiException(AuthErrorInfo.FAIL_PROCESS_RESPONSE);
        }
    }

    @Override
    protected String getDefaultRedirectUri() {
        return kakaoOAuth2ConfigData.getRedirectUri();
    }

    @HttpExchange
    interface KakaoApi {
        @PostExchange(value = "https://kauth.kakao.com/oauth/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        KakaoOAuth2DTO.OAuth2TokenDTO getToken(String grant_type, String client_id, String redirect_uri, String code);

        @GetExchange(value = "https://kapi.kakao.com/v2/user/me")
        KakaoOAuth2DTO.KakaoProfile getUserInfo(@RequestHeader("Authorization") String authHeader);
    }
}
