# Notification Enum Spec

## 도메인 Enum

### `NotificationType`
| 값 | 의미 |
| --- | --- |
| `AUTO_PURCHASE_CONFIRMED` | 자동 구매 확정 안내 |
| `ORDER_PAYMENT_SUCCEEDED` | 주문 결제 성공 안내 |
| `ORDER_PAYMENT_FAILED` | 주문 결제 실패 안내 |
| `SELLER_SETTLEMENT_PAYOUT_SUCCEEDED` | 판매자 정산 지급 성공 안내 |
| `SELLER_SETTLEMENT_PAYOUT_FAILED` | 판매자 정산 지급 실패 안내 |

### `NotificationReferenceType`
| 값 | 의미 |
| --- | --- |
| `PAYMENT` | 결제 리소스 참조 |
| `ORDER` | 주문 리소스 참조 |
| `SETTLEMENT` | 정산 리소스 참조 |
| `WITHDRAWAL` | 출금 리소스 참조 |

## Kafka 계약 Enum

### `OrderPaymentResultStatus`
| 값 | 의미 |
| --- | --- |
| `SUCCESS` | 결제 성공 |
| `FAILED` | 결제 실패 |

### `OrderPaymentFailureReason`
| 값 | 의미 |
| --- | --- |
| `DUPLICATE_ORDER_PAYMENT` | 중복 결제 요청 |
| `WALLET_NOT_FOUND` | 지갑 없음 |
| `INSUFFICIENT_BALANCE` | 잔액 부족 |
| `INVALID_REQUEST` | 잘못된 결제 요청 |
| `INTERNAL_ERROR` | 내부 오류 |

### `SellerSettlementPayoutResultStatus`
| 값 | 의미 |
| --- | --- |
| `SUCCESS` | 정산 지급 성공 |
| `FAILED` | 정산 지급 실패 |

### `PayoutFailureReason`
| 값 | 의미 |
| --- | --- |
| `WALLET_NOT_FOUND` | 지갑 없음 |
| `INVALID_PAYOUT_AMOUNT` | 잘못된 지급 금액 |
| `DUPLICATE_PAYOUT` | 중복 지급 |
| `SETTLEMENT_NOT_FOUND` | 정산 건 없음 |
| `TEMPORARY_DB_ERROR` | 일시적 DB 오류 |
| `KAFKA_PUBLISH_ERROR` | 이벤트 발행 실패 |
| `INTERNAL_ERROR` | 내부 오류 |
