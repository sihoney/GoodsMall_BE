FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew :db-migration:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/db-migration/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
