# Service Docs

## Overview

이 디렉터리는 서비스별 상세 문서를 모아두는 공간이다. 서비스 문서는 아키텍처 일반론보다 특정 서비스의 책임, 상태, API, 이벤트, 외부 의존성, 운영 포인트를 설명하는 데 집중한다.

서비스 문서가 하나의 파일로 충분하면 `*-service.md` 형태를 유지한다. 특정 서비스의 하위 도메인 문서가 많아지면 `docs/service/{service}/` 폴더로 분리하고, 기존 `*-service.md`는 링크 호환용 허브로 남긴다.

---

## Service Index

- [ai-service.md](ai-service.md)
- [auction-service.md](auction-service.md)
- [cart-service.md](cart-service.md)
- [gateway.md](gateway.md)
- [member-service.md](member-service.md)
  - [member/README.md](member/README.md)
  - [member/member.md](member/member.md)
  - [member/auth.md](member/auth.md)
  - [member/verification.md](member/verification.md)
  - [member/seller.md](member/seller.md)
  - [member/report.md](member/report.md)
  - [member/restriction.md](member/restriction.md)
  - [member/common.md](member/common.md)
- [notification-service.md](notification-service.md)
- [order-service.md](order-service.md)
- [payment-service.md](payment-service.md)
- [product-service.md](product-service.md)
- [settlement-service.md](settlement-service.md)

---

## Writing Guide

서비스 상세 문서는 다음 질문에 답해야 한다.

- 이 서비스는 무엇을 책임지는가?
- 어떤 상태와 원본 데이터를 소유하는가?
- 어떤 API를 제공하는가?
- 어떤 이벤트를 발행하거나 소비하는가?
- 어떤 외부 의존성이 있는가?
- 운영 시 어떤 실패 지점과 확인 포인트가 있는가?

문서는 구현 파일을 모두 나열하는 데 집중하지 않는다. 신규 개발자가 서비스를 수정하기 전에 알아야 하는 핵심 구조와 경계를 설명하는 데 집중한다.

---

## Folder Convention

서비스 상세 문서가 커지면 다음 구조를 사용한다.

```text
docs/service/{service}/
|-- README.md
|-- {domain-package}.md
`-- ...
```

기존 링크 호환을 위해 `docs/service/{service}-service.md`는 유지하고, 상세 문서로 이동하는 허브 역할을 맡긴다.

---

## Template

```md
# {Service Name or Domain Package}

## Responsibility

- 서비스 또는 패키지의 핵심 책임

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

## Event Integration

### Produced Events

| Topic | Event Type | When | Outbox |
|---|---|---|---|
| `...` | `...` | 발행 시점 | yes/no |

### Consumed Events

| Topic | Event Type | Purpose | Idempotency |
|---|---|---|---|
| `...` | `...` | 소비 목적 | 기준 |

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

- 장애 시 확인 포인트
- 모니터링 포인트
- 재처리 포인트

## Related Files

- 주요 패키지/클래스 경로

## Related Docs

- 상위 문서
- 연결 서비스 문서
```