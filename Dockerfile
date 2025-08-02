FROM eclipse-temurin:17-jdk AS base

# 실행 환경 설정
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod

FROM base AS final
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
