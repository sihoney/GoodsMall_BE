# 1단계: 빌드 스테이지
# Gradle 9.0.0 + JDK 21 이미지를 빌드용으로 사용
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew :cart:bootJar --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=512m"

# 2단계: 실행 스테이지
# JRE만 포함된 경량 이미지 사용 (JDK 불필요, 이미지 크기 절감)
FROM eclipse-temurin:21-jre

# 애플리케이션 실행 디렉토리
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사 (빌드 도구 제외)
COPY --from=builder /workspace/cart/build/libs/cart-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너가 사용할 포트 명시 (문서화 목적)
EXPOSE 8086

# 컨테이너 시작 시 실행할 명령어 (로컬 환경이 아닌
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/app.jar"]
