FROM eclipse-temurin:25-jdk AS base

# 실행 환경 설정
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod

FROM base AS final
COPY core/core-api/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
