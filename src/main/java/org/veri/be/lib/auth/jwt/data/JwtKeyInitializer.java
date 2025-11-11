package org.veri.be.lib.auth.jwt.data;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.veri.be.lib.auth.jwt.JwtUtil;

@ConditionalOnProperty(name = "auth.jwt.use", havingValue = "true")
@Component
public class JwtKeyInitializer {

    private final JwtProperties jwtProperties;

    public JwtKeyInitializer(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        System.out.println("Initializing JWT keys...");
        JwtUtil.init(jwtProperties);
    }
}
