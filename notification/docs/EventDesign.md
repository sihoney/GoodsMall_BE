# Notification Event Design

## 소비 이벤트 목록
| 이벤트 | 토픽 | Consumer | 생성 알림 |
| --- | --- | --- | --- |
| `AutoPurchaseConfirmedMessage` | `payment.auto-purchase-confirmed` | `AutoPurchaseConfirmedEventConsumer` | 자동 구매 확정 |
| `OrderPaymentResultMessage` | `payment.order-payment-result` | `OrderPaymentResultEventConsumer` | 결제 성공 또는 실패 |
| `SellerSettlementPayoutResultMessage` | `payment.seller-payout-result` | `SellerSettlementPayoutResultEventConsumer` | 정산 지급 성공 또는 실패 |

## 메시지 스펙

### `AutoPurchaseConfirmedMessage`
| 필드 | 타입 |
| --- | --- |
| `orderId` | UUID |
| `buyerMemberId` | UUID |
| `confirmedAt` | Instant |

### `OrderPaymentResultMessage`
| 필드 | 타입 |
| --- | --- |
| `eventId` | UUID |
| `orderId` | UUID |
| `buyerMemberId` | UUID |
| `amount` | BigDecimal |
| `status` | `OrderPaymentResultStatus` |
| `reasonCode` | `OrderPaymentFailureReason` |
| `occurredAt` | Instant |

### `SellerSettlementPayoutResultMessage`
| 필드 | 타입 |
| --- | --- |
| `eventId` | UUID |
| `requestEventId` | UUID |
| `settlementId` | UUID |
| `sellerMemberId` | UUID |
| `payoutAmount` | Long |
| `resultStatus` | `SellerSettlementPayoutResultStatus` |
| `failureReason` | `PayoutFailureReason` |
| `processedAt` | LocalDateTime |

## 처리 흐름
1. Kafka consumer 가 메시지 수신
2. 필수 필드 검증
3. 필요 시 시간대 변환 수행
4. 성공/실패 상태에 따라 적절한 알림 생성 use case 호출
5. `notification` 테이블에 저장

## 시간 처리
- `AutoPurchaseConfirmedMessage.confirmedAt` 는 `Instant -> Asia/Seoul LocalDateTime` 변환
- `OrderPaymentResultMessage.occurredAt` 도 동일하게 변환
- `SellerSettlementPayoutResultMessage.processedAt` 는 이미 `LocalDateTime`

## 실패 처리
- 필수 값 누락 시 `IllegalArgumentException`
- 현재 재시도 정책, DLQ, 중복 이벤트 방지 키는 명시적으로 구현되어 있지 않다
