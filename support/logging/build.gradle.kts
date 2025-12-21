plugins {
    `java-library`
    id("io.spring.dependency-management")
}

dependencies {
    api("org.springframework:spring-context")
    api("ch.qos.logback:logback-core")
    api("io.opentelemetry:opentelemetry-api")
    api("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.21.0-alpha")
}
