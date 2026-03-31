FROM gradle:9.0.0-jdk21 AS builder

WORKDIR /workspace

COPY . .

RUN gradle :member:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/member/build/libs/member-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
