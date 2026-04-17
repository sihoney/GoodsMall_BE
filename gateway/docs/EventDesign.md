# Gateway Event Design

## 개요
- gateway 모듈은 Kafka, 도메인 이벤트, 애플리케이션 이벤트를 발행하거나 소비하지 않는다.
- 역할은 요청 흐름 제어와 라우팅이다.

## 요청 흐름 관점의 이벤트성 동작
| 단계 | 설명 |
| --- | --- |
| Route Match | 요청 경로를 기준으로 대상 서비스 결정 |
| JWT Validation | 토큰 검증 성공/실패 판단 |
| Header Enrichment | 회원 식별 헤더 추가 |
| Forward | downstream 서비스로 전달 |

## 후속 확장 포인트
- 인증 실패 감사 로그 이벤트
- rate limiting 초과 이벤트
- 분산 추적 correlation ID 전파
