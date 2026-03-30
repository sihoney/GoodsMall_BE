FROM gradle:9.0.0-jdk21 AS builder

WORKDIR /workspace

COPY . .

RUN gradle :discovery:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/discovery/build/libs/discovery-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
