FROM eclipse-temurin:17-jdk AS build

RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor \
    tesseract-ocr-eng \
    libtesseract-dev \
    libleptonica-dev \
    && apt-get clean

WORKDIR /app
COPY build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

ENV JAVA_TOOL_OPTIONS="-Djava.library.path=/usr/lib/x86_64-linux-gnu -Djna.library.path=/usr/lib/x86_64-linux-gnu"

ENTRYPOINT ["java", "-jar", "app.jar"]
