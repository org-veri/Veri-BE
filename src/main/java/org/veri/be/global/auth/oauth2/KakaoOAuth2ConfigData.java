package org.veri.be.global.auth.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "oauth2.kakao")
public class KakaoOAuth2ConfigData {
    private String clientId;
    private String redirectUri;
    private String authorizationUri;
    private String tokenUri;
    private String userInfoUri;
}
