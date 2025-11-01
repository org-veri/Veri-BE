package org.veri.be.global.config;

import org.veri.be.global.auth.context.MemberContextClearInterceptor;
import org.veri.be.global.interceptors.InjectIPAddressInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InjectIPAddressInterceptor());
        registry.addInterceptor(new MemberContextClearInterceptor());
    }
}
