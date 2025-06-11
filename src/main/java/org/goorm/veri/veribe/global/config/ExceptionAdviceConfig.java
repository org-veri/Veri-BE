package org.goorm.veri.veribe.global.config;

import org.namul.api.payload.code.dto.supports.DefaultResponseErrorReasonDTO;
import org.namul.api.payload.error.configurer.DefaultExceptionAdviceConfigurer;
import org.namul.api.payload.error.configurer.ExceptionAdviceConfigurer;
import org.namul.api.payload.writer.FailureResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionAdviceConfig {

    @Bean
    ExceptionAdviceConfigurer<DefaultResponseErrorReasonDTO> exceptionAdviceConfigurer(FailureResponseWriter<DefaultResponseErrorReasonDTO> failureResponseWriter) {
        return new DefaultExceptionAdviceConfigurer(failureResponseWriter);
    }
}
