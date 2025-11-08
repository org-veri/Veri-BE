package org.veri.be.domain.auth.service.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.domain.auth.dto.AuthRequest;
import org.veri.be.domain.auth.dto.KakaoOAuth2DTO;
import org.veri.be.domain.auth.exception.AuthErrorInfo;
import org.veri.be.domain.auth.service.TokenCommandService;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.lib.auth.oauth2.KakaoOAuth2ConfigData;
import org.veri.be.lib.exception.http.ExternalApiException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class KakaoOAuth2Service extends AbstractOAuth2Service {

    private final KakaoOAuth2ConfigData kakaoOAuth2ConfigData;

    public KakaoOAuth2Service(MemberRepository memberRepository,
                              MemberQueryService memberQueryService,
                              TokenCommandService tokenCommandService,
                              KakaoOAuth2ConfigData kakaoOAuth2ConfigData
    ) {
        super(ProviderType.KAKAO, memberRepository, memberQueryService, tokenCommandService);
        this.kakaoOAuth2ConfigData = kakaoOAuth2ConfigData;
    }

    @Override
    protected String getAccessToken(String code, String redirectUri) {
        // 인가코드 토큰 가져오기
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", kakaoOAuth2ConfigData.getClientId());
        map.add("redirect_uri", redirectUri);
        map.add("code", code);
        HttpEntity<MultiValueMap> request = new HttpEntity<>(map, httpHeaders);

        try {
            ResponseEntity<String> response1 = restTemplate.exchange(
                    kakaoOAuth2ConfigData.getTokenUri(),
                    HttpMethod.POST,
                    request,
                    String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            KakaoOAuth2DTO.OAuth2TokenDTO oAuth2TokenDTO = null;

            oAuth2TokenDTO = objectMapper.readValue(response1.getBody(), KakaoOAuth2DTO.OAuth2TokenDTO.class);
            return oAuth2TokenDTO.getAccess_token();
        } catch (HttpClientErrorException e) {
            log.error("Failed to get access token from Kakao: {}\n{}", e.getMessage(), e.getResponseBodyAsString());
            throw new ExternalApiException(AuthErrorInfo.FAIL_GET_ACCESS_TOKEN);
        } catch (Exception e) {
            throw new ExternalApiException(AuthErrorInfo.FAIL_PROCESS_RESPONSE);
        }
    }

    @Override
    protected AuthRequest.OAuth2LoginUserInfo getUserInfo(String token) {
        KakaoOAuth2DTO.KakaoProfile kakaoProfile = getKakaoProfile(token);
        return AuthRequest.OAuth2LoginUserInfo.builder()
                .email(kakaoProfile.getKakao_account().getEmail())
                .providerId(String.valueOf(kakaoProfile.getId()))
                .nickname(kakaoProfile.getProperties().getNickname())
                .image(kakaoProfile.getProperties().getProfile_image())
                .providerType(ProviderType.KAKAO)
                .build();
    }

    private KakaoOAuth2DTO.KakaoProfile getKakaoProfile(String token) {
        // 토큰으로 정보 가져오기
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add("Authorization", "Bearer " + token);
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        HttpEntity<MultiValueMap> request1 = new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<String> response2 = restTemplate.exchange(
                    kakaoOAuth2ConfigData.getUserInfoUri(),
                    HttpMethod.GET,
                    request1,
                    String.class
            );

            ObjectMapper om = new ObjectMapper();

            return om.readValue(response2.getBody(), KakaoOAuth2DTO.KakaoProfile.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to get user info from Kakao: {}\n{}", e.getMessage(), e.getResponseBodyAsString());
            throw new ExternalApiException(AuthErrorInfo.FAIL_GET_USER_INFO);
        } catch (Exception e) {
            throw new ExternalApiException(AuthErrorInfo.FAIL_PROCESS_RESPONSE);
        }
    }

    @Override
    protected String getDefaultRedirectUri() {
        return kakaoOAuth2ConfigData.getRedirectUri();
    }
}
