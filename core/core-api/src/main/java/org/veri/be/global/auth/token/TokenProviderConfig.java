package org.veri.be.global.auth.token;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.veri.be.lib.auth.token.TokenProvider;

@Configuration
public class TokenProviderConfig {

    @Bean
    @Primary
    public TokenProvider tokenProvider(@Qualifier("jwtService") TokenProvider delegate) {
        return new JwtExceptionHandlingTokenProvider(delegate);
    }
}
