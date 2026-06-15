# Seller Package

## 목차

- [1. 책임](#1-책임)
- [2. 도메인 모델](#2-도메인-모델)
- [3. Redis 상태](#3-redis-상태)
- [4. 주요 서비스](#4-주요-서비스)
- [5. 포트](#5-포트)
- [6. 인프라 어댑터](#6-인프라-어댑터)
- [7. 주요 흐름](#7-주요-흐름)
- [8. 관련 파일](#8-관련-파일)

---

## 1. 책임

`seller` 패키지는 회원의 판매자 등록과 판매자 전환을 관리한다.

주요 책임:

- 판매자 등록 요청
- 현재 판매자 정보 조회
- 계좌 인증 완료 후 판매자 전환
- 판매자 정산 계좌 정보 암호화
- 판매자 등록 draft Redis 저장
- 판매자 전환 이벤트 발행

---

## 2. 도메인 모델

| 엔티티 | 설명 |
|---|---|
| `Seller` | 판매자 정보와 정산 계좌 정보 |

---

## 3. Redis 상태

| 상태 | 설명 |
|---|---|
| `SellerDraft` | 계좌 인증 완료 전 판매자 등록 임시 데이터 |

`SellerDraft`는 정산 도메인의 원장 데이터가 아니다. 판매자 등록 요청에서 입력한 은행/계좌 정보를 계좌 인증 완료 전까지 임시 보관하는 Redis 상태다. 계좌 인증이 완료되면 `SellerPromotionService`가 draft를 읽어 `Seller`를 생성하고, 회원 role을 `SELLER`로 변경한 뒤 draft를 삭제한다.

---

## 4. 주요 서비스

| 클래스 | 책임 |
|---|---|
| `SellerService` | 판매자 등록 요청과 조회 |
| `SellerPromotionService` | 회원을 판매자로 전환 |
| `AccountEncryptionService` | 정산 계좌 정보 암호화 |

---

## 5. 포트

| 포트 | 방향 | 설명 |
|---|---|---|
| `SellerUsecase` | in | 판매자 유스케이스 |
| `SellerPersistencePort` | out | 판매자 저장소 접근 |
| `SellerEventPort` | out | 판매자 이벤트 발행 |

---

## 6. 인프라 어댑터

| 어댑터 | 설명 |
|---|---|
| `SellerJpaAdapter` | 판매자 JPA 저장소 adapter |
| `RedisSellerDraftStore` | 판매자 등록 draft 저장 |
| `SellerEventPublisher` | 판매자 이벤트 발행 조율 |
| `SellerEventKafkaProducer` | 판매자 이벤트 Kafka 발행 |

---

## 7. 주요 흐름

- 판매자 등록 요청
- 계좌 인증
- 판매자 전환
- `SELLER_PROMOTED` 이벤트 발행

---

## 8. 관련 파일

- `service/member/src/main/java/com/example/member/seller/**`