FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /workspace

COPY settings.gradle build.gradle gradle.properties* ./
COPY gradle ./gradle
COPY db-migration ./db-migration

RUN gradle :db-migration:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/db-migration/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
