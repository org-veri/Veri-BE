package org.goorm.veri.veribe.global.config;

import org.goorm.veri.veribe.global.auth.context.MemberContextClearInterceptor;
import org.goorm.veri.veribe.global.interceptors.InjectIPAddressInterceptor;
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
