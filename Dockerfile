FROM eclipse-temurin:25-jre AS final
WORKDIR /app
COPY core/core-app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul \
-Xmx200m \
-Xss512k \
-XX:MaxDirectMemorySize=48m \
-XX:MaxMetaspaceSize=96m"

ENTRYPOINT ["java","-jar","app.jar"]
