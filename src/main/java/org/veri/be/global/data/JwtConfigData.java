package org.veri.be.global.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfigData {

    private TokenConfig access;
    private TokenConfig refresh;

    @Getter
    @Setter
    public static class TokenConfig {
        private String secret;
        private long validity;
    }
}
