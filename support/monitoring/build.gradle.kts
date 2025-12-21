plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-registry-prometheus")
    api("org.springframework.boot:spring-boot-starter-opentelemetry")
    api("io.opentelemetry:opentelemetry-exporter-otlp")
}
