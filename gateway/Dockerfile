FROM gradle:9.0.0-jdk21 AS builder

WORKDIR /workspace

COPY . .

RUN ./gradlew :gateway:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
