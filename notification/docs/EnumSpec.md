# Notification Enum Spec

## NotificationType
| Value | Description |
| --- | --- |
| `MEMBER_SIGNED_UP` | 회원 가입 완료 알림 |
| `AUTO_PURCHASE_CONFIRMED` | 자동 구매 확정 알림 |
| `ORDER_PAYMENT_SUCCEEDED` | 주문 결제 성공 알림 |
| `ORDER_PAYMENT_FAILED` | 주문 결제 실패 알림 |
| `SELLER_SETTLEMENT_PAYOUT_SUCCEEDED` | 판매자 정산 지급 성공 알림 |
| `SELLER_SETTLEMENT_PAYOUT_FAILED` | 판매자 정산 지급 실패 알림 |

현재 구현 메모:
- 실제 consumer가 생성하는 타입은 현재 `MEMBER_SIGNED_UP`, `ORDER_PAYMENT_SUCCEEDED`, `ORDER_PAYMENT_FAILED`
- 나머지 타입은 enum과 service 메서드는 준비돼 있지만 아직 consumer handler는 없음

## NotificationReferenceType
| Value | Description |
| --- | --- |
| `PAYMENT` | 결제 참조 |
| `ORDER` | 주문 참조 |
| `SETTLEMENT` | 정산 참조 |
| `WITHDRAWAL` | 출금 참조 |

## NotificationStatus
| Value | Description |
| --- | --- |
| `RECEIVED` | 이벤트 수신 준비 상태 |
| `STORED` | 알림 저장 완료 |
| `PUSHED` | SSE push 성공 |
| `FAILED` | delivery 실패 |
| `RETRYING` | retry 진행 중 |

## NotificationDlqReason
| Value | Description |
| --- | --- |
| `EVENT_PARSE_FAILURE` | 공통 envelope parse 실패 |
| `UNSUPPORTED_EVENT_TYPE` | handler가 없는 eventType |
| `INVALID_EVENT_PAYLOAD` | 필수 필드 누락, payload contract 위반 |
| `TEMPORARY_PROCESSING_ERROR` | retry 후보인 일시적 런타임 오류 |
| `IGNORE_DUPLICATE_EVENT` | 중복 이벤트 등 무시 대상 reason |

## NotificationAction.variant
| Value | Description |
| --- | --- |
| `primary` | 주요 액션 버튼 |
| `secondary` | 보조 액션 버튼 |

## NotificationAction.actionType
| Value | Description |
| --- | --- |
| `navigate` | 프론트 라우팅 이동 |
| `callback` | 클라이언트 추가 처리 필요 |

## Kafka Contract Enums

### OrderPaymentResultStatus
| Value | Description |
| --- | --- |
| `SUCCESS` | 결제 성공 |
| `FAILED` | 결제 실패 |

### OrderPaymentFailureReason
| Value | Description |
| --- | --- |
| `DUPLICATE_ORDER_PAYMENT` | 중복 결제 요청 |
| `WALLET_NOT_FOUND` | 지갑 없음 |
| `INSUFFICIENT_BALANCE` | 잔액 부족 |
| `INVALID_REQUEST` | 잘못된 결제 요청 |
| `INTERNAL_ERROR` | 내부 오류 |

### SellerSettlementPayoutResultStatus
| Value | Description |
| --- | --- |
| `SUCCESS` | 정산 지급 성공 |
| `FAILED` | 정산 지급 실패 |

### PayoutFailureReason
| Value | Description |
| --- | --- |
| `WALLET_NOT_FOUND` | 지갑 없음 |
| `INVALID_PAYOUT_AMOUNT` | 잘못된 지급 금액 |
| `DUPLICATE_PAYOUT` | 중복 지급 |
| `SETTLEMENT_NOT_FOUND` | 정산 건 없음 |
| `TEMPORARY_DB_ERROR` | 일시적 DB 오류 |
| `KAFKA_PUBLISH_ERROR` | 이벤트 발행 실패 |
| `INTERNAL_ERROR` | 내부 오류 |
