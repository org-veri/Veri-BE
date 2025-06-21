package org.goorm.veri.veribe.global.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfigData {
    private String secret;
    private JwtTime time;

    @Getter
    @Setter
    public static class JwtTime {
        private long access;
        private long refresh;

    }
}
