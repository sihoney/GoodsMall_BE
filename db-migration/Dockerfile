FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /workspace

COPY settings.gradle build.gradle gradle.properties* ./
COPY gradle ./gradle
COPY db-migration ./db-migration

RUN ./gradlew :db-migration:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/db-migration/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
