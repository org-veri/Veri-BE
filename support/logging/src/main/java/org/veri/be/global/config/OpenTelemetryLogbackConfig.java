package org.veri.be.global.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenTelemetryLogbackConfig {

    @Bean
    InitializingBean openTelemetryLogbackAppender(OpenTelemetry openTelemetry) {
        return () -> OpenTelemetryAppender.install(openTelemetry);
    }
}
