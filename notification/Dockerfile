FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew :notification:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/notification/build/libs/notification-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
