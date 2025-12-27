dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-aspectj")
    api("io.micrometer:micrometer-registry-prometheus")
    api("org.springframework.boot:spring-boot-starter-opentelemetry")
    api("io.opentelemetry:opentelemetry-exporter-otlp")
    api("net.ttddyy.observation:datasource-micrometer-spring-boot:2.0.1")

    api("org.springframework.boot:spring-boot-starter-web")
}
