# Common Package

## 목차

- [1. 책임](#1-책임)
- [2. 주요 컴포넌트](#2-주요-컴포넌트)
- [3. 관련 파일](#3-관련-파일)

---

## 1. 책임

`common` 패키지는 Member Service 내부 공통 인프라와 웹 지원 코드를 제공한다.

주요 책임:

- 공통 API 응답
- 공통 예외 처리
- Kafka topic 상수
- 설정 properties
- 인증 컨텍스트 복원
- session metadata 추출
- Jackson, Kafka, S3, SMTP, OAuth, password encoder 설정

---

## 2. 주요 컴포넌트

| 컴포넌트 | 설명 |
|---|---|
| `ApiResponse` | 공통 API 응답 wrapper |
| `MemberExceptionHandler` | Member Service 공통 예외 처리 |
| `KafkaTopics` | Kafka topic 상수 |
| `AuthSessionMetadataExtractor` | 요청에서 세션 메타데이터 추출 |
| `WebMvcConfig` | 웹 계층 공통 설정 |
| `KafkaProducerConfig` | Kafka producer 설정 |
| `S3Config` / `S3Properties` | 프로필 이미지 presigned URL 설정 |
| `EmailSenderConfig` / `EmailProperties` | 이메일 발송 설정 |
| `KakaoOAuthProperties` | Kakao OAuth 설정 |

---

## 3. 관련 파일

- `service/member/src/main/java/com/example/member/common/**`