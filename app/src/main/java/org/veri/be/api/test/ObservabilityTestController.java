package org.veri.be.api.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Profile("local")
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class ObservabilityTestController {

    @GetMapping("/trace")
    public Map<String, Object> testTrace() {
        log.info("Testing OpenTelemetry tracing - INFO level");
        log.debug("Testing OpenTelemetry tracing - DEBUG level");
        log.warn("Testing OpenTelemetry tracing - WARN level");

        return Map.of(
            "message", "Trace test endpoint",
            "status", "success",
            "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("/logs")
    public Map<String, Object> testLogs() {
        for (int i = 1; i <= 5; i++) {
            log.info("Log entry #{} - Testing Loki log aggregation", i);
        }

        return Map.of(
            "message", "Logs test endpoint",
            "logsGenerated", 5,
            "status", "success"
        );
    }

    @GetMapping("/error")
    public Map<String, Object> testError() {
        log.error("This is a test error log");
        try {
            throw new RuntimeException("Test exception for observability");
        } catch (RuntimeException e) {
            log.error("Caught test exception", e);
        }

        return Map.of(
            "message", "Error test endpoint",
            "status", "error logged"
        );
    }
}
