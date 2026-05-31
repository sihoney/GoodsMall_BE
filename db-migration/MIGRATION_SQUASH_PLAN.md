# Migration Squash Plan

## 1. 목적

기존 `db/migration/V*.sql` 파일을 바로 삭제하지 않고, 최종 스키마 기준의 baseline migration 초안을 별도 경로에 정리하기 위한 계획이다.

전제는 다음과 같다.

- 기존에 반영된 DB가 없다고 가정한다.
- 기존 `V*.sql`은 비교와 복구를 위해 보관한다.
- 실제 전환 전까지는 현재 Flyway scan 경로인 `classpath:db/migration`을 유지한다.

## 2. 보관 전략

기존 migration 파일은 아래 경로에 복사해 보관한다.

```text
db-migration/src/main/resources/db/migration-legacy/
```

draft baseline은 아래 경로에 작성한다.

```text
db-migration/src/main/resources/db/migration-squash-draft/
```

`migration-squash-draft`는 현재 Flyway scan 경로가 아니므로 자동 실행 대상이 아니다.

## 3. Draft Baseline 분리 기준

baseline 초안은 서비스 소유 스키마 단위로 분리한다.

1. `V1__create_payment_schema.sql`
2. `V2__create_settlement_schema.sql`
3. `V3__create_member_schema.sql`
4. `V4__create_cart_schema.sql`
5. `V5__create_product_schema.sql`
6. `V6__create_notification_schema.sql`
7. `V7__create_order_schema.sql`
8. `V8__create_ai_schema.sql`
9. `V9__create_auction_schema.sql`
정리 기준:

- `payment.auction_deposit`은 auction 흐름에서 사용되지만 payment schema에 속하므로 `V46`, `V105`를 payment draft에 둔다.
- 서비스별 테이블 생성 파일에는 해당 서비스가 소유한 schema/table/index만 둔다.
- courier 기본 데이터처럼 schema가 아닌 기준 데이터는 migration이 아니라 seed로 분리한다.

## 4. 흡수 대상과 제외 대상

baseline에는 최종 DDL 기준의 구조 변경만 흡수한다.

흡수 대상:

- schema 생성
- table 생성
- column 추가, 삭제, 타입 변경, nullable 변경, default 변경
- check constraint, foreign key, unique, index
- outbox 같은 보조 테이블 생성

제외 대상:

- 기존 데이터 값을 바꾸기 위한 `UPDATE` 성격의 migration
- 운영 중 생성된 레코드의 정합성을 맞추기 위한 backfill SQL
- 기준 데이터 insert

제외 예시:

- `V16__update_charge_status_values.sql`
- `V102__sync_order_item_status_with_active_return_request.sql`

## 5. 현재 반영한 보정

- `auction_deposit.bid_id`는 payment 도메인 엔티티 기준에 맞춰 `NOT NULL`로 정리했다.
- `settlement` 월별 정산 unique index는 settlement draft에 포함했다.
- 금액 컬럼 타입은 `DECIMAL(19,2)` 기준으로 맞췄다.
- `order_service.couriers` 기본 데이터는 schema migration에서 제거하고 seed 파일로 이동했다.
- payment/settlement, cart/product, notification/order로 묶여 있던 draft 파일을 서비스 단위로 분리했다.

## 6. 검증 순서

1. 빈 DB에서 draft baseline만 실행한다.
2. seed 실행 여부와 seed 순서를 확인한다.
3. 주요 서비스 기동을 확인한다.
4. 기존 migration 적용 결과와 draft baseline 적용 결과의 최종 schema 차이를 비교한다.

우선 검증 대상 서비스:

- `member`
- `product`
- `auction`
- `order`
- `payment`
- `settlement`

## 7. 보류 판단 기준

아래 중 하나라도 불확실하면 실제 squash 전환 전에 SQL 본문을 다시 확인한다.

- 데이터 보정 성격이 강한 migration인지
- create 시점에 바로 흡수해도 되는 구조 변경인지
- 운영 중 데이터 상태를 전제로 하는 SQL인지
- index/constraint가 create table 안에 자연스럽게 들어갈 수 있는지
- seed 파일에 과거 상태값이나 과거 컬럼 기준 데이터가 남아 있는지
