FROM gradle:9.0.0-jdk21 AS builder

WORKDIR /workspace

COPY . .

RUN gradle :gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
