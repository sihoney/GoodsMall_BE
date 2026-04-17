# 1단계: 빌드 스테이지
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew :auction:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/auction/build/libs/auction-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
