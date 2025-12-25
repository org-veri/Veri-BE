package org.veri.be.monitoring.config;

import io.micrometer.observation.ObservationPredicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import java.util.Set;

@Configuration
public class ObservationConfig {

    private static final Set<String> EXCLUDED_URI_PREFIXES = Set.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    );

    private static final Set<String> EXCLUDED_METHODS = Set.of(
            "HEAD",
            "OPTIONS",
            "TRACE"
    );

    private static final Set<String> EXCLUDED_OBSERVATION_NAMES = Set.of(
            "spring.security",
            "filterchain",
            "connection"
    );

    @Bean
    ObservationPredicate globalTraceFilter() {
        return (name, context) -> {
            if (EXCLUDED_OBSERVATION_NAMES.stream().anyMatch(name::startsWith)) {
                return false;
            }

            if (context instanceof ServerRequestObservationContext serverContext) {
                HttpServletRequest request = serverContext.getCarrier();
                String uri = request.getRequestURI();
                String method = request.getMethod();

                if (EXCLUDED_URI_PREFIXES.stream().anyMatch(uri::startsWith)) {
                    return false;
                }

                if (EXCLUDED_METHODS.contains(method.toUpperCase())) {
                    return false;
                }
            }

            return true;
        };
    }
}
