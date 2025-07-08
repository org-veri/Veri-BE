FROM eclipse-temurin:17-jdk AS base

# Tesseract 및 OCR 패키지 설치 (변경되지 않는 레이어)
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor \
    tesseract-ocr-eng \
    libtesseract-dev \
    libleptonica-dev \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# 실행 환경 설정
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-Djava.library.path=/usr/lib/x86_64-linux-gnu -Djna.library.path=/usr/lib/x86_64-linux-gnu"
ENV SPRING_PROFILES_ACTIVE=prod

FROM base AS final
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]