# Engineering Decisions

## Table of Contents

- [1. Overview](#1-overview)
- [2. Why MSA](#2-why-msa)
- [3. Why Kafka](#3-why-kafka)
- [4. Why Redis](#4-why-redis)
- [5. Why Separate Settlement](#5-why-separate-settlement)
- [6. Related Docs](#6-related-docs)

## 1. Overview

이 문서는 GoodsMall의 주요 설계 선택과 그 이유를 정리한다.

대상은 다음과 같다.

- 왜 서비스 책임을 분리했는가
- 왜 Kafka 기반 비동기 흐름을 사용했는가
- 왜 Redis를 인증/캐시 계층으로 사용했는가
- 왜 정산을 별도 서비스와 배치 흐름으로 분리했는가

## 2. Why MSA

GoodsMall은 일반 구매, 경매, 결제, 정산, 알림, 추천을 하나의 서비스에 모으기보다 서비스별 책임으로 분리하는 방향을 선택했다.

이 선택의 이유는 다음과 같다.

- 주문, 결제, 정산, 경매, 인증의 도메인 규칙이 서로 다르다.
- 후속 처리 실패가 모든 기능에 전파되지 않도록 장애 영향 범위를 줄일 필요가 있다.
- 경매, 정산, AI, 알림은 변경 속도와 운영 방식이 다르다.
- 서비스별로 독립 배포와 점진적 확장이 가능해야 한다.

현재 프로젝트는 `gateway`, `member`, `product`, `cart`, `auction`, `order`, `payment`, `settlement`, `notification`, `ai`로 책임을 나누고 있다.

## 3. Why Kafka

GoodsMall의 핵심 거래 흐름은 주문 요청 한 번으로 끝나지 않는다.

대표적으로 다음 후속 처리가 이어진다.

- 입찰 보증금 차감
- 낙찰 주문 생성
- 구매 확정 이후 escrow release
- 정산 후보 생성
- seller 지급 요청
- 알림 생성

이 흐름을 모두 동기 HTTP 호출로 연결하면 결제 또는 정산 단계의 장애가 앞선 요청 응답까지 직접 영향을 주게 된다.

그래서 프로젝트는 Kafka를 사용해 후속 처리를 비동기 이벤트 흐름으로 분리했다.

이 선택의 이유는 다음과 같다.

- 사용자 요청 응답과 후속 처리를 분리할 수 있다.
- 서비스 간 결합도를 낮출 수 있다.
- consumer 단위로 재처리와 운영 추적이 가능하다.
- outbox와 함께 사용해 DB 상태 변경과 이벤트 발행 사이의 불일치를 줄일 수 있다.

## 4. Why Redis

Redis는 이 프로젝트에서 단기 상태와 빠른 조회가 필요한 데이터를 처리하는 계층으로 사용한다.

주요 용도는 다음과 같다.

- refresh token 저장
- session 저장
- access token / session blacklist 조회
- AI 추천 캐시
- 이벤트 멱등 키 저장
- draft assist lock / 결과 캐시

이 선택의 이유는 다음과 같다.

- TTL 기반 만료 관리가 필요하다.
- Gateway에서 인증 관련 상태를 빠르게 조회해야 한다.
- DB에 영구 보관할 필요가 없는 단기 상태가 많다.
- 추천/보조 기능에서 짧은 캐시와 lock 관리가 필요하다.

## 5. Why Separate Settlement

정산은 결제의 일부처럼 보이지만, 실제 운영 흐름은 결제 승인과 다르다.

GoodsMall에서는 구매 확정 이후에야 seller 정산이 가능한 흐름이 존재한다. 또한 월별 정산, 부분 정산, payout 요청, 지급 결과 반영, 재시도 가능한 실패 처리까지 필요하다.

그래서 정산을 `payment` 내부 로직 하나로 끝내지 않고 `settlement` 서비스와 스케줄링 흐름으로 분리했다.

이 선택의 이유는 다음과 같다.

- 결제 완료 시점과 seller 지급 시점이 다르다.
- escrow release 이후 별도 집계가 필요하다.
- 월별 정산과 부분 정산은 배치성 처리가 적합하다.
- payout 실패와 수동 조치 경로를 분리해 운영할 필요가 있다.

현재 프로젝트는 `payment.settlement-candidate-created` 이벤트로 정산 후보를 적재하고, `settlement.seller-payout-requested` 흐름으로 지급 요청을 전달한다.

## 6. Related Docs

- [02-architecture.md](02-architecture.md)
- [03-service-responsibilities.md](03-service-responsibilities.md)
- [05-event-strategy.md](05-event-strategy.md)
- [08-deployment.md](08-deployment.md)
- [service/settlement-service.md](service/settlement-service.md)
- [service/payment-service.md](service/payment-service.md)
