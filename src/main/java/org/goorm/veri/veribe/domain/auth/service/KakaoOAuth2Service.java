package org.goorm.veri.veribe.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.goorm.veri.veribe.domain.auth.dto.KakaoOAuth2DTO;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2ErrorCode;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2Exception;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.goorm.veri.veribe.global.data.KakaoOAuth2ConfigData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoOAuth2Service extends AbstractOAuth2Service {

    private final KakaoOAuth2ConfigData kakaoOAuth2ConfigData;

    public KakaoOAuth2Service(MemberRepository memberRepository, TokenCommandService tokenCommandService, KakaoOAuth2ConfigData kakaoOAuth2ConfigData) {
        super(memberRepository, tokenCommandService);
        this.kakaoOAuth2ConfigData = kakaoOAuth2ConfigData;
    }

    @Override
    protected String getAccessToken(String code) {
        // 인가코드 토큰 가져오기
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", kakaoOAuth2ConfigData.getClientId());
        map.add("redirect_uri", kakaoOAuth2ConfigData.getRedirectUri());
        map.add("code", code);
        HttpEntity<MultiValueMap> request = new HttpEntity<>(map, httpHeaders);

        ResponseEntity<String> response1 = restTemplate.exchange(
                kakaoOAuth2ConfigData.getTokenUri(),
                HttpMethod.POST,
                request,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoOAuth2DTO.OAuth2TokenDTO oAuth2TokenDTO = null;

        try {
            oAuth2TokenDTO = objectMapper.readValue(response1.getBody(), KakaoOAuth2DTO.OAuth2TokenDTO.class);
            return oAuth2TokenDTO.getAccess_token();
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ErrorCode.FAIL_ACCESS_TOKEN);
        }
    }

    @Override
    protected OAuth2Request.OAuth2LoginUserInfo getUserInfo(String token) {
        KakaoOAuth2DTO.KakaoProfile kakaoProfile = getKakaoProfile(token);
        return OAuth2Request.OAuth2LoginUserInfo.builder()
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

        ResponseEntity<String> response2 = restTemplate.exchange(
                kakaoOAuth2ConfigData.getUserInfoUri(),
                HttpMethod.GET,
                request1,
                String.class
        );

        ObjectMapper om = new ObjectMapper();

        try {
            return om.readValue(response2.getBody(), KakaoOAuth2DTO.KakaoProfile.class);
        } catch(Exception e) {
            throw new OAuth2Exception(OAuth2ErrorCode.FAIL_USER_INFO);
        }
    }
}
