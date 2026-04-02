# 1단계: 빌드 스테이지
FROM gradle:9.0.0-jdk21 AS builder

WORKDIR /workspace

COPY . .

RUN ./gradlew :config:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

# 2단계: 실행 스테이지
## 1. eclipse-temurin:21-jre 로 이미지 생성
FROM eclipse-temurin:21-jre

## 2. 작업 위치 변경 /app
WORKDIR /app

## 3. 위에서 만들어진 config-0.0.1-SNAPSHOT.jar 를 app.jar로 복사
COPY --from=builder /workspace/config/build/libs/config-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8888

## Java 런타임에 실행 / JAR 파일을 / 실행할 JAR 파일
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
