# Service Docs

## Overview

이 문서는 서비스별 상세 문서를 모아두는 인덱스이며, 각 서비스 문서가 공통으로 따라야 할 작성 기준을 정의한다.

서비스 상세 문서는 아키텍처 일반론이 아니라, 특정 서비스를 실제로 이해하고 수정할 때 필요한 정보를 정리하는 문서여야 한다.

---

## Service Index

- [ai-service.md](ai-service.md)
- [auction-service.md](auction-service.md)
- [cart-service.md](cart-service.md)
- [gateway.md](gateway.md)
- [member-service.md](member-service.md)
- [notification-service.md](notification-service.md)
- [order-service.md](order-service.md)
- [payment-service.md](payment-service.md)
- [product-service.md](product-service.md)
- [settlement-service.md](settlement-service.md)

---

## Writing Guide

서비스 상세 문서는 아래 질문에 답해야 한다.

- 이 서비스는 무엇을 책임지는가?
- 어떤 상태를 소유하는가?
- 어떤 API를 제공하는가?
- 어떤 이벤트를 발행하고 구독하는가?
- 어떤 외부 의존성이 있는가?
- 어떤 운영 포인트와 장애 위험이 있는가?

문서는 구현 파일을 모두 나열하는 대신, 새로운 개발자가 이 서비스를 수정하기 전에 알아야 하는 핵심 구조를 설명하는 데 집중한다.

---

## Template

```md
# {Service Name}

## Overview

- 서비스 목적
- 핵심 책임
- 이 서비스가 존재하는 이유

## Owned Domain / Data

- 소유 엔티티
- 핵심 상태값
- 다른 서비스가 직접 수정하면 안 되는 원본 데이터

## Main Use Cases

- 대표 사용자/운영 시나리오
- 서비스가 처리하는 핵심 요청 흐름

## API Surface

| Endpoint | Method | Purpose | Auth |
|---|---|---|---|
| `/api/...` | `GET/POST/...` | 설명 | 필요 role 또는 public |

- 외부 API / 내부 API 구분
- 중요한 request/response 특징

## Request Flow in Service

- Controller
- Application Service
- Domain
- Repository

복잡한 흐름은 1~2개 정도만 대표적으로 설명한다.

## Event Integration

### Produced Events

| Topic | Event Type | When | Outbox |
|---|---|---|---|
| `...` | `...` | 언제 발행되는지 | yes/no |

### Consumed Events

| Topic | Event Type | Purpose | Idempotency |
|---|---|---|---|
| `...` | `...` | 무엇을 위해 소비하는지 | 기준 |

### Failure Handling

- retry 적용 여부
- DLQ 적용 여부
- 중복 처리 기준

## External Dependencies

- PostgreSQL / Redis / Kafka / Elasticsearch / S3
- 외부 API, OAuth provider, PG, mail provider 등

## Security / Authorization

- 접근 가능한 role
- 소유권 검증 여부
- Gateway 검증과 서비스 내부 검증의 경계

## Transaction / Consistency

- 주요 `@Transactional` 경계
- 동기 처리와 비동기 처리 경계
- eventual consistency가 발생하는 지점

## Operational Notes

- 스케줄러 여부
- 락/동시성 주의점
- 장애 시 취약 지점
- 모니터링 포인트
- 재처리 포인트

## Related Files

- 주요 패키지/클래스 경로

## Related Docs

- 상위 문서
- 연관 서비스 문서
```

---

## Notes

- 모든 API 스펙을 문서에 복붙하지 않는다.
- 모든 클래스 목록을 문서화하지 않는다.
- 상위 문서에 이미 있는 일반론을 반복하지 않는다.
- 서비스 문서는 책임, 상태, API/이벤트 입출력, 의존성, 운영 포인트를 우선 설명한다.
