FROM gradle:9.0.0-jdk21 AS builder

WORKDIR /workspace

COPY . .

RUN ./gradlew :payment:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/payment/build/libs/payment-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/app.jar"]
